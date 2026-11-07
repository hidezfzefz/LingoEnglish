# Test report — Lingo English 1.10

## 1.10 release — Smart Review and optional Night

- Build: `1.10.0` (`versionCode 12`), release APK signed with the debug key
- Device: Samsung SM-A546E, Android 16 / API 36 (1080×2340, density 450, font
  scale 1.0, portrait)
- Test window: 2026-11-05 to 2026-11-07

**Result: pass.** Both features were exercised on the physical phone with the
owner still signed in and the display size, density, font scale, and rotation
unchanged. The final release APK over-installed over the debug build with local
data preserved, opened on the Home screen in 587 ms (`am start -W` TotalTime),
and showed 296 XP, a 4-day streak, 12 queued review items, and Warm paper. The
owner used the app between the earlier snapshot and this pass, so the report uses
the larger live totals that were preserved.

### Smart Review

- The queue is built from the 83 bundled auto-scorable exercises (60 lesson
  questions, 23 practice-bank questions). Creative `WRITE` tasks are excluded
  at the content layer and were never queued.
- Controlled session: two consecutive correct answers cleared an item; a later
  wrong answer reset its streak from 1 to 0, incremented misses from 1 to 2,
  kept it queued, and awarded 0 XP. A 1/1 clear awarded 14 XP (4 correct + 10
  bonus), and the total/daily XP, streak, and active-day state updated once.
- Home hid the review card when the queue emptied; Practice showed the
  “nothing tricky waiting” empty state. With items queued, Home showed the
  count and estimated time, and Practice showed “12 waiting · start with 5”.
- A wrong quiz answer revealed “Review tricky questions”; the link opened the
  same review session, and Back returned to the preserved quiz result screen
  with the score and answers intact.
- Final 80% pass: 4 of 5 correct awarded 26 XP (4 × 4 correct plus the 10 XP
  bonus), left 11 items queued, and showed **Review again**. Review again opened
  a fresh five-item session beginning with the next queued question; the item
  cleared in the previous session did not return.
- Offline pass: after force-stop, the release cold-started in 1,963 ms with
  Wi-Fi and mobile data off, showed the 12-item Home card, and opened a review
  question from bundled content with no network.
- The queue survived force-stop and relaunch. The review session itself
  restarted safely instead of resuming a stale snapshot. No crash or ANR
  occurred in any review path.
- XP accounting was checked against the existing economy: review XP counts
  toward total XP, the daily goal, streak, and active days, and does not change
  quiz accuracy, perfect-quiz count, `todayQuizzes`, or the daily quick
  challenge.

### Night appearance

- Settings switched between **Warm paper** and **Night** immediately, with a
  check mark on the active choice. Warm paper remained the default for the
  restored owner state, and Night survived force-stop/relaunch.
- Night was walked on the phone across Home, Learn/level path, lesson slide,
  lesson quiz, Practice, Real Talk, Word Tiles, Flashcards, Word bank, Call,
  Progress, the Settings sheet, and the weekly test session. Text, controls,
  progress bars, and stickers stayed readable in every screen.
- The Settings sheet re-applied light status/navigation icons in Night after
  the dialog window was attached; Warm paper showed dark icons. The launch
  cutscene and call screens stayed on the Paper palette so their dark forest
  treatment did not invert.
- Night survived force-stop/relaunch: the Home background stayed dark, the
  Settings check mark stayed on Night, and the status/navigation icons stayed
  light.
- Reset persistence: with Night selected, the reset dialog was cancelled once
  and the state was unchanged; the confirmed reset cleared XP, streak, review
  items, and drafts, but kept Night, the 80 XP daily goal, launch marker `12`,
  the signed-in session, and the friend code. The pre-test backup was then
  restored byte-for-byte and Warm paper returned.
- `DesignTokensTest` checks the key text/background pairs: body text at least
  4.5:1, large text and UI components at least 3:1, in both palettes.
- The Night fixes below were found during this walk and rechecked after the
  final build.

### Focused journey results

| Journey | Result | Evidence |
| --- | --- | --- |
| J22 — Smart Review | Pass | Queue counts, five-item session, two-correct clearing, miss reset, 80% XP bonus, empty states, quiz-result link, and offline loading all behaved as specified. |
| J23 — Night appearance | Pass | Immediate switch, check mark, full-screen walk, force-stop persistence, system bars, and both palette contrast suites passed. |
| J24 — Review again and reset | Pass | 4/5 session showed 26 XP and 11 remaining; Review again started fresh with the next queued item; reset kept Night/goal/marker/session; backup restore returned the exact pre-test state. |
| J13 / J14 — Session and offline | Pass | Signed-in session and 12-item queue survived force-stop; offline cold start took 1,963 ms and review content stayed available. |

### Automated checks

| Check | Result |
| --- | --- |
| `:app:testDebugUnitTest` | 47 tests, 0 failures (including `ReviewQueueTest`, `DesignTokensTest`, and the existing content/reminder/next-step/cutscene suites) |
| `:app:lintDebug` | 0 errors, 44 warnings (all pre-existing `UseKtx` style suggestions; no new warning category) |
| Release build | `:app:assembleRelease` successful; 73,046,449 bytes |
| Debug build | `:app:assembleDebug` successful; debug and release both use the debug signing key, so the over-install preserved data |
| Cold start | Final release `am start -W`: TotalTime 587 ms, WaitTime 591 ms; offline release cold start 1,963 ms |
| Release install | `adb install -r app-release.apk` over the debug build: Success; version `1.10.0` (`12`); signed-in owner state intact; no `DEBUGGABLE` flag |
| Reset / restore | Night reset removed learning data but kept appearance, goal, launch marker, and session; tar restore returned the exact pre-test `lingo_progress.xml` SHA-256 `576b2328…` |

Final release artifact:

| Item | Value |
| --- | --- |
| APK | `app/build/outputs/apk/release/app-release.apk` |
| Size | 73,046,449 bytes |
| SHA-256 | `9ea0ce05b867b07c4dc150bae8a7f0183e9a8883ede159eff77d058ddd20f1c1` |
| Signature | APK Signature Scheme v2, `CN=Android Debug`, cert SHA-256 `3888438c…` (accepted risk A-01) |
| Version | `1.10.0` (`versionCode 12`), min SDK 26, target SDK 37 |
| CI | Pass — [Content audit 42408086000](https://github.com/hidezfzefz/LingoEnglish/actions/runs/42408086000) on `83f1153` |
| Release | [v1.10.0](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.10.0) |
| Release asset | Downloaded `LingoEnglish-1.10.0.apk` from the v1.10.0 release; SHA-256, V2 certificate, size, and version match the tested APK |

Post-release verification (2026-11-07): the published `LingoEnglish-1.10.0.apk`
was downloaded again, its SHA-256 matched
`9ea0ce05b867b07c4dc150bae8a7f0183e9a8883ede159eff77d058ddd20f1c1`, and it
installed with `adb install -r` over the existing 1.10.0 release on the phone.
The version stayed `1.10.0` (`12`), the cold start reached Home in 399 ms, and
the signed-in session, 296 XP, 4-day streak, 12 queued review items, and Warm
paper were preserved.

### Fixes made during testing

1. **Settings sheet system bars.** Material created the modal sheet’s dialog
   window with dark status/navigation icons even in Night. `SheetSystemBarAppearance()`
   now finds the dialog window through `DialogWindowProvider` and re-applies the
   active palette’s icon appearance after layout.
2. **Level path background.** The level path list had no full-screen background
   in Night, leaving a Paper-cream window behind unreadable header content and
   white status icons. It now paints `Cream`, which resolves to the dark
   background in Night and the original paper in Warm paper.
3. **Small-text contrast.** `BackHeader` titles now use `Ink`; the word-bank
   speaker uses `Ink`; the unsaved word-bank star uses `Muted`; the flashcard
   peach speaker uses `CoralDeep`; the flashcard lime speaker and lesson
   speaker use `Ink`/`palette.deep`. These replace cream-on-light fills that
   fell below 4.5:1 in Night.

### Data preservation and cleanup

- The owner’s pre-test app data was backed up outside the repo to
  `/tmp/opencode/lingo-v110-owner-2026-11-06.tar` (245,248 bytes, SHA-256
  `9efa1cc8…`). It contains the 296 XP / 4-day / 12-item state plus the encrypted
  session.
- The reset probe used the same-version `1.10.0` debuggable build because
  `run-as` is required for a byte-level backup and restore. The release build was
  reinstalled immediately afterward. The restored `lingo_progress.xml` hash
  `576b2328…` and `lingo_secure_auth.xml` hash `162d6a64…` both match the backup.
- The final state is the release build `1.10.0` (`12`), still signed in, with
  296 XP, a 4-day streak, 12 queued review items, completed-lesson and draft
  data intact, and Warm paper as the appearance.
- No app data was left cleared, the app was not uninstalled, and the owner was
  not signed out.

### Environment limits

The v1.9 environment limits still apply: the owner’s accessibility settings
were not changed, so 200% font, reduced motion, and TalkBack variants retain
the v1.9 emulator evidence; fully signed-out, two-phone audio, physical-phone
reminder tap, recovery-email delivery, and API 26 runtime remain owner checks
or build/lint evidence. Night-specific TalkBack labels reuse the existing
semantics; the appearance rows are `selectable` with `Role.RadioButton`.

The v1.10 Smart Review and Night walks ran at the phone’s normal 360×780 dp,
font scale 1.0, and portrait orientation. The 360×640 dp, 480×1000 dp, landscape,
and 200% font layouts retain the v1.9 emulator evidence because changing the
owner’s display, font, or rotation is not permitted. The reset/restore probe used
the same-version debuggable build only to access app-private files; the final
release APK was reinstalled and its preserved state and cold start were verified.

---

## 1.9.1 patch — A2 Travel lesson finish crash

- Build: `1.9.1` (`versionCode 11`), release APK signed with the debug key
- Device: Samsung SM-A546E, Android 16 / API 36 (1080×2340)
- Test window: 2026-11-04

**Root cause.** The owner’s saved `u2-l1` creative draft is 112,356 characters
across 2,089 lines. `OutlinedTextField` grew to fit every line, producing a
measured height of 262,994 px. Compose packs layout constraints into 18 bits, so
the maximum representable dimension is 262,143 px. Entering step 6 crashed with:

```text
java.lang.IllegalArgumentException: Can't represent a width of 962 and height of 262994 in Constraints
    at androidx.compose.material3.OutlinedTextFieldMeasurePolicy.measure
```

Android’s exit record confirmed `APP CRASH(EXCEPTION)` at 21:52:45 and an earlier
identical crash at 21:48:07.

**Fix.** `ExerciseView.kt` now gives the creative field `minLines = 4` and
`maxLines = 8`. The field keeps a readable height and scrolls internally; the
full draft stays in `writing_u2-l1-create` and remains editable. No data is
truncated or migrated.

**Verification.**

- Debug build with the fix: opened the real `u2-l1` creative step with the
  2,089-line draft, showed `16,567 words`, checked the writing, finished the
  lesson, and reached `Lesson complete!` with `+35 XP` and `4/4 tasks met`.
- Release candidate `1.9.1` (`11`) installed over the debug build with
  `adb install -r`; the same full path passed on the release binary. Logcat had
  no `FATAL EXCEPTION` or `AndroidRuntime` entry, and the app process stayed
  alive.
- The pre-test progress file was restored byte-for-byte (SHA-256
  `0d7b986e…`); the final phone state is the release build `1.9.1` (`11`) with
  the signed-in session, 1-day streak, 179 XP, and `u2-l1` still incomplete.
- `:app:testDebugUnitTest` 28/28; `:app:lintRelease` 0 errors, 43 non-blocking
  warnings.

Final patch artifact:

| Item | Value |
| --- | --- |
| APK | `app/build/outputs/apk/release/app-release.apk` |
| Size | 72,997,297 bytes |
| SHA-256 | `60dd5d0903448bc980ab1a836ea56dc58d8b70541323c3feeb2f728f61e76cba` |
| Signature | APK Signature Scheme v2, `CN=Android Debug`, cert SHA-256 `3888438c…` (accepted risk A-01) |
| Version | `1.9.1` (`versionCode 11`), min SDK 26, target SDK 37 |
| CI | Pass — [Content audit 41923416312](https://github.com/hidezfzefz/LingoEnglish/actions/runs/41923416312) |
| Release | [v1.9.1](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9.1) |
| Release asset | Downloaded from the v1.9.1 release; SHA-256, V2 certificate, size, and version match the tested APK |

Post-release verification (2026-11-04): the published `LingoEnglish-1.9.1.apk` was
downloaded again, its SHA-256 matched, and it installed with `adb install -r`
over the existing 1.9.1 release on the phone. The version stayed `1.9.1` (`11`),
the cold start reached Home in 1.470 s, and the signed-in session, 1-day streak,
and 179 XP were preserved. `u2-l1` remains incomplete at 0/80 XP.

---

# v1.9 baseline

- Build: `1.9` (`versionCode 10`), release APK signed with the debug key
- Platforms: Samsung SM-A546E, Android 16 / API 36 (1080×2340) as the remaining v1.9 device; Android 17 / API 37 emulator for the earlier accessibility and layout pass
- Test window: 2026-10-31 to 2026-11-01
- Plan: `TEST-PLAN.md`

## Result

**Pass, with the environment limits listed at the end.** The v1.9 launch cutscene and
once-per-lesson intro were exercised on the physical phone and on the emulator,
including tap-to-skip, the short sting on later cold starts, reduced motion, 200% font,
TalkBack semantics, the first-step preview, creative chips, Begin routing, back-out and
reopen replay rules, completed-lesson direct entry, offline launch, and a data-preserving
over-install. The final release APK was installed over the phone’s debug build without
uninstalling; the signed-in session and 133 XP / 3 completed lessons were preserved.
All v1.8 features and local data remain in place. The v1.8 baseline still supplies the
evidence for journeys that v1.9 did not change.

Final release artifact:

| Item | Value |
| --- | --- |
| APK | `app/build/outputs/apk/release/app-release.apk` |
| Size | 72,997,289 bytes |
| SHA-256 | `1d37c06a82d8f8ecb54e3e5ac9ff650929567d9bee63a4f759fb2764552cbaec` |
| Signature | APK Signature Scheme v2, `CN=Android Debug`, cert SHA-256 `3888438c…` (accepted risk A-01) |
| Version | `1.9` (`versionCode 10`), min SDK 26, target SDK 37 |
| CI | Pass — [Content audit 41730852766](https://github.com/hidezfzefz/LingoEnglish/actions/runs/41730852766), 2026-11-03 |
| Release | [v1.9](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9) |
| Release asset | Downloaded from the v1.9 release; SHA-256, V2 certificate, size, and version match the tested APK |

Post-release verification (2026-11-04): the published `LingoEnglish-1.9.apk` was downloaded
again, its SHA-256 matched, and it installed with `adb install -r` over the existing 1.9
release on the phone. The version stayed `1.9` (`10`), the cold start reached Home in
1.079 s, and the signed-in session, 1-day streak, and 179 XP were preserved. The earlier
over-install snapshot in this report recorded 133 XP; the owner has used the app since.

## v1.9 changes under test

- `CutscenePolicy`: full launch once per app version, sting on later cold starts,
  recovery links bypass the launch without consuming it, and lesson intros only for
  lessons that are neither completed nor already seen.
- `CutsceneContent`: one open creative prompt and two or three idea chips for each of the
  20 lessons. Chips are bundled local content and reveal examples only.
- `CutsceneScreen`: the full and sting launch compositions, tap/Skip escape, reduced
  motion, the lesson intro, stacked path pills at large font, and labelled chips.
- `LingoApp`: one `openLesson()` wrapper for every entry point; intro marked seen on
  entry; completed lessons open directly; back handling leaves the intro without replay.
- `ProgressStore`: additive `launch_cutscene_version` and `lesson_intros_seen` markers;
  a learning reset clears the lesson intros but keeps the launch marker, so resetting
  progress does not replay the full launch sequence.
- `app/build.gradle.kts`: version `1.9` / `versionCode 10`.

## Journey results

| ID | Journey | Result | Evidence / notes |
| --- | --- | --- | --- |
| J1 | First launch, cutscene, and gate | Pass for v1.9 paths | Phone: full sequence rendered before Home, tap-to-skip at ~1.7 s, launch marker advanced to `10`; force-stop/relaunch showed the sting in 1.61 s (`am start -W`). The signed-out gate itself was covered by the v1.8 baseline because the owner stays signed in. |
| J4 | Home | Pass | After the final release over-install, Home showed “Good afternoon.”, 133 XP, 1-day streak, daily goal 80, and the next lesson “At the Airport”. No data or session loss. |
| J5 | Lessons, intro, and creative tasks | Pass | Phone: `u2-l2` opened its intro with the first-step preview and a creative prompt; tapping a chip revealed “Breakfast is on the second floor until ten.”; **Begin first step** opened the real lesson at `1 / 6`. `u2-l3` was backed out of and reopened directly to the lesson with no replay. Completed `u1-l1` opened directly to `LESSON 1 / 6`. |
| J13 | Session lifecycle | Pass | The signed-in session survived the debug-to-release over-install and force-stop/relaunch; no re-login was required. Keystore-backed storage is unchanged from v1.8. |
| J14 | Offline and recovery | Pass | Phone in airplane mode: cold start reached Home in 1.695 s with local progress intact; the launch did not wait for a network. Airplane mode was turned off and connectivity verified with a successful ping. |
| J16 | Data preservation / upgrade | Pass | Emulator v1.8→v1.9 over-install preserved 200 XP, A2, completed `u1-l1`, writing draft, reminder, and daily goal. Phone: the final release APK installed over the v1.9 debug build with `adb install -r` and preserved the signed-in session and 133 XP / 3 completed lessons. No uninstall or data clear was used. |
| J17 | Accessibility | Pass, phone settings untouched | Emulator: 200% font launch and intro, path pills stack vertically at `fontScale >= 1.5`, TalkBack reaches the skip action, chip semantics expose labels, and Begin remains reachable by scrolling. The phone’s display size, density, font scale, and rotation were not changed. |
| J19 | Security smoke checks | Pass | No new permissions, exported components, network calls, accounts, or learner data. The release APK contains only the public Supabase `anon` JWT; scans are recorded below. |
| J20 | Launch cutscene policy | Pass | Full 3.2 s sequence once per version; tap-anywhere and **Skip intro** both escape; marker stored on finish. Later cold start uses the 1.2 s sting. Emulator: reduced motion showed a still frame and advanced (1.1 s full / 0.5 s sting). Phone: offline launch passed. |
| J21 | Lesson intro replay rules | Pass | All lesson entry points route through `openLesson()`. Intro is marked seen on entry, so backing out never replays it; completed lessons skip it; Begin opens the real step 0 in one tap. Chips are local Compose state and are not persisted. |

## v1.8 regression baseline

The v1.9 change is additive UI gating and two local progress markers. The following
v1.8 paths were rechecked on the v1.9 build: signed-in session, Home, Learn, lesson
entry and completion state, offline launch, over-install, and the full unit/lint gate.
The remaining v1.8 journeys — sign-up, recovery, weekly test, level exam, practice,
live calls, progress/leaderboard, reminders, permissions, and responsive layouts — are
unchanged by this release and retain their v1.8 evidence in the previous report. No v1.8
feature was removed, and the release introduces no new permissions or network paths.

## Stage 5 skill applicability

| Skill | Applied? | Reason |
| --- | --- | --- |
| `convio/thorough-testing` | Yes, Android equivalent | Wrote `TEST-PLAN.md`, ran unit tests and end-to-end phone journeys, covered failure cases, checked accessibility, performance, and responsive layouts, and wrote this report. Browser/Playwright/Lighthouse/linkinator steps do not apply to a native Compose app. |
| `convio/no-account-resources` | No | No preview URL, hosted database, or borrowed browser is needed; the APK is installed directly on the owner’s phone. |
| `jeffallan/playwright-expert` | No | No web UI or Playwright suite exists. Android UI journeys were driven with `adb` and UIAutomator instead. |
| `alirezarezvani/playwright-pro` | No | Same reason; there is no browser test suite to scaffold or review. |
| `wshobson/wcag-audit-patterns` | Yes, adapted | Applied POUR thinking to Compose semantics, labels, headings, contrast, 200% font, reduced motion, and touch targets. |
| `lambdatest/accessibility-skill` | No | No LambdaTest account or cloud device grid is used; accessibility was checked with Android lint, UIAutomator, and the emulator TalkBack/font-scale pass. |

Lighthouse mobile scores and Core Web Vitals are **N/A**: there is no web page. The
Android performance equivalents are the cold-start measurements (1.61 s sting, 1.695 s
offline launch) and the release APK size (72,997,289 bytes).

## Fixes made during testing

1. **Path pills broke at large font.** At `fontScale >= 1.5`, the three 1/2/3 pills now
   stack full-width instead of squeezing labels. Verified at 200% font on the emulator.
2. **Creative chips lacked spoken labels.** Chip semantics now merge descendants and
   expose a content description, so TalkBack reads the chip label and selected state
   instead of an unlabelled clickable node. Verified with `uiautomator` on the emulator.

## Automated checks

| Check | Result |
| --- | --- |
| `:app:testDebugUnitTest` | 28 tests, 0 failures (ContentAudit, ReminderSchedule, NextStep, CutscenePolicy, CutsceneContent) |
| `:app:lintDebug` | 0 errors, 42 warnings (KTX style suggestions, newer-dependency notices, obsolete-SDK notice) |
| `gitleaks git` | 0 findings across 19 commits |
| `gitleaks dir` | 11 findings, all in ignored build outputs, `local.properties`, or `supabase/.env.local`; none tracked |
| Semgrep (`p/default` + `p/secrets`, Docker) | 1 warning: expected exported launcher activity (A-04). No secrets or injection findings. |
| APK content scan | One JWT: the expected public Supabase `anon` token (`role=anon`). No service-role key, LiveKit secret, SMTP password, Supabase access token, database password, or private-key material. |
| Merged release manifest | Eight normal permissions (`INTERNET`, `ACCESS_NETWORK_STATE`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `BLUETOOTH`, `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`); no camera, foreground-service, media-projection, or screen-capture entries. Only `MainActivity` and the DUMP-guarded profile installer receiver are exported. |
| APK signature / badging | V2, `CN=Android Debug`; `1.9` (`10`), min 26, target 37; 72,997,289 bytes |

## Test data cleanup

- The phone’s pre-test progress backup was restored byte-for-byte (SHA-256
  `cb3eb70afcf3c20184dcd1b8152ef31a9de74f8d422297d64979a9ae3a8deb7a`).
- The test-added `u2-l2` and `u2-l3` intro markers were removed; only the owner’s
  existing `u2-l1` marker remains.
- The owner stayed signed in. The phone’s display size, density, font scale, and rotation
  were not changed. No app data was cleared and the app was not uninstalled.
- The emulator was stopped and was not used for the remaining device checks.

## Environment limits

- **Physical-phone font scale, reduced motion, and TalkBack.** The owner’s accessibility
  settings must not be changed. These checks were run on the emulator instead; the phone
  pass used the default settings.
- **Fully signed-out physical-phone pass.** The owner remains signed in. The signed-out
  gate is covered by the v1.8 baseline and code review, not by signing the owner out.
- **Two-phone audio quality.** Call signalling and controls retain v1.8 evidence; real
  speaker/microphone quality still needs two phones in a quiet room.
- **Physical-phone reminder tap.** The reminder remains configured from v1.8; the
  on-phone tap is still an owner check.
- **Recovery email delivery.** App-side recovery handling retains v1.8 evidence; real
  delivery to a routable mailbox remains an owner check.
- **API 26 runtime.** No Android 8 system image is installed; `minSdk` is verified by
  build and lint, not by a runtime pass.
- **Semgrep on the host.** The host binary hit a memory fault on the Kotlin rules; the
  full scan was run successfully in the Docker image with 4 GB.

## Exit decision

No unresolved app-logic crash, ANR, data loss, or high-severity security finding remains.
The launch and lesson-intro acceptance criteria pass on the physical phone, with the
accessibility variants covered on the emulator because the owner’s settings must not be
changed. The final release APK is built, signed, installed over the phone’s debug build,
and preserves the session and local progress. The two medium accepted risks remain the
debug signing key (A-01) and the custom-scheme recovery link (A-02). The release is
published at [v1.9](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9); the
downloaded asset’s SHA-256, V2 certificate, size, and version match the tested APK.
