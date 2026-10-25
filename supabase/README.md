# Lingo Live — setup

Lingo 1.4 introduced **live, audio-only calls** in place of the old text chat. Supabase
owns accounts, same-level matching, friend codes, ratings, reports, blocks, and
the weekly leaderboard. LiveKit Cloud carries the voice.

There are **no simulated partners, no preview calls, no video, and no in-call
text** in this build. The only in-call extras are the six emoji reactions
(`👋 ❤️ 😂 😮 👍 🔥`) and the call controls.

## Architecture

| Piece | Responsibility |
| --- | --- |
| Supabase Auth | Email + password accounts, session restore, confirmation, password recovery, JWT |
| `calls_schema.sql` | Profiles, friend codes, queue, sessions, ratings, reports, blocks, weekly scores, RPCs, RLS |
| Supabase Edge Function `livekit-token` | Checks the caller is a participant in an active session, then signs a short-lived LiveKit room token |
| Supabase Edge Function `auth-callback` | Public post-confirmation page used as the Site URL in auth emails |
| LiveKit Cloud | Audio tracks and emoji reactions (reliable data messages) |
| Android app | Email auth UI, same-level queue polling, ringing/accept, LiveKit room, microphone, ratings, reports, weekly board |

The app never contains the LiveKit API secret or the Supabase service-role key.

## What you need

1. A **new, empty Supabase project** on the free plan.
2. A **new LiveKit Cloud project** on the free plan.
3. The Android SDK and the project in this repository.

> Use a fresh Supabase project. The preview call schema and the old chat schema
> are different; this file is written for a clean database.

## 1. Supabase project

Create the project at <https://supabase.com/dashboard>. Wait for the database to
finish provisioning.

From **Project Settings → API**, copy:

- Project URL, for example `https://abcdefgh.supabase.co`
- `anon` / publishable key (safe to ship in the APK)
- Project ref (`abcdefgh`)

Never put the `service_role` key in the Android app. The Edge Function receives
it from Supabase automatically.

### Email/password auth

In **Authentication → Providers → Email**:

- Enable Email.
- For QA, turn **Confirm email** off so test accounts can sign in immediately.
- The deployed Site URL is
  `https://YOUR-PROJECT.supabase.co/functions/v1/auth-callback`, a public
  plain-text “You’re verified” page. Supabase uses it in the confirmation email
  even though the Android app itself only signs in with email/password.
- Confirmation email delivery needs a **custom SMTP provider**. Supabase’s
  built-in service is not configured on this project; without custom SMTP,
  confirmation must stay off. Fill `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`,
  `SMTP_PASS`, `SMTP_ADMIN_EMAIL`, and `SMTP_SENDER_NAME` in
  `supabase/.env.local`, then run:

  ```bash
  ./supabase/configure_auth.sh
  ```

  The script sets the SMTP provider, points Site URL at `auth-callback`, adds
  the `lingo://reset-password` redirect used by the Android recovery screen, and
  re-enables confirmation. Test one real signup and one password reset end to
  end before inviting learners.

#### Google Workspace SMTP

Google requires **2-Step Verification** and a 16-character **App Password**;
the normal Google password will not work. For production:

1. Use a Google Workspace account with a verified custom domain.
2. Create or verify the sender mailbox, such as `auth@yourdomain.com`.
3. Enable 2-Step Verification for that user, then create an App Password under
   **Google Account → Security → App passwords**.
4. Run the setup script, entering the full Workspace sender address and the App
   Password when prompted:

   ```bash
   ./supabase/setup_google_smtp.sh
   ```

The script tests the Google SMTP login before changing Supabase, writes the
credentials to the mode-600 env file, and then enables confirmation. It uses
`smtp.gmail.com:587`, which is the correct endpoint for both Workspace and free
Gmail. No Android rebuild is required.

For automation, provide the sender and a mode-600 App Password file:

```bash
LINGO_GOOGLE_SENDER=auth@yourdomain.com \
LINGO_GOOGLE_APP_PASSWORD_FILE=/path/to/google-app-password \
./supabase/setup_google_smtp.sh
```

Never put the App Password in a command argument, source file, or chat message.
After switching, check the domain’s SPF, DKIM, and DMARC records and repeat one
signup plus one password reset. Free Gmail has low daily limits and is suitable
only for early testing; Workspace has higher limits, and a dedicated
transactional provider is preferable at larger production volumes.

The app signs up and signs in with email/password on the device. Email
confirmation uses the public `auth-callback` page. Password recovery uses the
Android deep link `lingo://reset-password`, which must be present in the Supabase
redirect allow-list; `configure_auth.sh` adds it automatically.

## 2. Apply the database schema

Use **one** of these routes.

### Route A — SQL editor (fastest)

1. Open the Supabase **SQL Editor**.
2. Paste the complete contents of [`calls_schema.sql`](calls_schema.sql).
3. Run it once.
4. Check **Database → Functions** for `claim_match`, `start_friend_call`,
   `accept_call`, `rate_call`, `report_call`, and the rest.

### Route B — Supabase CLI migration

Keep deployment values in the untracked `supabase/.env.local` file:

```bash
cp supabase/.env.local.example supabase/.env.local
# edit supabase/.env.local, then:
./supabase/deploy.sh
```

`deploy.sh` reads that file automatically, links the project, pushes the
migration, sets the three LiveKit secrets, and deploys both Edge Functions
(`livekit-token` and the public `auth-callback` page). If you used Route A, run
it with `SKIP_DB_PUSH=1`.

The same schema is mirrored in
[`migrations/20261021000000_live_calls.sql`](migrations/20261021000000_live_calls.sql).

`db push` uses the database password in `SUPABASE_DB_PASSWORD`; if that value is
empty, the CLI may ask for it interactively. Do not run both routes on the same
project.

The untracked file needs:

```dotenv
SUPABASE_URL=https://YOUR-PROJECT.supabase.co
SUPABASE_ANON_KEY=...
SUPABASE_PROJECT_REF=...
SUPABASE_ACCESS_TOKEN=sbp_...
SUPABASE_DB_PASSWORD=...
LIVEKIT_URL=wss://YOUR-PROJECT.livekit.cloud
LIVEKIT_API_KEY=...
LIVEKIT_API_SECRET=...

# Optional, only needed for confirmation emails:
SMTP_HOST=
SMTP_PORT=587
SMTP_USER=
SMTP_PASS=
SMTP_ADMIN_EMAIL=
SMTP_SENDER_NAME=Lingo
```

`SUPABASE_ACCESS_TOKEN` is a personal access token from
**Account → Access Tokens**. The service-role key is never needed locally.

## 3. LiveKit Cloud

Create a project at <https://cloud.livekit.io>. From the project settings copy:

- WebSocket URL, e.g. `wss://your-project.livekit.cloud`
- API key
- API secret

The URL and API key are needed by the Edge Function. The secret must **never**
be placed in `local.properties` or the APK.

Set the function secrets (replace the values):

```bash
npx supabase secrets set \
  LIVEKIT_URL=wss://your-project.livekit.cloud \
  LIVEKIT_API_KEY=... \
  LIVEKIT_API_SECRET=... \
  --project-ref "$SUPABASE_PROJECT_REF"
```

Deploy the function:

```bash
npx supabase functions deploy livekit-token --project-ref "$SUPABASE_PROJECT_REF"
```

Or paste [`functions/livekit-token/index.ts`](functions/livekit-token/index.ts)
into the Supabase dashboard’s Edge Functions editor, add the same three secrets,
and deploy with **Verify JWT** on.

## 4. Point the Android build at Supabase

The build reads `LINGO_SUPABASE_URL` and `LINGO_SUPABASE_ANON_KEY` from the
environment first, then from `local.properties`. The helper script preserves the
existing `sdk.dir` line and can read `SUPABASE_URL` / `SUPABASE_ANON_KEY` from
`supabase/.env.local`:

```bash
cd /path/to/LingoEnglish
./scripts/configure_live.sh
```

You can still pass them explicitly if you prefer:

```bash
./scripts/configure_live.sh \
  https://your-project.supabase.co \
  your-anon-publishable-key
```

Or add these two lines to `local.properties` yourself:

```properties
LINGO_SUPABASE_URL=https://your-project.supabase.co
LINGO_SUPABASE_ANON_KEY=eyJ...
```

The URL and anon key are compiled into `BuildConfig`. That is expected for a
Supabase client. The service-role key and LiveKit secret are not. Release
builds fail fast if either value is missing, so an offline build cannot be
shipped by accident.

## 5. Build and install

```bash
./gradlew :app:assembleRelease --no-daemon --max-workers=2 --console=plain
```

The APK is written to:

```text
app/build/outputs/apk/release/app-release.apk
```

Install over the existing app to preserve data:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

The release build is signed with the local debug key, matching the existing
installed builds.

## 6. Two-device QA checklist

Use two accounts. Both should be signed in on the Call tab.

- [ ] Wrong password shows an error and does not crash.
- [ ] Sign out and sign in restores the same profile and friend code.
- [ ] Two accounts at the same CEFR level enter the queue and are paired.
- [ ] A call across different levels cannot happen by random matching.
- [ ] A friend code can call across levels and rings on the other phone.
- [ ] Accept opens audio on both phones; decline ends the caller’s ring.
- [ ] Microphone permission is requested; denying it ends the call safely.
- [ ] Both microphones mute/unmute and the other side hears the change.
- [ ] All six emoji reactions travel over LiveKit data messages.
- [ ] Skip ends the current call and starts a new same-level search.
- [ ] End call shows the summary; every player gets a 1–5 star rating.
- [ ] Rating is stored and updates the player’s average.
- [ ] Report ends the call, records the reason, and blocks the player.
- [ ] A blocked player cannot be matched or called again.
- [ ] End session leaves the queue and closes the call screen.
- [ ] Session restore works after killing and reopening the app.
- [ ] A signed-in weekly test posts to the same-level weekly board.
- [ ] Lessons, Learning Lab, progress, CEFR exams, anti-cheat, and sticker UI
      still work.

## Security and behaviour notes

- `profiles` is readable only by its owner; call RPCs expose only the partner
  fields needed by the UI.
- `friend_code`, level, ratings, bans, and call counters cannot be edited
  directly by a signed-in client. A trigger protects those columns.
- Random matching is same-level and uses a PostgreSQL advisory lock per level.
- Friend calls may cross levels intentionally.
- Reports insert a block through a database trigger, so the block survives even
  if the client closes immediately afterwards.
- Ratings are one-per-session and one-per-rater, enforced by a unique key.
- Weekly scores are submitted after the local weekly test and only the first
  server-side submission per week counts.
- Incoming calls require the app process to be alive; there is no push
  notification or background call service. The call screen keeps the
  display awake while a call is open.

## Files

| File | Purpose |
| --- | --- |
| `calls_schema.sql` | Canonical live-call schema for the SQL editor |
| `migrations/20261021000000_live_calls.sql` | Same schema for `supabase db push` |
| `functions/livekit-token/index.ts` | Token Edge Function |
| `functions/auth-callback/index.ts` | Public post-confirmation page |
| `configure_auth.sh` | Configure SMTP, Site URL, and confirmation |
| `setup_google_smtp.sh` | Secure Google SMTP setup and login test |
| `config.toml` | CLI function verification settings |
| `.env.local.example` | Template for local deployment secrets |
| `deploy.sh` | Optional one-command CLI deployment |
| `legacy_chat_schema.sql` | Historical text-chat schema; not used since 1.4 |

The schema was validated against PostgreSQL 16 with stubbed Supabase auth
roles/claims. Hosted Supabase and LiveKit still require the two-account QA pass
above.
