-- Lingo Live 1.4 — live call backend
--
-- Run this once in a fresh Supabase project's SQL editor (or apply it as a
-- migration). Audio never travels through Supabase: LiveKit carries the
-- voice. Supabase owns accounts, friend codes, matching, safety, ratings, and
-- the tiny amount of call history shown in the app.
--
-- This file intentionally replaces the old preview schema. The old text-chat
-- schema is preserved as legacy_chat_schema.sql for history only.

create extension if not exists pgcrypto;

do $$
begin
  create type public.cefr_level as enum ('A1', 'A2', 'B1', 'B2', 'C1', 'C2', 'TOP');
exception
  when duplicate_object then null;
end $$;

-- ---------------------------------------------------------------------------
-- Profiles
-- ---------------------------------------------------------------------------
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default 'Learner',
  country text,
  avatar_sticker text not null default 'SPROUT',
  cefr_level public.cefr_level not null default 'A1',
  friend_code text not null default upper(substr(md5(random()::text || clock_timestamp()::text), 1, 6)),
  rating_sum integer not null default 0 check (rating_sum >= 0),
  rating_count integer not null default 0 check (rating_count >= 0),
  calls_completed integer not null default 0 check (calls_completed >= 0),
  seconds_talked bigint not null default 0 check (seconds_talked >= 0),
  is_banned boolean not null default false,
  created_at timestamptz not null default now(),
  last_seen timestamptz not null default now(),
  constraint display_name_length check (char_length(display_name) between 2 and 32),
  constraint friend_code_shape check (friend_code ~ '^[A-Z0-9]{6}$')
);

create unique index if not exists profiles_friend_code_idx
  on public.profiles (friend_code);

create index if not exists profiles_level_seen_idx
  on public.profiles (cefr_level, last_seen desc);

-- New auth users get a profile immediately. The app fills in the level and
-- display name after the first successful session.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, display_name, avatar_sticker)
  values (
    new.id,
    case
      when char_length(trim(coalesce(new.raw_user_meta_data ->> 'display_name', ''))) >= 2
        then left(trim(new.raw_user_meta_data ->> 'display_name'), 32)
      else 'Learner ' || left(new.id::text, 4)
    end,
    coalesce(
      nullif(trim(new.raw_user_meta_data ->> 'avatar_sticker'), ''),
      'SPROUT'
    )
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- Server-owned fields cannot be edited directly by a signed-in client. The
-- security-definer functions below opt in explicitly with a transaction-local
-- setting so the trigger still protects the public API.
create or replace function public.protect_profile_fields()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if current_setting('lingo.profile_write', true) = 'on' then
    return new;
  end if;

  if coalesce(auth.role(), '') <> 'service_role' then
    if new.cefr_level is distinct from old.cefr_level
       or new.friend_code is distinct from old.friend_code
       or new.rating_sum is distinct from old.rating_sum
       or new.rating_count is distinct from old.rating_count
       or new.calls_completed is distinct from old.calls_completed
       or new.seconds_talked is distinct from old.seconds_talked
       or new.is_banned is distinct from old.is_banned then
      raise exception 'This profile field is managed by Lingo Live';
    end if;
  end if;
  return new;
end;
$$;

drop trigger if exists protect_profile_fields on public.profiles;
create trigger protect_profile_fields
  before update on public.profiles
  for each row execute function public.protect_profile_fields();

-- ---------------------------------------------------------------------------
-- Call queue, sessions, ratings, safety
-- ---------------------------------------------------------------------------
create table if not exists public.call_queue (
  user_id uuid primary key references public.profiles(id) on delete cascade,
  level public.cefr_level not null,
  joined_at timestamptz not null default now(),
  last_seen timestamptz not null default now(),
  expires_at timestamptz not null default (now() + interval '2 minutes')
);

create table if not exists public.call_sessions (
  id uuid primary key default gen_random_uuid(),
  room_name text not null unique default 'lingo-' || replace(gen_random_uuid()::text, '-', ''),
  level public.cefr_level not null,
  caller_id uuid not null references public.profiles(id) on delete cascade,
  callee_id uuid not null references public.profiles(id) on delete cascade,
  call_kind text not null default 'match' check (call_kind in ('match', 'friend')),
  status text not null default 'ringing' check (status in ('ringing', 'active', 'ended')),
  end_reason text check (
    end_reason in ('ended', 'skipped', 'disconnected', 'reported', 'declined', 'no_answer')
  ),
  created_at timestamptz not null default now(),
  started_at timestamptz,
  ended_at timestamptz,
  constraint call_participants_differ check (caller_id <> callee_id)
);

alter table public.call_sessions
  drop constraint if exists call_sessions_end_reason_check;
alter table public.call_sessions
  add constraint call_sessions_end_reason_check check (
    end_reason in ('ended', 'skipped', 'disconnected', 'reported', 'declined', 'no_answer')
  );

create index if not exists call_sessions_caller_idx
  on public.call_sessions (caller_id, created_at desc);
create index if not exists call_sessions_callee_idx
  on public.call_sessions (callee_id, created_at desc);
create index if not exists call_sessions_live_idx
  on public.call_sessions (status, created_at desc);
create index if not exists call_queue_level_time_idx
  on public.call_queue (level, joined_at);

create table if not exists public.call_ratings (
  id bigint generated by default as identity primary key,
  session_id uuid not null references public.call_sessions(id) on delete cascade,
  rater_id uuid not null references public.profiles(id) on delete cascade,
  rated_id uuid not null references public.profiles(id) on delete cascade,
  stars smallint not null check (stars between 1 and 5),
  created_at timestamptz not null default now(),
  unique (session_id, rater_id),
  constraint rating_not_self check (rater_id <> rated_id)
);

create table if not exists public.blocks (
  blocker_id uuid not null references public.profiles(id) on delete cascade,
  blocked_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (blocker_id, blocked_id),
  constraint cannot_block_self check (blocker_id <> blocked_id)
);

create table if not exists public.reports (
  id bigint generated by default as identity primary key,
  session_id uuid references public.call_sessions(id) on delete set null,
  reporter_id uuid not null references public.profiles(id) on delete cascade,
  reported_id uuid not null references public.profiles(id) on delete cascade,
  reason text not null check (reason in ('bad_language', 'harassment', 'sexual', 'threat', 'other')),
  created_at timestamptz not null default now(),
  constraint report_not_self check (reporter_id <> reported_id)
);

create index if not exists reports_reported_idx
  on public.reports (reported_id, created_at desc);

create or replace function public.apply_call_rating()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  perform set_config('lingo.profile_write', 'on', true);
  update public.profiles
  set rating_sum = rating_sum + new.stars,
      rating_count = rating_count + 1
  where id = new.rated_id;
  return new;
end;
$$;

drop trigger if exists apply_call_rating on public.call_ratings;
create trigger apply_call_rating
  after insert on public.call_ratings
  for each row execute function public.apply_call_rating();

create or replace function public.block_reported_player()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.blocks (blocker_id, blocked_id)
  values (new.reporter_id, new.reported_id)
  on conflict do nothing;
  return new;
end;
$$;

drop trigger if exists block_reported_player on public.reports;
create trigger block_reported_player
  after insert on public.reports
  for each row execute function public.block_reported_player();

-- ---------------------------------------------------------------------------
-- Weekly leaderboard (leaderboard-only, never a promotion path)
-- ---------------------------------------------------------------------------
create table if not exists public.weekly_scores (
  user_id uuid not null references public.profiles(id) on delete cascade,
  week_start date not null,
  level public.cefr_level not null,
  score smallint not null check (score between 0 and 10),
  created_at timestamptz not null default now(),
  primary key (user_id, week_start)
);

-- ---------------------------------------------------------------------------
-- Helpers
-- ---------------------------------------------------------------------------
create or replace function public.my_level()
returns public.cefr_level
language sql
stable
security definer
set search_path = public
as $$
  select cefr_level from public.profiles where id = auth.uid();
$$;

create or replace function public.is_active_user(uid uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.profiles
    where id = uid and is_banned = false
  );
$$;

create or replace function public.is_blocked_pair(a uuid, b uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.blocks
    where (blocker_id = a and blocked_id = b)
       or (blocker_id = b and blocked_id = a)
  );
$$;

-- Updates presence sparsely; polling every two seconds should not cause a write
-- on every request.
create or replace function public.touch_last_seen()
returns void
language sql
security definer
set search_path = public
as $$
  update public.profiles
  set last_seen = now()
  where id = auth.uid()
    and last_seen < now() - interval '20 seconds';
$$;

-- ---------------------------------------------------------------------------
-- Profile RPCs
-- ---------------------------------------------------------------------------
create or replace function public.my_profile()
returns table (
  display_name text,
  friend_code text,
  cefr_level public.cefr_level,
  rating numeric,
  rating_count integer,
  calls_completed integer,
  minutes_talked integer,
  avatar_sticker text,
  country text
)
language sql
stable
security definer
set search_path = public
as $$
  select
    p.display_name,
    p.friend_code,
    p.cefr_level,
    case when p.rating_count > 0
      then round(p.rating_sum::numeric / p.rating_count, 2)
      else 0::numeric
    end,
    p.rating_count,
    p.calls_completed,
    (p.seconds_talked / 60)::integer,
    p.avatar_sticker,
    p.country
  from public.profiles p
  where p.id = auth.uid();
$$;

create or replace function public.sync_profile(
  p_display_name text default null,
  p_level text default null,
  p_avatar text default null
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
  v_level public.cefr_level;
begin
  if me is null then raise exception 'Not authenticated'; end if;

  if p_display_name is not null and trim(p_display_name) <> '' then
    if char_length(trim(p_display_name)) < 2 or char_length(trim(p_display_name)) > 32 then
      raise exception 'Display names must be 2 to 32 characters';
    end if;
  end if;

  if p_avatar is not null and length(trim(p_avatar)) > 24 then
    raise exception 'Unknown avatar';
  end if;

  if p_level is null or trim(p_level) = '' then
    select cefr_level into v_level from public.profiles where id = me;
    v_level := coalesce(v_level, 'A1'::public.cefr_level);
  else
    if upper(trim(p_level)) not in ('A1','A2','B1','B2','C1','C2','TOP') then
      raise exception 'Unknown English level';
    end if;
    v_level := upper(trim(p_level))::public.cefr_level;
  end if;

  perform set_config('lingo.profile_write', 'on', true);

  insert into public.profiles (id, display_name, avatar_sticker, cefr_level, last_seen)
  values (
    me,
    coalesce(nullif(trim(p_display_name), ''), 'Learner'),
    coalesce(nullif(trim(p_avatar), ''), 'SPROUT'),
    v_level,
    now()
  )
  on conflict (id) do update
    set display_name = coalesce(nullif(trim(p_display_name), ''), profiles.display_name),
        avatar_sticker = coalesce(nullif(trim(p_avatar), ''), profiles.avatar_sticker),
        cefr_level = v_level,
        last_seen = now();
end;
$$;

create or replace function public.queue_size()
returns integer
language sql
stable
security definer
set search_path = public
as $$
  select count(*)::integer
  from public.call_queue q
  join public.profiles p on p.id = q.user_id
  where q.level = public.my_level()
    and q.expires_at > now()
    and p.is_banned = false;
$$;

-- Abandoned calls must not reserve a learner forever. The client ends its own
-- rings and connections with timeouts, but this is the backstop for a crash or
-- a phone that goes offline mid-call.
create or replace function public.expire_stale_calls()
returns void
language sql
security definer
set search_path = public
as $$
  update public.call_sessions
  set status = 'ended',
      end_reason = 'disconnected',
      ended_at = now()
  where (status = 'ringing' and created_at < now() - interval '2 minutes')
     or (status = 'active' and coalesce(started_at, created_at) < now() - interval '2 hours');
$$;

-- ---------------------------------------------------------------------------
-- Live call state
-- ---------------------------------------------------------------------------
-- The one shape every call RPC returns. Keeping it identical is what lets the
-- Android client use one decoder for polling, matching, ringing, and accepting.
create or replace function public.my_call_state()
returns table (
  session_id uuid,
  status text,
  call_kind text,
  is_caller boolean,
  partner_id uuid,
  partner_name text,
  partner_level public.cefr_level,
  partner_avatar text,
  partner_rating numeric,
  partner_rating_count integer,
  partner_country text,
  room_name text,
  started_at timestamptz,
  created_at timestamptz
)
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
  sess public.call_sessions%rowtype;
  other_id uuid;
begin
  if me is null then return; end if;

  perform public.expire_stale_calls();
  perform public.touch_last_seen();

  select s.* into sess
  from public.call_sessions s
  where (s.caller_id = me or s.callee_id = me)
    and s.status in ('ringing', 'active')
  order by s.created_at desc
  limit 1;

  if sess.id is null then
    return;
  end if;

  other_id := case when sess.caller_id = me then sess.callee_id else sess.caller_id end;

  return query
  select
    sess.id,
    sess.status,
    sess.call_kind,
    (sess.caller_id = me),
    p.id,
    p.display_name,
    p.cefr_level,
    p.avatar_sticker,
    case when p.rating_count > 0
      then round(p.rating_sum::numeric / p.rating_count, 2)
      else 0::numeric
    end,
    p.rating_count,
    p.country,
    sess.room_name,
    sess.started_at,
    sess.created_at
  from public.profiles p
  where p.id = other_id;
end;
$$;

create or replace function public.claim_match()
returns table (
  session_id uuid,
  status text,
  call_kind text,
  is_caller boolean,
  partner_id uuid,
  partner_name text,
  partner_level public.cefr_level,
  partner_avatar text,
  partner_rating numeric,
  partner_rating_count integer,
  partner_country text,
  room_name text,
  started_at timestamptz,
  created_at timestamptz
)
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
  my_lvl public.cefr_level;
  partner uuid;
begin
  if me is null then raise exception 'Not authenticated'; end if;

  -- If we already have a live call, return it instead of joining the queue.
  if exists (select 1 from public.my_call_state()) then
    return query select * from public.my_call_state();
    return;
  end if;

  select p.cefr_level into my_lvl
  from public.profiles p
  where p.id = me and p.is_banned = false;

  if my_lvl is null then
    raise exception 'An active Lingo profile is required';
  end if;

  -- One matcher at a time per level. This makes pairing atomic without a
  -- separate matching server.
  perform pg_advisory_xact_lock(hashtext('lingo-match-' || my_lvl::text));

  insert into public.call_queue (user_id, level, joined_at, last_seen, expires_at)
  values (me, my_lvl, now(), now(), now() + interval '2 minutes')
  on conflict (user_id) do update
    set level = excluded.level,
        last_seen = now(),
        expires_at = excluded.expires_at;

  select q.user_id into partner
  from public.call_queue q
  join public.profiles p on p.id = q.user_id
  where q.user_id <> me
    and q.level = my_lvl
    and q.expires_at > now()
    and p.is_banned = false
    and not public.is_blocked_pair(me, q.user_id)
    and not exists (
      select 1
      from public.call_sessions s
      where s.status in ('ringing', 'active')
        and (s.caller_id = q.user_id or s.callee_id = q.user_id)
    )
  order by q.joined_at asc
  limit 1
  for update of q skip locked;

  if partner is null then
    return;
  end if;

  insert into public.call_sessions (level, caller_id, callee_id, call_kind, status, started_at)
  values (my_lvl, me, partner, 'match', 'active', now());

  delete from public.call_queue where user_id in (me, partner);

  return query select * from public.my_call_state();
end;
$$;

create or replace function public.leave_call_queue()
returns void
language sql
security definer
set search_path = public
as $$
  delete from public.call_queue where user_id = auth.uid();
$$;

-- Friend calls are the intentional cross-level path. They ring first, so the
-- other learner must accept before either phone publishes a microphone track.
create or replace function public.start_friend_call(p_code text)
returns table (
  session_id uuid,
  status text,
  call_kind text,
  is_caller boolean,
  partner_id uuid,
  partner_name text,
  partner_level public.cefr_level,
  partner_avatar text,
  partner_rating numeric,
  partner_rating_count integer,
  partner_country text,
  room_name text,
  started_at timestamptz,
  created_at timestamptz
)
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
  my_lvl public.cefr_level;
  code text;
  target public.profiles%rowtype;
begin
  if me is null then raise exception 'Not authenticated'; end if;

  perform public.expire_stale_calls();

  select p.cefr_level into my_lvl
  from public.profiles p
  where p.id = me and p.is_banned = false;

  if my_lvl is null then raise exception 'An active Lingo profile is required'; end if;

  if exists (
    select 1 from public.call_sessions s
    where (s.caller_id = me or s.callee_id = me)
      and s.status in ('ringing', 'active')
  ) then
    raise exception 'You are already in a call';
  end if;

  code := upper(regexp_replace(trim(coalesce(p_code, '')), '[^A-Za-z0-9]', '', 'g'));
  if char_length(code) <> 6 then
    raise exception 'Friend codes have six characters';
  end if;

  select * into target from public.profiles p where p.friend_code = code limit 1;

  if target.id is null then raise exception 'No learner has that friend code'; end if;
  if target.id = me then raise exception 'That is your own friend code'; end if;
  if target.is_banned then raise exception 'This learner is unavailable'; end if;
  if public.is_blocked_pair(me, target.id) then raise exception 'This learner is blocked'; end if;
  if target.last_seen < now() - interval '2 minutes' then
    raise exception 'That learner is offline right now';
  end if;
  if exists (
    select 1 from public.call_sessions s
    where (s.caller_id = target.id or s.callee_id = target.id)
      and s.status in ('ringing', 'active')
  ) then
    raise exception 'That learner is on another call';
  end if;

  insert into public.call_sessions (level, caller_id, callee_id, call_kind, status)
  values (my_lvl, me, target.id, 'friend', 'ringing');

  delete from public.call_queue where user_id in (me, target.id);

  return query select * from public.my_call_state();
end;
$$;

create or replace function public.accept_call(p_session uuid)
returns table (
  session_id uuid,
  status text,
  call_kind text,
  is_caller boolean,
  partner_id uuid,
  partner_name text,
  partner_level public.cefr_level,
  partner_avatar text,
  partner_rating numeric,
  partner_rating_count integer,
  partner_country text,
  room_name text,
  started_at timestamptz,
  created_at timestamptz
)
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
begin
  if me is null then raise exception 'Not authenticated'; end if;

  update public.call_sessions
  set status = 'active',
      started_at = now()
  where id = p_session
    and callee_id = me
    and public.call_sessions.status = 'ringing';

  if not found then
    raise exception 'This call is no longer ringing';
  end if;

  return query select * from public.my_call_state();
end;
$$;

create or replace function public.decline_call(p_session uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.call_sessions
  set status = 'ended',
      end_reason = 'declined',
      ended_at = now()
  where id = p_session
    and callee_id = auth.uid()
    and status = 'ringing';
end;
$$;

create or replace function public.end_call(p_session uuid, p_reason text default 'ended')
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
  sess public.call_sessions%rowtype;
  duration_seconds integer;
begin
  if me is null then raise exception 'Not authenticated'; end if;
  if p_reason not in ('ended', 'skipped', 'disconnected', 'reported', 'declined', 'no_answer') then
    raise exception 'Unknown end reason';
  end if;

  select * into sess
  from public.call_sessions
  where id = p_session
    and (caller_id = me or callee_id = me)
  for update;

  if sess.id is null or sess.status = 'ended' then
    return;
  end if;

  duration_seconds := case
    when sess.status = 'active' and sess.started_at is not null
      then greatest(0, extract(epoch from (now() - sess.started_at))::integer)
    else 0
  end;

  update public.call_sessions
  set status = 'ended',
      end_reason = p_reason,
      ended_at = now()
  where id = p_session;

  if duration_seconds > 0 then
    perform set_config('lingo.profile_write', 'on', true);
    update public.profiles
    set calls_completed = calls_completed + 1,
        seconds_talked = seconds_talked + duration_seconds
    where id in (sess.caller_id, sess.callee_id);
  end if;

  delete from public.call_queue
  where user_id in (sess.caller_id, sess.callee_id);
end;
$$;

create or replace function public.rate_call(p_session uuid, p_stars integer)
returns table (
  average numeric,
  rating_count integer
)
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
  other uuid;
begin
  if me is null then raise exception 'Not authenticated'; end if;
  if p_stars < 1 or p_stars > 5 then raise exception 'Stars must be 1 to 5'; end if;

  select case when caller_id = me then callee_id else caller_id end
  into other
  from public.call_sessions
  where id = p_session
    and status = 'ended'
    and (caller_id = me or callee_id = me);

  if other is null then raise exception 'A finished call is required'; end if;

  insert into public.call_ratings (session_id, rater_id, rated_id, stars)
  values (p_session, me, other, p_stars)
  on conflict (session_id, rater_id) do nothing;

  return query
  select
    case when p.rating_count > 0
      then round(p.rating_sum::numeric / p.rating_count, 2)
      else 0::numeric
    end,
    p.rating_count
  from public.profiles p
  where p.id = other;
end;
$$;

create or replace function public.report_call(p_session uuid, p_reason text)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
  other uuid;
begin
  if me is null then raise exception 'Not authenticated'; end if;
  if p_reason not in ('bad_language', 'harassment', 'sexual', 'threat', 'other') then
    raise exception 'Unknown report reason';
  end if;

  select case when caller_id = me then callee_id else caller_id end
  into other
  from public.call_sessions
  where id = p_session
    and (caller_id = me or callee_id = me);

  if other is null then raise exception 'Call not found'; end if;

  insert into public.reports (session_id, reporter_id, reported_id, reason)
  values (p_session, me, other, p_reason);

  perform public.end_call(p_session, 'reported');
end;
$$;

create or replace function public.submit_weekly_score(p_score integer)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  me uuid := auth.uid();
  my_lvl public.cefr_level;
begin
  if me is null then raise exception 'Not authenticated'; end if;
  if p_score < 0 or p_score > 10 then raise exception 'Weekly score must be 0 to 10'; end if;

  select cefr_level into my_lvl
  from public.profiles
  where id = me and is_banned = false;

  if my_lvl is null then raise exception 'An active Lingo profile is required'; end if;

  -- One counted attempt per server week. A repeated submit is harmless and
  -- cannot improve a score.
  insert into public.weekly_scores (user_id, week_start, level, score)
  values (me, date_trunc('week', now())::date, my_lvl, p_score)
  on conflict (user_id, week_start) do nothing;
end;
$$;

create or replace function public.weekly_leaderboard()
returns table (
  user_id uuid,
  display_name text,
  country text,
  cefr_level public.cefr_level,
  score smallint,
  is_you boolean
)
language sql
stable
security definer
set search_path = public
as $$
  select
    s.user_id,
    p.display_name,
    p.country,
    s.level,
    s.score,
    (s.user_id = auth.uid())
  from public.weekly_scores s
  join public.profiles p on p.id = s.user_id
  where s.week_start = date_trunc('week', now())::date
    and s.level = public.my_level()
    and p.is_banned = false
  order by s.score desc, s.created_at asc
  limit 100;
$$;

-- ---------------------------------------------------------------------------
-- Row Level Security
-- ---------------------------------------------------------------------------
alter table public.profiles enable row level security;
alter table public.call_queue enable row level security;
alter table public.call_sessions enable row level security;
alter table public.call_ratings enable row level security;
alter table public.blocks enable row level security;
alter table public.reports enable row level security;
alter table public.weekly_scores enable row level security;

drop policy if exists "profiles are readable" on public.profiles;
drop policy if exists "profiles update own" on public.profiles;
drop policy if exists "profiles read own" on public.profiles;
create policy "profiles read own"
  on public.profiles for select to authenticated
  using (id = auth.uid());

create policy "profiles update own"
  on public.profiles for update to authenticated
  using (id = auth.uid())
  with check (id = auth.uid() and is_banned = false);

drop policy if exists "queue manage own" on public.call_queue;
drop policy if exists "queue read own" on public.call_queue;
create policy "queue read own"
  on public.call_queue for select to authenticated
  using (user_id = auth.uid());

drop policy if exists "calls read participants" on public.call_sessions;
create policy "calls read participants"
  on public.call_sessions for select to authenticated
  using (caller_id = auth.uid() or callee_id = auth.uid());

drop policy if exists "ratings read participants" on public.call_ratings;
create policy "ratings read participants"
  on public.call_ratings for select to authenticated
  using (
    exists (
      select 1 from public.call_sessions s
      where s.id = session_id
        and (s.caller_id = auth.uid() or s.callee_id = auth.uid())
    )
  );

drop policy if exists "blocks manage own" on public.blocks;
drop policy if exists "blocks read own" on public.blocks;
create policy "blocks read own"
  on public.blocks for select to authenticated
  using (blocker_id = auth.uid());

drop policy if exists "reports read own" on public.reports;
create policy "reports read own"
  on public.reports for select to authenticated
  using (reporter_id = auth.uid());

drop policy if exists "weekly board read cohort" on public.weekly_scores;
create policy "weekly board read cohort"
  on public.weekly_scores for select to authenticated
  using (level = public.my_level() or user_id = auth.uid());

-- ---------------------------------------------------------------------------
-- Function grants
-- ---------------------------------------------------------------------------
revoke all privileges on function public.expire_stale_calls() from public, anon;
revoke all privileges on function public.my_profile() from public, anon;
revoke all privileges on function public.sync_profile(text, text, text) from public, anon;
revoke all privileges on function public.queue_size() from public, anon;
revoke all privileges on function public.my_call_state() from public, anon;
revoke all privileges on function public.claim_match() from public, anon;
revoke all privileges on function public.leave_call_queue() from public, anon;
revoke all privileges on function public.start_friend_call(text) from public, anon;
revoke all privileges on function public.accept_call(uuid) from public, anon;
revoke all privileges on function public.decline_call(uuid) from public, anon;
revoke all privileges on function public.end_call(uuid, text) from public, anon;
revoke all privileges on function public.rate_call(uuid, integer) from public, anon;
revoke all privileges on function public.report_call(uuid, text) from public, anon;
revoke all privileges on function public.submit_weekly_score(integer) from public, anon;
revoke all privileges on function public.weekly_leaderboard() from public, anon;

grant execute on function public.my_profile() to authenticated;
grant execute on function public.sync_profile(text, text, text) to authenticated;
grant execute on function public.queue_size() to authenticated;
grant execute on function public.my_call_state() to authenticated;
grant execute on function public.claim_match() to authenticated;
grant execute on function public.leave_call_queue() to authenticated;
grant execute on function public.start_friend_call(text) to authenticated;
grant execute on function public.accept_call(uuid) to authenticated;
grant execute on function public.decline_call(uuid) to authenticated;
grant execute on function public.end_call(uuid, text) to authenticated;
grant execute on function public.rate_call(uuid, integer) to authenticated;
grant execute on function public.report_call(uuid, text) to authenticated;
grant execute on function public.submit_weekly_score(integer) to authenticated;
grant execute on function public.weekly_leaderboard() to authenticated;
