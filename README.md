# Lingo English

An Android app for building practical, confident English — from first hellos to
creative, high-stakes conversations.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/hidezfzefz/LingoEnglish)](https://github.com/hidezfzefz/LingoEnglish/releases/latest)
[![Content audit](https://github.com/hidezfzefz/LingoEnglish/actions/workflows/content-audit.yml/badge.svg)](https://github.com/hidezfzefz/LingoEnglish/actions/workflows/content-audit.yml)

## Download

Install the latest APK from the
[Releases page](https://github.com/hidezfzefz/LingoEnglish/releases/latest).
The current build is [Lingo English 1.10.0](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.10.0).
Android 8.0 (API 26) or newer is required.

```bash
adb install -r LingoEnglish-1.10.0.apk
```

That release publishes `LingoEnglish-1.10.0.apk` and
`LingoEnglish-1.10.0.apk.sha256`. Verify the download before installing:

```bash
sha256sum -c LingoEnglish-1.10.0.apk.sha256
```

The v1.10.0 APK SHA-256 is
`9ea0ce05b867b07c4dc150bae8a7f0183e9a8883ede159eff77d058ddd20f1c1`
(73,046,449 bytes). The v1.9.1 release remains available for its original
notes.

The published APK is a sideload preview signed with the project's debug key.
Keep that in mind if you plan to publish on Google Play: use your own release
keystore and keep it out of version control.

## What's new in 1.10

- **Smart Review**: questions you miss in lessons, practice, or a weekly test
  join a local review queue. A wrong answer adds the question or resets its
  streak; one correct answer keeps it; two correct answers in a row clear it.
  Review sessions take up to five questions at a time, show the same question
  UI as the rest of the app, and earn quiz-style XP (4 per correct, plus a
  10 XP bonus at 80% or better) toward your daily goal, streak, and active days.
- **Review where you already are**: a Home card appears only when questions are
  due, Practice has a “Tricky questions” card with the live count, and a quiz
  result offers a review link after a miss. Review state and XP are stored on
  the device alongside the rest of your progress.
- **Optional Night appearance**: Settings now offers **Warm paper** (the
  original look and the default) and **Night**, a dark forest-and-lime
  appearance with the same type, stickers, and shapes. The choice is local,
  survives restarts, and applies immediately to every screen, including system
  bar icons. A learning reset does not change it.
- Version 1.10 (versionCode 12) installs directly over 1.9.1 without clearing
  the signed-in session, XP, streak, completed lessons, writing drafts,
  reminders, daily goal, exam results, weekly scores, or the review queue.

The full v1.10 release notes are in [RELEASE-NOTES-1.10.md](RELEASE-NOTES-1.10.md).

## What's new in 1.9.1

- Fixes a crash when finishing a lesson whose saved **Create** draft is very long.
  The writing field now stays at a readable height and scrolls internally instead of
  growing to fit every line. The full draft is kept and remains editable.
- Version 1.9.1 (versionCode 11) installs directly over 1.9 without clearing the
  signed-in session, XP, completed lessons, writing drafts, reminders, or daily goal.
- No other behavior changed. The v1.9 launch cutscene, lesson intros, creative chips,
  and all v1.8 features are unchanged.

The patch release notes are in [RELEASE-NOTES-1.9.1.md](RELEASE-NOTES-1.9.1.md).

## What's new in 1.9

- A branded 3.2-second launch sequence on the first cold start after install or
  update, then a 1.2-second logo-and-path sting on later cold starts. Tap
  anywhere or use **Skip intro**; reduced-motion mode shows a still frame and
  moves on quickly.
- A once-per-lesson intro for lessons you have not completed. It previews the
  real first step and offers a lesson-specific creative prompt with two or
  three tappable idea chips. The chips reveal short examples only — there is no
  typing and no new saved learner data.
- Replay rules that respect the learner: the full launch appears once per app
  version, the lesson intro is marked seen on entry so backing out never
  replays it, completed lessons open directly, and password-recovery links skip
  the launch sequence.
- Version 1.9 (versionCode 10) installs directly over 1.8 without clearing the
  signed-in session, XP, level, completed lessons, writing drafts, reminders,
  or daily goal. Every v1.8 feature is retained.

The full v1.9 release notes are in [RELEASE-NOTES-1.9.md](RELEASE-NOTES-1.9.md).

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
- **6 units, 20 lessons, 45 teaching slides, and 103 exercises** — 83
  auto-scorable questions plus 20 creative tasks — covering everyday English,
  travel, work, culture, and creative communication.
- **Smart Review** built from the 83 bundled auto-scorable questions: the
  queue is local, capped at five questions per session, and clears an item only
  after two consecutive correct answers.
- **Warm paper or Night**: a two-choice appearance setting with a
  WCAG-checked dark forest palette. Warm paper remains the default.
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
- **A warm welcome, once**: a local, silent launch cutscene introduces the
  forest-and-paper identity on the first start of each app version; later starts
  get a short sting. It runs before the sign-in gate, is always skippable, and
  respects the system animation setting.
- **A thoughtful lesson entry**: every not-yet-completed lesson opens with a
  one-screen preview of its first step and a lesson-specific creative question.
  Idea chips offer examples, not answers; they are local UI state and are never
  saved.
- **Accessibility and reliability**: secondary text meets WCAG AA contrast,
  icon-only controls have labels and 48 dp touch targets, launch and lesson
  intros expose TalkBack labels and skip actions, sign-in and call actions
  cannot double-submit, and every network action keeps its retry path.

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
has to use the language rather than only choose an option. v1.9 adds a
lesson-specific creative spark to the entry intro for all 20 lessons; the
prompts and chips are bundled with the app and never sent to a server.

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
[SECURITY-REVIEW.md](SECURITY-REVIEW.md) for the v1.10 security review.

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

Release builds use the project debug key for the 1.10 line so existing installs can
update in place. That is an accepted sideload risk, documented as A-01 in
[SECURITY-REVIEW.md](SECURITY-REVIEW.md); use a dedicated release keystore
before any store distribution.

The v1.10, v1.9.1, and v1.9 launch evidence is in [LAUNCH-CHECKLIST.md](LAUNCH-CHECKLIST.md),
[TEST-PLAN.md](TEST-PLAN.md), and [TEST-REPORT.md](TEST-REPORT.md). Data handling
is described in [PRIVACY.md](PRIVACY.md).

## Accounts and ownership

- **GitHub:** the owner’s `hidezfzefz` account owns the repository, tags, CI,
  and public release assets. This is the only distribution account.
- **Supabase:** the owner-managed project holds accounts, RLS policies, and the
  backend. Only the public `anon` key is compiled into the app.
- **LiveKit:** the owner-managed project supplies call rooms. Its API key and
  secret stay in `supabase/.env.local` and the edge function environment.
- **No other accounts:** there is no Play Store, hosting, analytics, ads,
  payment, or crash-reporting account in this release.

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

Run the v1.9 launch and lesson-intro tests with:

```bash
./gradlew :app:testDebugUnitTest --tests 'com.lingo.app.CutscenePolicyTest' --tests 'com.lingo.app.CutsceneContentTest' --console=plain
./gradlew --stop
```

CI runs the same audit on every push and pull request. It verifies:

- unique lesson and exercise IDs;
- four options and a valid answer key for every scored question;
- 12 questions in every level exam, with exactly three per skill;
- 300 randomized weekly selections per level, with 10 unique,
  skill-balanced questions in every selection;
- shuffling never changes the correct answer;
- a word target, success criteria, and model answer for every creative task;
- a launch-cutscene rule for fresh installs, updates, later cold starts, and
  recovery links;
- a creative spark with two or three usable chips for every lesson;
- the two-consecutive-correct review rule, queue codec bounds, and review XP
  formula;
- the Paper and Night text/background contrast pairs.

On-device smoke tests cover the login gate, password recovery, lessons,
creative writing, weekly tests, live calls, Smart Review, Night appearance, and
data-preserving upgrades. The full v1.10, v1.9.1, and v1.9 matrix and evidence
are in [TEST-PLAN.md](TEST-PLAN.md) and [TEST-REPORT.md](TEST-REPORT.md).

## Privacy

Local progress, account data, call audio, retention, and deletion are described
in [PRIVACY.md](PRIVACY.md). The app bundles no ads, analytics, or third-party
tracking SDKs.

## Licence

Released under the [MIT License](LICENSE).
