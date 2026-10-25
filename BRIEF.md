# Lingo English v1.8: brief

**What it is:** An Android app that takes learners from A1 to confident, practical English through short lessons, creative production tasks, level exams, weekly tests, and live voice calls with matched learners.

**For:** Existing Lingo learners on Android 8.0+ who want a steady daily habit, and new learners who need a clear next step without a placement interview. The first thing they do is sign in, then continue the next lesson on the path.

**Success:** A v1.8 APK on GitHub Releases with every v1.7 feature intact, no loss of local progress, a green content audit and CI run, a completed security review, and measurable polish: reminders that bring learners back without nagging, a clearer next action on Home, and an accessibility pass.

## Scope (v1.8)

- **Preserve the full v1.7 feature set:** CEFR path, lessons, creative writing, XP, streaks, stickers, level exams with review, weekly tests, accounts and password recovery, same-level matching, friend-code calls, ratings, reporting, blocking, rotation-safe sessions, and local-first progress.
- **Opt-in practice reminders:** a local daily notification at a learner-chosen time, scheduled on-device. No server, no email, no new personal data. Skip the reminder when the daily goal is already met or the learner has practised today. Reschedule after reboot and app update. Ask for Android 13+ notification permission only when the learner turns reminders on.
- **A clearer next step on Home:** one prominent recommendation derived from existing local state (continue lesson, weekly test due, level exam ready, or practise), with the streak and daily-goal progress kept visible.
- **Accessibility and polish:** content descriptions for icon-only controls, at least 48 dp touch targets, font-scale and landscape checks, consistent focus states, and reduced-motion-safe transitions.
- **Reliability:** explicit offline/network error states with retry for sign-in, password recovery, matchmaking, and weekly submission; guard against duplicate submits; keep saved state across rotation and process death.
- **Release hygiene:** version 1.8 (versionCode 9), release notes, updated README, APK checksum, and a data-preserving upgrade test over v1.7.

**Not in v1.8:** iOS or cross-platform rewrite, new curriculum units, pronunciation scoring, offline audio downloads, payments, leaderboards, Google Workspace migration, or a visual rebrand.

## Design

- **Direction:** keep Lingo's existing warm paper, forest, lime, and hand-drawn sticker identity. Refine spacing, type hierarchy, contrast, touch targets, and focus states rather than redesigning.
- Light theme only, matching the current app. Support dynamic font scaling and landscape without clipping.
- Accessibility target: WCAG 2.2 AA where applicable to a native Android app.

## Stack

- **App:** Kotlin + Jetpack Compose + Material 3, min SDK 26, target/compile SDK 37. No rewrite; this keeps local progress and the current APK update path.
- **Local state:** application-scoped `ProgressStore` and `CallStore`, SharedPreferences, saveable Compose state; reminders use `AlarmManager` + a `BroadcastReceiver` (no WorkManager dependency).
- **Backend:** Supabase for email/password auth, profiles, matchmaking, weekly scores, and password recovery. LiveKit for voice calls. Existing secrets stay only in `supabase/.env.local` and `local.properties`.
- **Delivery:** GitHub Actions content audit and Android Lint, GitHub Releases with a debug-signed sideload APK. MIT licence remains.

## Content and integrations

- No new lesson content in this cycle. The existing 6 units, 20 lessons, 80 exercises, 20 creative tasks, exam banks, and weekly selection remain the source of truth.
- Local Android notifications only. No analytics, ads, crash SDK, or new third-party service.

## Constraints and risks

- Must not clear or migrate away existing local data; upgrades install over v1.7.
- Notification exact-alarm restrictions on Android 12+ mean reminders should use an inexact daily alarm or `setAndAllowWhileIdle`, and degrade gracefully if permission is denied.
- Live-call QA still needs two accounts; the physical phone remains signed in as the real user.
- Gradle builds must stay low-resource (`--no-daemon --max-workers=2`) and stop daemons afterwards.
- Deployment secrets must never be printed, committed, or placed in the APK beyond the public Supabase URL and anon key.

## Assumptions (please check)

- "It" means the existing Lingo English Android app, not a new product.
- Android-only remains the right call for this cycle; iOS is deferred.
- The learner wants gentle, opt-in reminders rather than default-on notifications.
- Keeping the current visual identity is preferable to a rebrand.
- A debug-signed APK is still acceptable for sideloading; Play Store signing remains out of scope.
- The user's "continue" after dismissing the discovery questions is approval to proceed with these defaults.
