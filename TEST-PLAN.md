# Test plan — Lingo English 1.10

Build under test: `1.10.0` (`versionCode 12`), release APK signed with the debug key.  
Platform: Android (the project decision is Android-only; iOS is out of scope).  
Test window: 2026-11-05 to 2026-11-06.

## 1. Scope

This plan proves the v1.10 app works end to end on real Android runtimes, including the
new local Smart Review queue and optional Night appearance, the preserved v1.9 launch
cutscene and once-per-lesson intro, the preserved v1.8 features, and the security posture
of the release APK. It covers:

- every screen and tab, including login, Home, Learn, Practice, Call, Progress, Settings,
  all lesson types, weekly tests, level exams, and the call flow;
- Smart Review: the Home and Practice cards, the quiz-result link, a five-item session,
  two-consecutive-correct clearing, a later miss restarting the item, review XP and the
  80% bonus, empty states, queue persistence, and **Review again**;
- the optional Night appearance: the Settings selector, immediate re-theming, persistence
  across force-stop and reset, system-bar icons, and contrast in both palettes;
- the full launch cutscene on first start after install/update, the short sting on later
  cold starts, tap/Skip escape, recovery-link bypass, and reduced-motion behaviour;
- the once-per-lesson intro, its first-step preview, creative chips, Begin routing,
  back-out/reopen replay rules, and completed-lesson direct entry;
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
| E1 | Android 17 / API 37 emulator, 1080×2400 | 411×914 dp | 420 dpi | Earlier v1.9 full-journey and accessibility pass |
| E2 | Same emulator, 720×1280 | 360×640 dp | 320 dpi | Small-phone layout (earlier pass) |
| E3 | Same emulator, 1440×3000 | 480×1000 dp | 480 dpi | Large-phone layout (earlier pass) |
| E4 | Same emulator, portrait rotated to landscape | 914×411 dp | 420 dpi | Rotation survival (earlier pass) |
| E5 | Physical Samsung SM-A546E, Android 16 / API 36, 1080×2340 | 360×780 dp | 384 dpi override | Primary v1.10 device; display and font settings must not be changed |
| E6 | Android 8 / API 26 | — | — | `minSdk` target; no API 26 system image available, so runtime coverage is API 36/37 only |

Font scale: 1.0 on the physical phone; 1.3 and 2.0 on the emulator for the accessibility
pass. The phone’s font scale, display size, density, and rotation are not changed.  
Theme: Warm paper by default, with the optional Night appearance walked in Settings.  
Locale: English. Network: normal, then offline, then restored.

The physical phone is the v1.10 QA device. The emulator is stopped and is not used for the
remaining device checks; its earlier v1.9 results are retained only for the settings that
cannot be changed on the owner’s phone (font scale, reduced motion, TalkBack).

### Test accounts

- `qa-test-<random>@example.com` — fresh account for sign-up, recovery, and call tests.
- A second temporary account created through the Supabase API for friend-code and
  blocking tests. Both accounts are deleted after the run.
- Local progress is reset only after a pre-test backup, and the backup is restored
  byte-for-byte on the physical phone. The owner’s live data is never left cleared.

## 3. Journeys

Each journey lists the happy path, the edge cases to try, and what “correct” means.

### J1 — First launch, cutscene, and login gate

1. Clear app data and launch.
2. Confirm the full branded cutscene appears before the sign-in gate, then reaches the
   gate in under 5 seconds or ends immediately on a tap / **Skip intro**.
3. Confirm the greeting gate appears before any tab content.
4. Submit empty fields, an invalid email, and a wrong password.
5. Tap Sign in repeatedly while a request is in flight.
6. Force-stop and relaunch: confirm the short sting, not the full sequence, and that the
   launch marker is already stored.

Edge cases: offline launch, unknown email, password with spaces, keyboard covering the
button, back button on the gate, recovery link while the cutscene is playing, system
animations disabled.  
Correct: no tab content is reachable signed out; the full sequence appears once per app
version; later cold starts get the sting; tap/Skip always escapes; a recovery link skips
the launch and does not mark the version seen; errors are specific but do not reveal
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
3. Check the Smart Review card when items are queued: count, estimated time, and start.
4. Open Settings from the header; open the weekly check-in.

Edge cases: zero XP, completed daily goal, empty review queue, offline, process restart,
Night appearance.  
Correct: only one recommendation appears; dates/streaks are correct; the card is not
duplicated by the hero; the review count matches the stored queue and opens the same
five-item session as Practice.

### J5 — Lessons, entry intro, and creative tasks

1. Open a not-yet-completed lesson from Learn.
2. Confirm the once-per-lesson intro shows the lesson title, the three-step path, a
   preview of the real first step, and a lesson-specific creative prompt with two or
   three idea chips.
3. Tap a chip and confirm it reveals a short example; tap it again to collapse. Confirm
   nothing is typed or saved.
4. Tap **Begin first step** and confirm the real lesson opens at step 1 with the same
   first-step title shown in the preview, within 5 seconds.
5. Back out of a different lesson intro, reopen that lesson, and confirm the intro does
   not replay. Open a completed lesson and confirm it goes straight to the lesson.
6. Move through teaching slides, a multiple-choice exercise, a listening exercise, a
   word-tile exercise, and the creative task.
7. Submit a creative answer below and above the word target.
8. Finish the lesson and return to Home.

Edge cases: back mid-lesson, back from the intro, reopen after process death, rotate
mid-exercise, background/foreground, duplicate Finish taps, no TTS voice installed.  
Correct: the intro appears once for each not-yet-completed lesson, is marked seen on
entry, and never replays after backing out; completed lessons skip it; Begin opens the
real first step in one tap; chips are local UI state; answers are graded correctly, the
explanation appears, drafts survive rotation, XP is awarded once, and the next lesson
unlocks per the CEFR gates.

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
3. Open Smart Review from Practice in both the queued and empty states.
4. Finish each and return to Practice.

Edge cases: wrong answers, empty word bank, empty review queue, no TTS, rotation,
offline.  
Correct: answers and feedback are correct, progress is saved, and no workout traps the
user on a blank screen; the review card says how many items are waiting and the empty
state offers no dead action.

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
5. Open Appearance, switch to Night, confirm the whole app re-themes, force-stop,
   relaunch, and confirm Night is still selected; switch back to Warm paper.
6. While Night is selected, open Reset progress, cancel, then confirm the copy and
   destructive action; confirm the reset keeps Night selected.
7. Check the version label.

Edge cases: permission denied twice, notifications disabled in system settings, time
picker at 00:00/23:59, reboot after enabling, app update after enabling, unknown stored
theme value, reset while Night is selected.  
Correct: reminders are off by default at 19:00; enabling schedules one inexact alarm;
practising that day suppresses the reminder; denial keeps reminders off with guidance;
Reset clears local progress but keeps the account, friend code, daily goal, launch marker,
and appearance; Warm paper is the default for a fresh install.

### J13 — Session and account lifecycle

1. Sign in, force-stop, relaunch, and confirm the session is restored.
2. Rotate, background for several minutes, and return.
3. Sign out and confirm the gate returns.
4. Sign in again and confirm the profile/friend code is unchanged.
5. Queue review items, force-stop, relaunch, and confirm the Home and Practice counts
   still match.

Edge cases: corrupted encrypted prefs, Keystore unavailable (code fallback), expired
refresh token, offline relaunch, queued review items across process death.  
Correct: tokens stay in Keystore-backed storage, the app never falls back to plaintext,
sign-out clears the session, and the local review queue survives restart.

### J14 — Offline and recovery

1. Launch offline with an existing session; confirm the sting/full cutscene still
   completes without a network and reaches Home in under 5 seconds.
2. Navigate Home, Learn, Practice, Progress.
3. Open a lesson intro offline; confirm the first-step preview and creative chips still
   work because they are bundled locally.
4. Attempt sign-in, leaderboard refresh, matching, and a friend call.
5. Restore the network and retry.

Edge cases: airplane mode, DNS failure, slow network, network loss mid-request.  
Correct: the launch sequence is local and never waits for a network; local lessons,
intros, and progress still work; network surfaces show a retry; no spinner is left
forever; retry succeeds after the network returns.

### J15 — Permission prompts

1. Fresh install: enable reminders and handle the notification prompt (allow and deny).
2. Fresh call: handle the microphone prompt (allow and deny).
3. Revoke microphone permission in system settings and return to the call.

Edge cases: “don’t ask again”, notification channel disabled, Bluetooth permission on
a call with a headset.  
Correct: prompts appear only in context; denial is explained; the app does not crash or
loop the prompt.

### J16 — Data preservation and upgrade

1. Record XP, streak, completed lessons, writing draft, reminder, daily goal, queued
   review items, and the selected appearance.
2. Install the v1.10 release APK over the v1.9 build with `adb install -r`.
3. Launch and confirm the full cutscene appears once for the new version, then compare
   all values.
4. Force-stop and relaunch; confirm the sting appears and the values are unchanged.
5. Confirm no test-only intro markers remain if the phone was used for QA.

Correct: local progress, the encrypted session, reminder, daily goal, review queue, and
appearance survive; the full cutscene appears once for the update; later starts get the
sting; no re-login or data clear is required; the phone’s display and font settings are
untouched.

### J17 — Accessibility

- TalkBack labels for icon-only controls (settings, speaker, back, call controls).
- Every tappable target at least 48 dp.
- Headings exposed as headings; switch roles for reminder toggles.
- Text contrast at WCAG AA for normal and secondary text.
- Font scale 1.3 and 2.0: no clipped labels, no overlapping controls, settings sheet
  scrolls, and all actions remain reachable.
- Reduced motion: the launch shows a still completed frame and advances; the lesson
  intro has no entrance animation that hides content; transitions do not block input.
- Launch cutscene: tap anywhere and the 48 dp **Skip intro** action both work; TalkBack
  reaches the skip action; no essential information is conveyed by motion or colour.
- Lesson intro: the lesson title is a heading, each idea chip has a content description,
  the selected chip is exposed, and Begin is reachable at 200% font after scrolling.
- No essential information is conveyed by colour alone.

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

### J20 — Launch cutscene policy

1. Fresh install or version change: confirm the full 3.2-second sequence appears once,
   before the sign-in gate or Home.
2. Tap anywhere during the full sequence and confirm it ends immediately and stores the
   current version as seen.
3. Force-stop and relaunch: confirm the 1.2-second sting, not the full sequence.
4. Open a password-recovery link: confirm the launch is skipped and the version is not
   marked seen, so the next normal cold start still gets the full sequence.
5. Enable reduced motion: confirm the full sequence shows a still frame for about 1.1 s
   and the sting for about 0.5 s, then advances without blocking input.
6. Launch offline: confirm the sequence still completes and reaches Home.

Edge cases: back during the launch, tap during the sting, process death during the
sequence, update over an existing install, 200% font.  
Correct: full once per version; sting on later starts; tap/Skip escape; recovery links
bypass without consuming the full sequence; reduced motion and offline both work; no
network, sound, permission, or learner data is used.

### J21 — Lesson intro replay rules

1. Open a not-yet-completed lesson from each entry point: Home, Learn, Today’s plan,
   next lesson, and the completion screen.
2. Confirm each opens the intro with the correct lesson content and first-step preview.
3. Back out of the intro, reopen the same lesson, and confirm it opens directly to the
   lesson with no intro.
4. Complete a lesson, reopen it, and confirm it opens directly.
5. Kill the process between the intro and Begin, relaunch, and reopen the lesson.
6. Tap a creative chip, confirm the example appears, then tap Begin; confirm no chip
   selection or example text is persisted.

Edge cases: completed lesson, intro seen but lesson not completed, process death, back
from intro, 200% font, TalkBack.  
Correct: one intro per not-yet-completed lesson; marked seen on entry; no replay after
backing out; completed lessons always direct; all entry points use the same wrapper;
chips are ephemeral local UI state.

### J22 — Smart Review

1. Miss an auto-scorable lesson question, finish the lesson, and confirm **Review tricky
   questions** appears on the quiz result.
2. Open it and confirm the review uses the same exercise UI, five items or fewer, and
   shows the source lesson.
3. Answer one item correctly twice in a row and confirm it leaves the queue; answer
   another incorrectly after a first correct answer and confirm its streak resets.
4. Finish a session and confirm the result shows correct answers, XP, and the 80% bonus
   only at 80% or above.
5. Check Home and Practice before and after the queue empties.
6. Force-stop and relaunch, then reopen review from each entry point.

Edge cases: queue with one item, queue with more than five, wrong answer on the first
review, 100%, 80%, and 60% sessions, creative `WRITE` tasks, unknown saved exercise ID,
malformed stored queue, offline, Night.  
Correct: only auto-scorable bundled exercises enter the queue; a miss adds an item, one
correct keeps it, a second consecutive correct clears it, and a later miss restarts the
streak; review XP is 4 per correct plus 10 for 80%+, counts once toward daily XP, goal,
streak, and active days, and does not change quiz accuracy, perfect quizzes,
`todayQuizzes`, or the daily quick challenge; the queue is capped and survives restart;
no creative task is ever queued.

### J23 — Night appearance

1. Open Settings, confirm Warm paper is selected by default for the owner state, and
   select Night.
2. Walk Home, Learn/level path, a lesson slide and quiz, Practice, Real Talk, Word Tiles,
   flashcards, word bank, Call, Progress, the Settings sheet, and a weekly test.
3. Check text, secondary text, controls, progress bars, stickers, status-bar icons, and
   navigation-bar icons in both palettes.
4. Force-stop and relaunch in Night; confirm the selected appearance and system bars.
5. Switch back to Warm paper and confirm the original palette returns.

Edge cases: selecting the already-selected row, switching during a lesson, resetting
progress in Night, launch cutscene, call screens, 200% font, TalkBack.  
Correct: Night applies immediately and persists; Warm paper remains the default; the
launch cutscene and call screens keep their intentional dark-forest treatment rather than
inverting; body text is at least 4.5:1 and large text/UI components at least 3:1; system
bars match the palette; no screen is left with an unreadable paper-on-paper or
ink-on-ink pair.

### J24 — Review again and reset persistence

1. Finish a review session with items still queued and tap **Review again**.
2. Confirm a fresh five-item session starts and does not resurrect cleared items.
3. Finish the queue and confirm the empty state and the Review again action are sensible.
4. With Night selected, back up the app data, open Reset progress, cancel once, then
   confirm the reset.
5. Confirm XP, streak, completed lessons, drafts, and review items clear; daily goal,
   launch marker, account, friend code, and Night remain.
6. Restore the backup byte-for-byte, force-stop, relaunch, and confirm the owner’s state
   is back.

Edge cases: Review again with an empty queue, reset while a session is open, reset in
Warm paper, process death immediately after reset.  
Correct: Review again opens a fresh session over the remaining queue; reset preserves the
appearance and launch marker, clears only learning data, keeps the account, and a restored
backup returns the exact pre-test state.

## 4. How each layer is tested

| Layer | Tool / method |
| --- | --- |
| Unit logic | Gradle `:app:testDebugUnitTest` — content audit, reminder schedule, next-step selection, cutscene policy, lesson sparks, `ReviewQueue` rules/codec, design-token contrast |
| API / RLS | Supabase REST integration test with a temporary user; direct writes must return 403/42501 |
| End-to-end UI | Release APK on the physical Samsung phone, driven with `adb`/UIAutomator; earlier emulator screenshots retained for settings that cannot be changed |
| Accessibility | Android lint accessibility checks, UIAutomator semantics, earlier emulator TalkBack/font-scale pass |
| Performance | `adb shell am start -W` cold start, frame statistics, APK size; no web Core Web Vitals |
| Responsive | `wm size`/`wm density` overrides on the emulator (earlier pass), restored afterwards; phone display untouched |
| Offline | Phone airplane mode, then restore |
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
- tap-to-skip and Skip-intro on both launch modes; back during the launch;
- back from a lesson intro, reopen the same lesson, and open a completed lesson;
- back/leave during a lesson, test, exam, ringing call, and active call;
- rotate during an exercise, a timed test, and a call;
- notification denied, microphone denied, microphone revoked mid-call;
- review: first wrong answer, one correct then a miss, second consecutive correct,
  80% and 60% sessions, empty queue, Review again, unknown/malformed stored item;
- appearance: Night selected across restart, reset, lesson, test, and call; unknown
  stored theme value falls back to Warm paper;
- offline launch, offline lesson intro, offline review, offline leaderboard, offline
  matching, network restored;
- wrong friend code, own friend code, offline friend, busy friend, blocked pair;
- expired recovery link and reused recovery link;
- process death/relaunch with an active session and a queued review;
- reset while Night is selected, then restore the pre-test backup;
- upgrade over the previous build without clearing data.

## 6. Entry and exit criteria

Entry: release APK builds, unit tests and lint pass, security stage is closed, the
physical phone is unlocked and connected, and the owner’s session is preserved.

Exit: every v1.10-specific journey (Smart Review, Night appearance, Review again, reset
persistence) passes on the physical phone; the retained v1.9 launch/intro and v1.8
journeys still pass or retain recorded evidence; the earlier emulator
accessibility/layout pass is recorded; no serious accessibility violation; no unresolved
crash, ANR, data loss, or security regression; `TEST-REPORT.md` records the results,
failures fixed, and anything still open.

## 7. Out of scope and known limits

- iOS: the project is Android-only.
- Two-phone live-audio quality: requires two physical phones and a quiet audio path.
- Fully signed-out testing on the physical phone: the owner stays signed in; the
  signed-out gate is covered on the emulator and by code review.
- Physical-phone font scale, reduced motion, and TalkBack: the owner’s display and
  accessibility settings must not be changed; these were covered on the emulator.
- API 26 runtime: no API 26 system image is installed; `minSdk` is verified by build and
  lint, not by running on Android 8.
- Payments, file uploads, web admin, and browser performance: not part of this app.
