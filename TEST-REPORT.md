# Test report — Lingo English 1.9

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
