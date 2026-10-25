# Lingo English v1.8: build plan

## Architecture

- **One activity, Compose navigation state.** Keep `MainActivity` + `LingoApp` and the existing tab/overlay state. No Navigation library or module split: the app is small and the current state model already survives rotation.
- **Application-scoped stores.** `ProgressStore` remains the single source of truth for local progress and preferences; `CallStore` remains the source of truth for auth and calls. Both already outlive activity recreation.
- **New reminder package.** `data/ReminderSchedule.kt` holds pure `java.time` next-trigger logic. `reminders/PracticeReminder.kt` owns the notification channel, alarm scheduling, and cancellation. `reminders/ReminderReceiver.kt` shows the notification and schedules the next day. A boot/package-replaced receiver reschedules only when reminders are enabled.
- **No new dependency.** Use `AlarmManager.setAndAllowWhileIdle` for a single next reminder rather than WorkManager. Android may delay delivery in Doze, which is acceptable for a practice nudge; no exact-alarm permission is requested.
- **Persistence.** Add `reminder_enabled`, `reminder_hour`, and `reminder_minute` to the existing `lingo_progress` preferences. Defaults: off, 19:00. `ProgressStore.reset()` keeps the reminder preference (like the daily goal) or turns it off explicitly; decision: keep the daily goal, turn reminders off and cancel the alarm so a reset truly starts quiet.
- **Navigation.** No new screen. Settings gains a reminder section. A notification tap deep-links to the Practice tab through an intent extra consumed by `LingoApp` after the sign-in gate. Home gains one recommendation card above the existing daily plan.
- **Offline behaviour.** Lessons, progress, flashcards, and reminders remain fully local. Sign-in, password recovery, matchmaking, and weekly submission keep their existing network paths and show retryable errors. No offline cache is added in v1.8.
- **Permissions.** Add `POST_NOTIFICATIONS` (runtime on Android 13+) and `RECEIVE_BOOT_COMPLETED` (normal). The notification permission is requested only when the learner enables reminders. Existing microphone/Bluetooth permissions are unchanged. No exact-alarm, location, camera, or payment permission.

## Screens and state

| Surface | Change |
| --- | --- |
| Settings sheet | Daily reminder switch, chosen time, permission-denied guidance, link to system notification settings |
| Home | One “Recommended next” card: weekly test due → level exam ready → continue lesson → free practice |
| Notification | “A few minutes of English?” with the learner’s streak, tap opens Practice |
| Practice tab | Unchanged; receives the deep link |
| Everything else | Unchanged behaviour and layout |

## Build order

Each task is small enough to verify on its own. Do not start a task until the previous one builds.

1. **Baseline.** Confirm the clean tree at v1.7, run `:app:testDebugUnitTest` and `:app:lintDebug`, and record the current APK size and install state.
   *Verify:* tests and lint pass; `adb shell dumpsys package com.lingo.app` shows version 1.7 / code 8 on the phone.
2. **Reminder schedule logic.** Add `ReminderSchedule.nextTrigger(now, hour, minute, practicedToday)` returning the next `ZonedDateTime`, plus `notificationText(streak, xp)`.
   *Verify:* JVM unit tests cover later-today, already-past-today, practiced-today, midnight, and month/year boundaries.
3. **Reminder preferences.** Add enabled/hour/minute state and setters to `ProgressStore`; update `reset()` to disable reminders and cancel the alarm.
   *Verify:* compile; a small instrumentation-free test or manual `SharedPreferences` check; reset leaves reminders off.
4. **Scheduler and receivers.** Add the notification channel, `PracticeReminder.schedule/cancel`, `ReminderReceiver`, `ReminderRescheduleReceiver`, and manifest entries with `exported="false"`.
   *Verify:* `dumpsys alarm` lists one Lingo alarm after enabling; reboot/package-replaced reschedules; disabling removes it.
5. **Settings UI.** Add the switch, time row, Material 3 time picker, and permission flow (`RequestPermission`), including “blocked in system settings” recovery.
   *Verify:* enabling on Android 13+ shows the permission dialog; denying leaves the switch off with a clear message; allowing schedules the alarm; changing time reschedules.
6. **Notification deep link.** Add `EXTRA_OPEN_PRACTICE`, handle it in `MainActivity`/`LingoApp`, and route to the Practice tab only after the sign-in gate.
   *Verify:* tapping the notification opens Practice when signed in and the gate when signed out; after sign-in it lands on Practice.
7. **Home recommendation.** Add a pure `NextStep.recommend(...)` function and a compact card using it; hide the card when the daily goal is already complete.
   *Verify:* unit tests cover weekly due, exam ready, next lesson, path complete, and goal complete; UI shows exactly one primary action.
8. **Accessibility pass.** Audit every icon-only control for a content description, ensure 48 dp touch targets, add heading semantics to section titles, verify contrast, and check 200% font scale and landscape.
   *Verify:* `lintDebug` has no accessibility errors; TalkBack reads the Home, Settings, and Practice screens in a logical order; no clipped text at 200%.
9. **Reliability pass.** Find and fix duplicate-submit paths in sign-in, sign-up, password reset, call start/end, and weekly submission; ensure each failure has a retry path and no success state is shown on error.
   *Verify:* code review checklist; airplane-mode manual tests for sign-in and call start; repeated taps produce one request.
10. **Automated tests.** Keep the content audit, add the new JVM tests, and run `:app:testDebugUnitTest` plus `:app:lintDebug`.
    *Verify:* all tests pass; lint 0 errors; CI workflow updated if a new test class needs it.
11. **Docs and version.** Bump to 1.8 / versionCode 9, update README and BUILDING with reminders, permissions, and the upgrade note, and write release notes.
    *Verify:* README commands still work; no secret or signing key is added.
12. **Release candidate.** Build `:app:assembleRelease`, verify the APK signature and SHA-256, install over v1.7 on the physical phone.
    *Verify:* 147 XP, sign-in, streak, lessons, and install date survive; reminders default off; app version reads 1.8.
13. **Device QA.** Exercise the login gate, lesson, creative draft, weekly test, level exam, voice call, settings, reminder, rotation, and process-death paths on the emulator and phone.
    *Verify:* the test matrix in `PLAN.md` passes; no orphan QA data is left on Supabase.
14. **Security review handoff.** Prepare the manifest, data-flow, auth, secret, and dependency checklist for Convio stage 6; fix anything it finds before release.
    *Verify:* stage 6 “done when” list is complete.
15. **Ship.** Push, wait for CI, tag `v1.8`, publish the APK and checksum, and verify the release installs over v1.7.
    *Verify:* CI green, release assets present, README link resolves.

## Test matrix

| Area | Cases |
| --- | --- |
| Reminder logic | Later today, past today, practiced today, midnight, DST-safe local time, month/year boundary |
| Reminder device | Enable, deny permission, allow permission, change time, disable, reboot, app update, tap notification, goal already met |
| Home recommendation | Weekly test due, exam eligible, next lesson, path complete, goal complete |
| Existing learning | Lesson completion XP, creative draft save, weekly test once per week, exam pass/fail/retry |
| Accounts | Sign-up, sign-in, wrong password, password reset link, sign-out, gate on cold start |
| Calls | Same-level match, friend code, Skip, End, report, rating, block, rotation |
| Data safety | Install 1.8 over 1.7; XP, streak, completed lessons, drafts, and sign-in preserved |
| Accessibility | TalkBack order, 200% font, landscape, 48 dp targets, contrast |
| Security | Exported components, PendingIntent flags, no secrets in logs/APK, backup rules |

## Risks and mitigations

- **Alarm delivery is inexact.** Accept Doze delays; never promise an exact minute in copy. Provide a “practise now” path from the app.
- **Notification permission denial.** Keep reminders off, explain how to enable them in system settings, and never block learning.
- **Receiver security.** Use explicit intents, immutable PendingIntents, `exported="false"`, and no user data beyond the notification text.
- **Reset surprise.** Reset turns reminders off and cancels the alarm; the settings copy says so.
- **Data loss.** No schema migration removes keys; only additive preferences. Verify install-over-upgrade before release.

## Out of scope

iOS, cross-platform rewrite, new curriculum, pronunciation scoring, offline audio, payments, leaderboards, rebrand, Google Workspace migration.
