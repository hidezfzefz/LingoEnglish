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
adb install -r LingoEnglish-1.5.apk
```

The published APK is a sideload preview signed with the project's debug key.
Keep that in mind if you plan to publish on Google Play: use your own release
keystore and keep it out of version control.

## What is inside

- **A complete CEFR path** from A1 to TOP, with XP and active-day gates.
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
- **Live voice calls** with same-level matching, friend-code cross-level calls,
  ratings, reporting, and blocking.
- **Email/password accounts** with password recovery and a mandatory greeting
  gate until the learner signs in.
- **Local-first progress**: XP, streaks, completed lessons, exam results, and
  weekly scores are stored on the device.

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

See [BUILDING.md](BUILDING.md) for the low-resource Gradle settings and
[supabase/README.md](supabase/README.md) for backend deployment, auth,
password recovery, LiveKit, and SMTP setup.

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

CI runs the same audit on every push and pull request. It verifies:

- unique lesson and exercise IDs;
- four options and a valid answer key for every scored question;
- 12 questions and all four skills in every level exam;
- 300 randomized weekly selections per level, with 10 unique,
  skill-balanced questions in every selection;
- shuffling never changes the correct answer;
- a word target, success criteria, and model answer for every creative task.

On-device smoke tests cover the login gate, password recovery, lessons,
creative writing, weekly tests, live calls, and data-preserving upgrades.

## Licence

Released under the [MIT License](LICENSE).
