-- Lingo 1.8 security hardening
--
-- This migration tightens the public API surface discovered during the v1.8
-- review. It does not change call behaviour: every write still goes through a
-- SECURITY DEFINER RPC, and the app only needs SELECT on the tables below.

-- ---------------------------------------------------------------------------
-- Least-privilege table grants
-- ---------------------------------------------------------------------------
-- Supabase's default grants give anon and authenticated far more than this app
-- uses. RLS already blocks most paths, but reducing grants makes a future
-- policy mistake less dangerous. Anonymous callers need no table access.
revoke all on table public.profiles from anon;
revoke all on table public.call_queue from anon;
revoke all on table public.call_sessions from anon;
revoke all on table public.call_ratings from anon;
revoke all on table public.blocks from anon;
revoke all on table public.reports from anon;
revoke all on table public.weekly_scores from anon;

-- Signed-in clients read through RLS and write through RPCs only.
revoke all on table public.profiles from authenticated;
revoke all on table public.call_queue from authenticated;
revoke all on table public.call_sessions from authenticated;
revoke all on table public.call_ratings from authenticated;
revoke all on table public.blocks from authenticated;
revoke all on table public.reports from authenticated;
revoke all on table public.weekly_scores from authenticated;

grant select on table public.profiles to authenticated;
grant select on table public.call_queue to authenticated;
grant select on table public.call_sessions to authenticated;
grant select on table public.call_ratings to authenticated;
grant select on table public.blocks to authenticated;
grant select on table public.reports to authenticated;
grant select on table public.weekly_scores to authenticated;

-- The old direct-update policy let a client write last_seen and other display
-- fields without going through sync_profile. The app has always used the RPC,
-- so dropping the policy removes the unused write path entirely.
drop policy if exists "profiles update own" on public.profiles;

-- ---------------------------------------------------------------------------
-- Helper functions are internal, not PostgREST endpoints
-- ---------------------------------------------------------------------------
-- is_active_user() and is_blocked_pair() disclose ban/block relationships by
-- UUID. touch_last_seen() and expire_stale_calls() are only called by other
-- SECURITY DEFINER functions. Revoke them from every client role; the owner
-- can still call them internally.
revoke all on function public.is_active_user(uuid) from public, anon, authenticated;
revoke all on function public.is_blocked_pair(uuid, uuid) from public, anon, authenticated;
revoke all on function public.touch_last_seen() from public, anon, authenticated;
revoke all on function public.expire_stale_calls() from public, anon, authenticated;

-- my_level() is used by the weekly_scores RLS policy, so authenticated must
-- retain EXECUTE. It only reads the caller's own level.
revoke all on function public.my_level() from public, anon;
grant execute on function public.my_level() to authenticated;

-- ---------------------------------------------------------------------------
-- Profile data constraints
-- ---------------------------------------------------------------------------
-- A signup can supply avatar_sticker in user metadata. Keep it to the small
-- sticker key shape and bound the optional country string so neither field can
-- be used to store arbitrary blobs.
alter table public.profiles drop constraint if exists avatar_sticker_shape;
alter table public.profiles
  add constraint avatar_sticker_shape
  check (avatar_sticker ~ '^[A-Z_]{1,24}$') not valid;
alter table public.profiles validate constraint avatar_sticker_shape;

alter table public.profiles drop constraint if exists country_length;
alter table public.profiles
  add constraint country_length
  check (country is null or char_length(country) <= 56) not valid;
alter table public.profiles validate constraint country_length;

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
    case
      when upper(trim(coalesce(new.raw_user_meta_data ->> 'avatar_sticker', '')))
        ~ '^[A-Z_]{1,24}$'
        then upper(trim(new.raw_user_meta_data ->> 'avatar_sticker'))
      else 'SPROUT'
    end
  )
  on conflict (id) do nothing;
  return new;
end;
$$;
