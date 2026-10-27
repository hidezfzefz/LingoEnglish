# Test report — Lingo English 1.8

- Build: `1.8` (`versionCode 9`), release APK signed with the debug key
- Platforms: Android 17 / API 37 emulator (1080×2400, 420 dpi) and Samsung SM-A546E, Android 16 / API 36 (1080×2340)
- Test window: 2026-10-26 to 2026-10-27
- Plan: `TEST-PLAN.md`

## Result

**Pass, with the environment limits listed at the end.** Every core journey — login,
lessons, weekly test, level exam, practice, live calls, ratings, reports, blocking,
reminders, offline recovery, responsive layouts, accessibility, data preservation, and
security smoke tests — was exercised on the release runtime. The published GitHub APK
was downloaded over HTTPS, its checksum and signer verified, and installed over the
existing builds on both the emulator and the physical phone without uninstalling; the
phone’s session and local data were preserved. Two phone-only checks remain open:
two-phone audio quality and a fully signed-out physical-phone pass. They are not code
regressions and are called out below.

Final release artifact:

| Item | Value |
| --- | --- |
| APK | `app/build/outputs/apk/release/app-release.apk` |
| Published | <https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.8> |
| Assets | `LingoEnglish-1.8.apk`, `LingoEnglish-1.8.apk.sha256` |
| Size | 72,948,079 bytes |
| SHA-256 | `0394199900e79bf2ebdf744c72725bf0334a8be73efde7235a31eac89d7b39b7` |
| Signature | APK Signature Scheme v2, `CN=Android Debug`, cert SHA-256 `3888438c…` (accepted risk A-01) |
| Version | `1.8` (`versionCode 9`), min SDK 26, target SDK 37 |
| CI | [Run 40412239645](https://github.com/hidezfzefz/LingoEnglish/actions/runs/40412239645) on `67ae40b`: unit tests and Android Lint passed |

## Published-release verification

- `v1.8` tag points at `67ae40b`; the repository is public and MIT licensed.
- The release APK was re-downloaded from the release URL over HTTPS. Its SHA-256 matched
  `0394199900e79bf2ebdf744c72725bf0334a8be73efde7235a31eac89d7b39b7`, and `apksigner`
  reported the same V2 debug certificate as the build output.
- The downloaded APK installed with `adb install -r` over the debug build on the
  emulator, and over 1.7 on the physical phone. Both retained their existing session and
  local progress.
- The APK pulled back from the phone after install had the same SHA-256 and V2 certificate.
- Cold start on the physical phone was 2.6 s; the emulator cold start after the
  over-install was 4.9 s. No normal-launch crash or ANR occurred on either device.

## Journeys

| ID | Journey | Result | Evidence / notes |
| --- | --- | --- | --- |
| J1 | Login gate and credentials | Pass | Gate blocks tabs; empty/invalid/wrong-password replies are short and do not reveal whether an email exists; only one request runs while the button is busy. Sign-out returns to the gate. |
| J2 | Sign-up | Pass | Fresh account created, profile/friend code generated, Home opened, sign-out/sign-in worked. Duplicate-email and short-password validation are implemented server-side and mapped to safe copy. |
| J3 | Password recovery | Pass, with email-delivery limit | Server-generated `lingo://reset-password` link opened the recovery form; mismatched passwords were rejected; a valid change succeeded; the old password was rejected; the new password signed in. An invalid link produced “This reset link is invalid or expired.” Reusing the same link before token expiry is possible and is recorded under A-02. Real email delivery was not tested (test mailbox is unroutable). |
| J4 | Home | Pass | Greeting, streak, XP, goal, today’s lesson and Begin updated after lessons. The conditional next-step choice is covered by six unit tests; the hero-suppression case was observed. |
| J5 | Lessons and creative tasks | Pass | Full lesson flow with teaching, multiple choice, listening, word tiles, and the creative task; +30 XP and 3/4 tasks on first completion; writing draft persisted. Rotation/duplicate-submit guards were reviewed in code and exercised through the shared test controls. |
| J6 | Weekly test | Pass, timer via shared component | 10-question weekly test completed at 2/10; an unanswered question blocked submission with a clear message; score persisted once. The countdown/deadline logic is shared with the level exam, where expiry was forced. |
| J7 | Level exam / CEFR | Pass | A2 exam: 12/12, +50 XP, A2 unlocked, B1 shown locked at 200 XP. Fail/retake path: invalidated attempt displayed “Try again Nov 2” until the retry date was cleared. Landscape: all four options visible and selectable above the fixed Next button; rotation mid-attempt preserved the timer and answers. B1 timed attempt auto-submitted at 00:00 without losing the screen. |
| J8 | Practice workouts | Pass | All seven Practice entries opened directly from the release build: Daily Challenge (question 1/5), Real Talk (four scenes), Word Tiles (puzzle 1/5), Flashcards (card 1/36, flip/reveal), Listening (1/5), Grammar (1/5), and Word bank (36 words, filters). Back navigation returned to the hub each time; no workout was completed, so XP and saved words were unchanged. |
| J9 | Call setup, matching, friend codes | Pass, two-phone ringing not repeated | A2 profile, stats and friend code rendered; start/cancel queue worked; invalid code, own code, and blocked pair returned safe messages; double-tap guard prevents a second search. A second temporary account rang the phone over the real signalling path, and Accept/Decline appeared correctly. Cross-level friend calls are allowed by the RPC; only same-level random matching filters by level. |
| J10 | Active call, safety, history | Pass, audio quality not tested | Connected call with a second LiveKit participant: timer ran, mic toggled, reactions rendered, End produced the summary. 5-star rating persisted (`rating_count=1`, average 5.0). Report → Harassment → “Report and block” ended the call, showed “Report sent”, created the report row and block row, and a later call returned “This learner is blocked.” Call history counters updated. |
| J11 | Progress, stickers, weekly board | Pass | XP (200), A2 level, weekly 2/10, lesson count and streak matched Home and the level path. Sticker and stat values were checked. After signing in with a fresh temporary account, opening the level path triggered the live weekly-board refresh; it rendered the account as “1 You 2/10” with the same-level cohort note. There is no separate manual refresh control; refresh on open is the designed path. |
| J12 | Settings, reminders, notifications | Pass | Goal 30/40 persisted across dismissal and relaunch. Reminders defaulted off at 19:00; deny showed guidance and a system-settings link; grant + 20:00 scheduled one inexact `RTC_WAKEUP` alarm; reboot rescheduled it; changing the time replaced it; disable cancelled it. A 21:10 alarm fired and rescheduled for tomorrow but correctly posted no notification because the daily goal was already met. On the emulator, with today's XP reset and yesterday as the last study day, the 21:14 and 21:21 alarms posted notification `1801` on `practice_reminders`: “A few minutes of English?” / “Your 1-day streak is waiting. Tap to practise.” The notification's content intent opened the Practice hub when delivered to a warm app. The emulator reminder was then disabled, the alarm cancelled, and its settings restored to off/19:00. On the physical phone, the 00:29 alarm fired on 27 Oct and rescheduled for 28 Oct; the notification had already been cleared when checked later, so on-phone tap confirmation remains with the owner. |
| J13 | Session lifecycle | Pass, fault injection limited | Keystore-backed session survived force-stop, over-install and reboot; sign-out returned the gate; sign-in restored the same friend code and 200 XP/A2. Deleting a temporary account server-side made the next Call load show “Your call profile could not load” with Try again and Sign out; signing out returned the gate with 200 XP/A2 intact and no crash. Corrupted encrypted prefs and Keystore-unavailable fallback were reviewed, not forced. |
| J14 | Offline and recovery | Pass, partial | Airplane-mode launch and Home/Learn/Practice/Progress navigation worked from local data; Call showed a retry instead of hanging; reconnecting and tapping Try again loaded the A2 hub. Offline sign-in and an offline friend call were not separately driven. |
| J15 | Permission prompts | Pass, mid-call revoke not run | Notification allow/deny and microphone allow/deny appeared only in context. Denying the microphone produced “Microphone access is needed for a live call.” with no crash or repeated prompt. Android kills the process when a runtime permission is revoked during active use; that platform case was left as a documented limit rather than forced. |
| J16 | Data preservation / upgrade | Pass | Over-install preserved session, 200 XP, A2, friend code and writing draft. The final release APK was then installed over the debug build without uninstalling: `1.8` (`9`), session still signed in, 200 XP/A2/friend code `D38-17D` intact, Settings drag handle labelled, weekly 2/10 retained, cold start 4.9 s. Force-stop/relaunch preserved the same values again. The same published APK was downloaded over HTTPS and installed over 1.7 on the owner's Samsung SM-A546E (Android 16) without uninstalling; the signed-in session, 60 XP, 2-day streak, A1 level, daily goal 80, friend code `E6E-108`, writing data, and the enabled 00:29 reminder were all preserved. Display size, density, font scale and rotation were unchanged, and cold start was 2.6 s. Earlier, a post-reboot emulator launch showed a system-wide ANR while services were still starting; a normal cold launch then completed in about 3.6 s. |
| J17 | Accessibility | Pass, TalkBack manual limited | Icon-only controls carry descriptions; reminder row exposes a switch/checkable state; daily-goal chips expose their number and 48 dp minimum height; headings are marked. Font scales 1.3 and 2.0 kept Home and Settings scrollable with no clipped actions. The sheet drag handle was labelled during this pass. A full TalkBack read-through was not performed. |
| J18 | Responsive layouts | Pass | 360×640, 411×914 and 480×1000 dp portrait, plus 411×914 landscape. Home, Call and Exam had no horizontal scroll or clipped primary actions; bottom navigation stayed usable. |
| J19 | Security smoke checks | Pass | Signed-out users cannot reach tabs; recovery tokens stay in memory and are cleared after use; APK contains only the public anon JWT; direct writes and helper RPCs are denied; scans below are clean apart from accepted findings. |

## Fixes made during testing

1. **Landscape exam options were clipped.** Replaced the single-column answer list with a compact two-column grid in landscape, reduced vertical spacing, and reset a per-question `ScrollState`. Verified all four answers remain above the Next button and rotation preserves the attempt.
2. **Raw PostgREST error leak (S-07).** An unknown friend code displayed the request URL and redacted Authorization metadata. `friendlyError` now maps known server conditions to short text and uses a generic fallback. Verified invalid, own, and blocked codes.
3. **Unlabelled sheet drag handle.** Added “Drag handle” semantics to the Settings sheet handle so it no longer appears as an unlabelled clickable node.
4. **Mutable GitHub Actions tags (S-08).** Pinned the four workflow actions to reviewed commit SHAs.

## Automated checks

| Check | Result |
| --- | --- |
| `:app:testDebugUnitTest` | 17 tests, 0 failures (ContentAudit 1, ReminderSchedule 10, NextStep 6) |
| `:app:lintDebug` | 0 errors, 39 warnings |
| `gitleaks git` | 0 findings across tracked history |
| `gitleaks dir` | 11 findings, all ignored files (`build/`, `local.properties`, `supabase/.env.local`); none tracked |
| OSV `querybatch` | 0 vulnerabilities in 204 Maven modules |
| Semgrep (`p/default` + `p/secrets`, Docker) | 1 warning: expected exported launcher activity (A-04) |
| APK content scan | Only the public `anon` Supabase JWT; no service-role, LiveKit, SMTP, access-token, database-password or private-key material |
| Merged release manifest | No camera, foreground-service, media-projection or screen-capture entries; only `MainActivity` and the DUMP-guarded profile installer receiver exported |
| APK signature / badging | V2, `CN=Android Debug`; `1.8` (`9`), min 26, target 37 |

## Test data cleanup

The temporary accounts used for sign-up, calls, reports, and the leaderboard were
deleted server-side; deleted-account sign-in returns HTTP 400 and no profile row remains.
The emulator was left signed out at the login gate with 200 XP/A2 and the reminder
off/19:00. The physical phone was not signed out; its 60 XP, 2-day streak, A1, daily goal
80, friend code `E6E-108`, and 00:29 reminder were left unchanged.

## Environment limits

- **Physical-phone reminder tap and signed-out pass.** The published APK is installed
  over 1.7 on the physical phone with its 00:29 reminder preserved. The 27 Oct alarm
  fired and rescheduled for 28 Oct, but the notification had been cleared by the time it
  was checked, so the on-phone tap was not observed. The phone also stays signed in as
  the owner, so a fully signed-out physical-phone journey was not run. Both are owner
  checks rather than code gaps.
- **Two-phone audio quality.** Call signalling, LiveKit room join, controls, rating,
  report and block were exercised with a headless second participant and a second
  account. Speaker/microphone quality, echo, and Bluetooth routing still need two real
  phones in a quiet room.
- **Recovery email delivery.** The app-side recovery flow was exercised with a
  server-generated link. Actual Gmail SMTP delivery/confirmation remains deferred.
- **API 26 runtime.** No Android 8 system image is installed; min SDK is covered by the
  build and lint, not by a runtime pass.
- **Mid-call permission revocation.** Android terminates the app process when
  `RECORD_AUDIO` is revoked while in use; this platform behaviour was reviewed but not
  forced because it destroys the session.
- **Emulator render stall during notification taps.** Twice, tapping the reminder while
  the emulator's RenderThread was saturated produced a 5-second input-dispatch ANR. The
  captured main-thread stack was entirely in `HardwareRenderer.setStopped` waiting for
  RenderThread, with the emulator radio service near 100% CPU and thousands of major
  faults. The same content intent opened Practice when delivered to a warm app, and
  normal cold starts completed in 4.9–8.0 s. This is an emulator resource limit, not app
  logic; the physical phone’s scheduled 00:29 reminder will confirm delivery in normal use.
- **TalkBack.** Labels, roles and touch targets were checked through UIAutomator and
  Android lint; a complete spoken-navigation pass was not run.

## Exit decision

No unresolved app-logic crash, data loss, or high-severity security finding remains. The
only ANRs seen were two emulator RenderThread stalls during notification taps and the
earlier post-reboot system-wide start; normal cold launches completed on both devices,
including the final release installed over the debug build. The two medium accepted risks
are the debug signing key (A-01) and the custom-scheme recovery link (A-02). The v1.8
release is published with a verified APK, checksum, signature and green CI, and it
installed over the existing debug build on the emulator and over 1.7 on the physical
phone with local data preserved. Stage 7 testing and stage 8 shipping are complete on
that basis. The two-phone audio check, on-phone reminder tap, and fully signed-out
physical-phone pass remain owner actions; the phone’s next scheduled reminder is at
00:29.
