# Test plan — Lingo English 1.8

Build under test: `1.8` (`versionCode 9`), release APK signed with the debug key.  
Platform: Android (the project decision is Android-only; iOS is out of scope).  
Test window: 2026-10-26.

## 1. Scope

This plan proves the v1.8 app works end to end on real Android runtimes, including the
new reminders and Home recommendation, the existing lesson/test/call features, and the
security changes from stage 6. It covers:

- every screen and tab, including login, Home, Learn, Practice, Call, Progress, Settings,
  all lesson types, weekly tests, level exams, and the call flow;
- small, medium, and large phone layouts in portrait and landscape;
- notification and microphone permission prompts, including denial and recovery;
- offline launch, offline navigation, and recovery after the network returns;
- accessibility semantics, touch targets, font scaling, and colour contrast;
- data preservation across an over-install and process restart;
- the failure paths users are most likely to hit.

Browser-only checks from the Convio testing skill (Playwright on Chromium/Firefox/WebKit,
axe, Lighthouse, linkinator) do not apply to a native Compose app. Their Android
equivalents are listed in section 4A. The only web surface is Supabase Auth and the
static auth-callback page; those were reviewed in `SECURITY-REVIEW.md` and are exercised
through the app.

## 2. Test environments

| ID | Device / runtime | Screen | Density | Notes |
| --- | --- | --- | --- | --- |
| E1 | Android 17 / API 37 emulator, 1080×2400 | 411×914 dp | 420 dpi | Primary full-journey device |
| E2 | Same emulator, 720×1280 | 360×640 dp | 320 dpi | Small-phone layout |
| E3 | Same emulator, 1440×3000 | 480×1000 dp | 480 dpi | Large-phone layout |
| E4 | Same emulator, portrait rotated to landscape | 914×411 dp | 420 dpi | Rotation survival |
| E5 | Physical Android phone, 1080×2340 | 360×780 dp | 384 dpi override | Only if the device is connected; display state must not be changed |
| E6 | Android 8 / API 26 | — | — | `minSdk` target; no API 26 system image available, so runtime coverage is API 37 only |

Font scale: 1.0 for the main pass, 1.3 and 2.0 for the accessibility pass.  
Theme: light only (the app intentionally has no dark mode).  
Locale: English. Network: normal, then offline, then restored.

The display settings of the physical phone must never be changed to run E5.

### Test accounts

- `qa-test-<random>@example.com` — fresh account for sign-up, recovery, and call tests.
- A second temporary account created through the Supabase API for friend-code and
  blocking tests. Both accounts are deleted after the run.
- Local progress is reset only on the emulator, never on the physical phone.

## 3. Journeys

Each journey lists the happy path, the edge cases to try, and what “correct” means.

### J1 — First launch and login gate

1. Clear app data and launch.
2. Confirm the greeting gate appears before any tab content.
3. Submit empty fields, an invalid email, and a wrong password.
4. Tap Sign in repeatedly while a request is in flight.

Edge cases: offline sign-in, unknown email, password with spaces, keyboard covering the
button, back button on the gate.  
Correct: no tab content is reachable signed out; errors are specific but do not reveal
whether an email exists; only one request runs; the button shows a busy state.

### J2 — Sign up

1. Create a fresh account with a display name.
2. Confirm Home opens and the profile/friend code is created.
3. Sign out and sign back in.

Edge cases: duplicate email, short password, missing display name, double-tap Create.  
Correct: one account is created, errors are readable, and the session survives a
restart after sign-in.

### J3 — Password recovery

1. Request a recovery link for the test account.
2. Open the `lingo://reset-password` link on the emulator.
3. Set a new password, sign in with it, then sign in with the old password.

Edge cases: unknown email, mismatched passwords, expired/reused link, back button while
the reset form is open.  
Correct: recovery never opens a normal session; the new password works once; the old
password is rejected; errors are actionable.

### J4 — Home

1. Check greeting, streak, XP, daily goal, today’s lesson, and the Begin action.
2. Check the conditional “next step” card states: weekly test due, level exam ready,
   free practice, and the hidden state when the hero already offers the next lesson.
3. Open Settings from the header; open the weekly check-in.

Edge cases: zero XP, completed daily goal, offline, process restart.  
Correct: only one recommendation appears; dates/streaks are correct; the card is not
duplicated by the hero.

### J5 — Lessons and creative tasks

1. Open a lesson from Learn.
2. Move through teaching slides, a multiple-choice exercise, a listening exercise, a
   word-tile exercise, and the creative task.
3. Submit a creative answer below and above the word target.
4. Finish the lesson and return to Home.

Edge cases: back mid-lesson, rotate mid-exercise, background/foreground, duplicate
Finish taps, no TTS voice installed.  
Correct: answers are graded correctly, the explanation appears, drafts survive
rotation, XP is awarded once, and the next lesson unlocks per the CEFR gates.

### J6 — Weekly test

1. Open the weekly check-in when due.
2. Answer all 10 questions, leave one unanswered, then submit.
3. Review the result and retake if allowed.

Edge cases: timer expiry, back/leave confirmation, rotation with 30 seconds left,
double-submit.  
Correct: 10 unique skill-balanced questions, the countdown survives rotation, an
unanswered question blocks submission with a clear message, and the score is saved once.

### J7 — Level exam and CEFR progression

1. Open the level path and a ready exam.
2. Answer 12 questions, review flagged answers, submit.
3. Check pass/fail feedback and the next CEFR level.
4. Confirm a locked level cannot be opened early.

Edge cases: fail at 9/12, pass at 10/12, timer expiry, rotate during the exam.  
Correct: 10/12 passes, results show the skill breakdown, XP/gates are consistent, and a
failed attempt can be retried according to the retake rule.

### J8 — Practice workouts

1. Complete the daily challenge.
2. Open Real Talk, Word Tiles, flashcards, word bank, roleplay, sentence tiles, and
   pronunciation.
3. Finish each and return to Practice.

Edge cases: wrong answers, empty word bank, no TTS, rotation, offline.  
Correct: answers and feedback are correct, progress is saved, and no workout traps the
user on a blank screen.

### J9 — Call setup, matching, and friend codes

1. Open Call and confirm the signed-in profile, level, stats, and friend code.
2. Start and cancel same-level matching.
3. Enter an invalid code, your own code, and a valid friend code.
4. Use the second test account to join the same-level queue and match.
5. Confirm ringing appears on both sides and accept/decline works.

Edge cases: no one in queue, callee offline, callee already in a call, blocked pair,
double-tap Find partner, network loss during search.  
Correct: one request per tap, clear empty/error states, no self-call, no cross-level
random match, and friend calls can cross levels. Two-phone audio is covered in J10 and
is limited by available hardware.

### J10 — Active call, safety, and history

1. Join a call, grant the microphone permission, and check mute, speaker, reactions,
   timer, and End.
2. Try Skip, End, report, rating, and block.
3. Open call history after the call.

Edge cases: deny microphone, revoke it mid-call, receive a call while on another tab,
rotate during ringing and during a call, network drop, second call while busy.  
Correct: the permission prompt is contextual; denial explains what is missing; controls
do not double-submit; the summary, rating, report, and block complete; history is
accurate; rotation does not drop the call. Full audio quality between two real phones
is not available in this environment and remains a physical-device check.

### J11 — Progress, stickers, and weekly board

1. Open Progress and check XP, level, lessons, words, accuracy, streak, and stickers.
2. Scroll to the weekly leaderboard and refresh.
3. Compare the values with Home and the level path.

Edge cases: zero state, offline leaderboard, only one learner in the cohort.  
Correct: totals match, the caller’s row is identifiable, no other learner’s private
data is exposed, and offline shows a retry rather than a crash.

### J12 — Settings, reminders, and notifications

1. Change the daily goal and confirm it persists.
2. Enable reminders, grant notifications, choose a time, and confirm the scheduled
   notification.
3. Deny notifications and check the blocked guidance and system-settings link.
4. Disable reminders and confirm the alarm is cancelled.
5. Open Reset progress, cancel, then confirm the copy and destructive action.
6. Check the version label.

Edge cases: permission denied twice, notifications disabled in system settings, time
picker at 00:00/23:59, reboot after enabling, app update after enabling.  
Correct: reminders are off by default at 19:00; enabling schedules one inexact alarm;
practising that day suppresses the reminder; denial keeps reminders off with guidance;
Reset clears local progress but keeps the account and friend code.

### J13 — Session and account lifecycle

1. Sign in, force-stop, relaunch, and confirm the session is restored.
2. Rotate, background for several minutes, and return.
3. Sign out and confirm the gate returns.
4. Sign in again and confirm the profile/friend code is unchanged.

Edge cases: corrupted encrypted prefs, Keystore unavailable (code fallback), expired
refresh token, offline relaunch.  
Correct: tokens stay in Keystore-backed storage, the app never falls back to plaintext,
and sign-out clears the session.

### J14 — Offline and recovery

1. Launch offline with an existing session.
2. Navigate Home, Learn, Practice, Progress.
3. Attempt sign-in, leaderboard refresh, matching, and a friend call.
4. Restore the network and retry.

Edge cases: airplane mode, DNS failure, slow network, network loss mid-request.  
Correct: local lessons and progress still work; network surfaces show a retry; no
spinner is left forever; retry succeeds after the network returns.

### J15 — Permission prompts

1. Fresh install: enable reminders and handle the notification prompt (allow and deny).
2. Fresh call: handle the microphone prompt (allow and deny).
3. Revoke microphone permission in system settings and return to the call.

Edge cases: “don’t ask again”, notification channel disabled, Bluetooth permission on
a call with a headset.  
Correct: prompts appear only in context; denial is explained; the app does not crash or
loop the prompt.

### J16 — Data preservation and upgrade

1. Record XP, streak, completed lessons, and a writing draft.
2. Install the release APK over the previous build with `adb install -r`.
3. Launch and compare all values.
4. Force-stop, reboot the emulator, and compare again.

Correct: local progress and the encrypted session survive, the legacy session key is
gone, and no re-login is required after an over-install.

### J17 — Accessibility

- TalkBack labels for icon-only controls (settings, speaker, back, call controls).
- Every tappable target at least 48 dp.
- Headings exposed as headings; switch roles for reminder toggles.
- Text contrast at WCAG AA for normal and secondary text.
- Font scale 1.3 and 2.0: no clipped labels, no overlapping controls, settings sheet
  scrolls, and all actions remain reachable.
- Reduced motion: transitions do not block input; no essential information is conveyed
  by colour alone.

### J18 — Responsive layouts

- 360×640, 411×914, and 480×1000 dp in portrait.
- 411×914 rotated to landscape.
- No horizontal scrolling, no clipped buttons, no text running off cards, and the
  bottom bar remains usable.

### J19 — Security smoke checks

- Signed-out users cannot reach tabs through the launcher or a notification.
- Password-recovery tokens are not persisted or logged.
- No service-role, LiveKit, or SMTP secret is visible in the APK.
- Direct table writes are rejected; RPCs work (covered in `SECURITY-REVIEW.md`).

## 4. How each layer is tested

| Layer | Tool / method |
| --- | --- |
| Unit logic | Gradle `:app:testDebugUnitTest` — content audit, reminder schedule, next-step selection |
| API / RLS | Supabase REST integration test with a temporary user; direct writes must return 403/42501 |
| End-to-end UI | Release APK on the emulator, driven with `adb`/UIAutomator; screenshots at each journey |
| Accessibility | Android lint accessibility checks, UIAutomator semantics, manual TalkBack/font-scale pass |
| Performance | `adb shell am start -W` cold start, frame statistics, APK size; no web Core Web Vitals |
| Responsive | `wm size`/`wm density` overrides on the emulator, restored afterwards |
| Offline | Emulator airplane mode / Wi-Fi off, then restore |
| Data preservation | `adb install -r` over the prior build, then compare on-device values |
| Security | Gitleaks, OSV, Semgrep, APK scan, live ACL query (see `SECURITY-REVIEW.md`) |

### 4A. Browser-tool substitutions

| Convio web check | Android equivalent |
| --- | --- |
| Playwright Chromium/Firefox/WebKit | UIAutomator journeys on the Android runtime; no browser engine is involved |
| axe-core | Android lint accessibility checks + UIAutomator semantics + TalkBack |
| Lighthouse / Core Web Vitals | Cold-start time, frame stats, APK size, memory sanity |
| Linkinator | In-app link/route reachability (all routes are compiled, not URLs) |
| 360–1920 px widths | 360, 411, and 480 dp phone widths; tablets are out of scope for this phone app |

## 5. Failure cases to force

- invalid email, wrong password, duplicate sign-up, short password;
- empty answer, wrong answer, unanswered weekly question;
- double-tap Sign in, Create, Begin, Submit, Finish, Find partner, End call;
- back/leave during a lesson, test, exam, ringing call, and active call;
- rotate during an exercise, a timed test, and a call;
- notification denied, microphone denied, microphone revoked mid-call;
- offline launch, offline leaderboard, offline matching, network restored;
- wrong friend code, own friend code, offline friend, busy friend, blocked pair;
- expired recovery link and reused recovery link;
- process death/relaunch with an active session;
- upgrade over the previous build without clearing data.

## 6. Entry and exit criteria

Entry: release APK builds, unit tests and lint pass, security stage is closed, test
accounts are available, and the emulator is healthy.

Exit: every journey above passes on E1 and the layout pass passes on E2/E3/E4; no serious
accessibility violation; no unresolved crash, ANR, data loss, or security regression;
`TEST-REPORT.md` records the results, failures fixed, and anything still open.

## 7. Out of scope and known limits

- iOS: the project is Android-only.
- Two-phone live-audio quality: requires two physical phones and a quiet audio path.
- Fully signed-out testing on the physical phone: requires the device to be connected.
- API 26 runtime: no API 26 system image is installed; `minSdk` is verified by build and
  lint, not by running on Android 8.
- Payments, file uploads, web admin, and browser performance: not part of this app.
