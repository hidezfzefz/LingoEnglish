# Lingo English

An Android app for building practical, confident English — from first hellos to
creative, high-stakes conversations.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/hidezfzefz/LingoEnglish)](https://github.com/hidezfzefz/LingoEnglish/releases/latest)
[![Content audit](https://github.com/hidezfzefz/LingoEnglish/actions/workflows/content-audit.yml/badge.svg)](https://github.com/hidezfzefz/LingoEnglish/actions/workflows/content-audit.yml)

## Download

Install the latest APK from the
[Releases page](https://github.com/hidezfzefz/LingoEnglish/releases/latest).
Android 8.0 (API 26) or newer is required.

```bash
adb install -r LingoEnglish-1.8.apk
```

Each release publishes `LingoEnglish-1.8.apk` and
`LingoEnglish-1.8.apk.sha256`. Verify the download before installing:

```bash
sha256sum -c LingoEnglish-1.8.apk.sha256
```

The v1.8 APK SHA-256 is
`0394199900e79bf2ebdf744c72725bf0334a8be73efde7235a31eac89d7b39b7`
(72,948,079 bytes).

The published APK is a sideload preview signed with the project's debug key.
Keep that in mind if you plan to publish on Google Play: use your own release
keystore and keep it out of version control.

## What's new in 1.8

- Opt-in daily practice reminders with an in-app time picker and clear
  guidance if Android blocks notifications.
- A conditional “recommended next” card on Home for weekly tests, level exams,
  and free practice.
- WCAG AA secondary text, labelled icon controls, 48 dp touch targets, larger
  TimePicker-ready settings, and a scrollable settings sheet.
- Duplicate-submit guards for sign-in, sign-up, password reset, and friend
  calls, plus rescheduling after reboot, clock changes, and app updates.
- Keystore-backed session storage and least-privilege Supabase access: the app
  reads through RLS and writes only through validated RPCs.
- Version 1.8 (versionCode 9) installs directly over 1.7 without clearing
  local progress.

## What is inside

- **A complete CEFR path** from A1 to TOP, with XP and active-day gates.
- **Opt-in practice reminders**: choose a daily time in Settings and get one
  local notification on this phone. Reminders skip days you have already
  practised, survive reboots and app updates, and never send your data to a
  server. Android 13+ asks for notification permission only when you turn them
  on.
- **A clearer next step on Home**: when a weekly test is due, a level exam is
  ready, or the path is complete, Home shows one recommendation card. It stays
  out of the way once the daily goal is met.
- **6 units, 20 lessons, 45 teaching slides, and 80 exercises** covering
  everyday English, travel, work, culture, and creative communication.
- **20 guided “Create” tasks** — one per lesson. Learners invent, explain,
  paraphrase, negotiate, pitch, and tell stories, with success criteria,
  optional sentence starters, a word target, and a model answer.
- **Serious level exams** with 12 questions balanced three per skill across
  grammar, vocabulary, reading, and use of English. Every attempt is drawn
  from a 14-question level bank, so retakes are not identical. A pass is
  10/12, the timer is 18 minutes, and results include a skill-by-skill
  breakdown and an answer review with explanations.
- **Weekly tests** with 10 questions drawn from the current and previous level.
  Question selection is deduplicated and balanced across all four skills.
- **Rotation-safe testing**: questions, answers, results, and the countdown are
  preserved when the phone rotates, so a configuration change never resets or
  voids an attempt. An active voice call also survives rotation.
- **Live voice calls** with same-level matching, friend-code cross-level calls,
  ratings, reporting, and blocking.
- **Email/password accounts** with password recovery and a mandatory greeting
  gate until the learner signs in.
- **Local-first progress**: XP, streaks, completed lessons, exam results, and
  weekly scores are stored on the device. Creative writing is saved as a draft
  while the learner types, and account tokens are excluded from Android
  backups.
- **Accessibility and reliability**: secondary text meets WCAG AA contrast,
  icon-only controls have labels and 48 dp touch targets, sign-in and call
  actions cannot double-submit, and every network action keeps its retry path.

## Creative learning

The newer lessons deliberately move beyond recognition:

| Lesson | Creative focus |
| --- | --- |
| Ask Better Questions | Open questions, follow-ups, and question ladders |
| Explain It Simply | One idea per sentence, analogy, and audience awareness |
| Paraphrase & Remix | Keeping meaning while changing register and emphasis |
| Invent & Pitch | Problem, idea, concrete benefit, and a small ask |
| Think on Your Feet | Buying time, structured thinking, and curious disagreement |

Every original lesson also ends with a short production task, so the learner
has to use the language rather than only choose an option.

## Build from source

### Requirements

- JDK 21
- Android SDK with compile SDK 37
- A Supabase project for accounts and live calls

### Configure the live backend

The app reads `LINGO_SUPABASE_URL` and `LINGO_SUPABASE_ANON_KEY` from the
environment first, then from `local.properties`.

```bash
./scripts/configure_live.sh https://YOUR-PROJECT.supabase.co YOUR_ANON_KEY
```

Never commit `local.properties`, `supabase/.env.local`, service-role keys,
LiveKit secrets, or signing keystores. Release builds fail fast if the public
Supabase URL and anon key are missing.

### Build

```bash
./gradlew :app:assembleRelease --no-daemon --max-workers=2 --console=plain
./gradlew --stop
```

The APK is written to:

```text
app/build/outputs/apk/release/app-release.apk
```

See [BUILDING.md](BUILDING.md) for the low-resource Gradle settings,
[supabase/README.md](supabase/README.md) for backend deployment, auth,
password recovery, LiveKit, and SMTP setup, and
[SECURITY-REVIEW.md](SECURITY-REVIEW.md) for the v1.8 security review.

## Release and rollback

1. Confirm `versionName` and `versionCode` in `app/build.gradle.kts`.
2. Run the unit tests and lint, then build the release APK using the command
   above.
3. Record `sha256sum app/build/outputs/apk/release/app-release.apk` and install
   the APK over the previous build with `adb install -r` to prove app data
   survives.
4. Commit the source, tag `vX.Y`, and publish a GitHub Release with the APK and
   its `.sha256` file.
5. Roll back by installing the previous release APK over the current one with
   `adb install -r`. Do not uninstall first: uninstalling erases local progress.
   A downgrade is only safe when the old version accepts the current local data;
   otherwise restore a device backup.

Release builds use the project debug key for v1.8 so existing installs can
update in place. That is an accepted sideload risk, documented as A-01 in
[SECURITY-REVIEW.md](SECURITY-REVIEW.md); use a dedicated release keystore
before any store distribution.

The v1.8 launch evidence is in [LAUNCH-CHECKLIST.md](LAUNCH-CHECKLIST.md),
[TEST-PLAN.md](TEST-PLAN.md), and [TEST-REPORT.md](TEST-REPORT.md). Data handling
is described in [PRIVACY.md](PRIVACY.md).

## Project layout

```text
app/src/main/java/com/lingo/app/
  data/       Content, lesson/test models, progress, calls, Supabase store
  ui/         Compose screens and design system
supabase/     SQL schema, edge function, auth and deployment scripts
scripts/      Local build configuration helpers
```

## Tests and verification

Run the headless content audit with:

```bash
./gradlew :app:testDebugUnitTest --tests com.lingo.app.ContentAuditTest --console=plain
./gradlew --stop
```

Run the reminder and Home-recommendation tests with:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.lingo.app.ReminderScheduleTest' --tests 'com.lingo.app.NextStepTest' --console=plain
./gradlew --stop
```

CI runs the same audit on every push and pull request. It verifies:

- unique lesson and exercise IDs;
- four options and a valid answer key for every scored question;
- 12 questions in every level exam, with exactly three per skill;
- 300 randomized weekly selections per level, with 10 unique,
  skill-balanced questions in every selection;
- shuffling never changes the correct answer;
- a word target, success criteria, and model answer for every creative task.

On-device smoke tests cover the login gate, password recovery, lessons,
creative writing, weekly tests, live calls, and data-preserving upgrades. The
full v1.8 matrix and evidence are in [TEST-PLAN.md](TEST-PLAN.md) and
[TEST-REPORT.md](TEST-REPORT.md).

## Privacy

Local progress, account data, call audio, retention, and deletion are described
in [PRIVACY.md](PRIVACY.md). The app bundles no ads, analytics, or third-party
tracking SDKs.

## Licence

Released under the [MIT License](LICENSE).
