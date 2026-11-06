# Security review — Lingo English 1.10

Reviewed: 2026-11-06

Scope: the Android app, its Supabase schema/RLS/Edge Functions, LiveKit token issuing, and the v1.10 release APK.

Reviewer: Lingo maintainers (self-review, no external penetration test).

## 1.10 release

Two local-only features are added: **Smart Review** and an optional **Night**
appearance. Neither adds a permission, dependency, network endpoint, account,
exported component, backend schema change, or new service.

- **Review data.** `ProgressStore` adds the `review_items` preference: one
  tab-separated line per queued exercise with the exercise ID, miss count,
  consecutive-correct streak, and last-seen timestamp. Exercise IDs are checked
  against `^[A-Za-z0-9_-]{1,80}$`, counts are range-checked, malformed lines and
  duplicate IDs are ignored, and decoding is capped at 200 lines. The queue is
  read only through `LingoContent.exerciseById`; an ID that no longer exists in
  bundled content is filtered out before a session. There is no free text and
  no learner-authored content in this field.
- **Appearance data.** `theme_mode` stores only `PAPER` or `NIGHT`; an unknown
  value falls back to Warm paper. It is a display preference and is not cleared
  by **Reset progress**.
- **Smart Review UI.** The session snapshots at most five bundled exercises,
  reuses `ExerciseView`, records a miss or correct exactly once per checked
  answer, and awards XP through the existing `addTotalXp`/`recordStudy` path.
  It adds no WebView, file access, intent handling, logging, or network call.
- **Night UI.** Palette values are compiled-in constants. Switching appearance
  changes colors and system-bar icon flags only; it does not store or transmit
  learner data.
- **Capture hooks.** `LessonScreen` and `QuizScreen` record review outcomes only
  for non-creative exercises. Creative `WRITE` tasks are excluded from the
  reviewable set at the content layer as well as at the UI layer.

## Release under review

| Item | Value |
| --- | --- |
| Package | `com.lingo.app` |
| Version | `1.10.0` (`versionCode 12`) |
| APK | `app-release.apk`, 73,046,449 bytes |
| Published | Pending v1.10.0 release |
| CI | Pending v1.10.0 push |
| SHA-256 | `9ea0ce05b867b07c4dc150bae8a7f0183e9a8883ede159eff77d058ddd20f1c1` |
| Signature | APK Signature Scheme v2, `CN=Android Debug` (see A-01) |
| Published asset | Pending; verify after publication. |
| Minimum / target SDK | 26 / 37 |

The app is distributed as a sideloaded APK from GitHub. There is no Play Store
listing, payment flow, file upload, WebView, admin area, or server-side HTML
rendering.

The v1.9 and v1.9.1 sections below remain as baseline evidence for the release
line.

## 1.9.1 patch

The only code change is a bounded line count for the creative writing field
(`minLines = 4`, `maxLines = 8`) to prevent a Compose constraint overflow when a
saved draft is extremely long. The full draft remains in local preferences and
is not truncated. The patch adds no permissions, dependencies, network calls,
storage fields, or exported components, and it does not change authentication,
RLS, LiveKit, or backup behavior.

## Release under review

| Item | Value |
| --- | --- |
| Package | `com.lingo.app` |
| Version | `1.9.1` (`versionCode 11`) |
| APK | `app-release.apk`, 72,997,297 bytes |
| Published | [v1.9.1](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9.1) |
| CI | [Content audit 41923416312](https://github.com/hidezfzefz/LingoEnglish/actions/runs/41923416312) — pass |
| SHA-256 | `60dd5d0903448bc980ab1a836ea56dc58d8b70541323c3feeb2f728f61e76cba` |
| Signature | APK Signature Scheme v2, `CN=Android Debug` (see A-01) |
| Published asset | Downloaded `LingoEnglish-1.9.1.apk` from the v1.9.1 release: SHA-256, V2 certificate, size, and version match the tested APK. Installed with `adb install -r` over the existing 1.9.1 build; the signed-in session and 179 XP were preserved. |
| Minimum / target SDK | 26 / 37 |

The app is distributed as a sideloaded APK from GitHub. There is no Play Store listing,
payment flow, file upload, WebView, admin area, or server-side HTML rendering.

The v1.9 review below remains as the baseline evidence for the release line.

## What changed in v1.9

- **Launch cutscene.** A local Compose screen with a full 3.2 s sequence once per app
  version and a 1.2 s sting on later cold starts. It reads no learner data, makes no
  network call, plays no sound, requests no permission, and is skippable by tap or the
  **Skip intro** action.
- **Lesson intro.** A local screen shown once for a not-yet-completed lesson. It previews
  the lesson’s real first slide and offers a bundled creative prompt with two or three
  idea chips. Chips are local UI state; they are not persisted and do not create a new
  learner-data field.
- **Progress markers.** Two additive preferences: `launch_cutscene_version` and
  `lesson_intros_seen`. They contain a version code and lesson IDs only, live in ordinary
  app preferences, and are not sent to a server.
- **Routing.** All lesson entry points now go through one `openLesson()` wrapper so the
  one-time intro cannot drift from the real first step. Recovery links skip the launch
  and do not mark the version seen.
- **No new permissions, exported components, network endpoints, accounts, or dependencies.**
  The v1.9 APK keeps the v1.8 manifest surface.

## Attack surface

- **Public Android entry point:** the launcher activity. It also accepts
  `lingo://reset-password` recovery links.
- **Supabase Auth:** email/password sign-up and sign-in, sign-out, password-recovery
  email, session refresh.
- **Supabase PostgREST:** `SELECT` on seven RLS-protected tables plus `SECURITY DEFINER`
  RPCs for every write, matchmaking, blocking, reporting, ratings, and the weekly board.
- **Supabase Edge Functions:** `livekit-token` (requires a Supabase JWT) and
  `auth-callback` (a public post-confirmation page).
- **LiveKit Cloud:** audio-only rooms over WSS. Tokens are minted by the Edge Function;
  the LiveKit API secret never reaches the APK.
- **Local device:** progress and cutscene markers in ordinary preferences, the Supabase
  session in Keystore-backed storage, one optional reminder alarm, and notification
  intents.
- **Secrets:** the public Supabase URL/anon key are compiled into the APK by design. The
  service-role key, LiveKit API secret, SMTP password, and Supabase access token live only
  in `supabase/.env.local` and are not tracked.

The cutscene adds no attacker-controlled input, no deep-link handling beyond the existing
recovery path, no file or network access, and no new persisted personal data. It does not
change the login gate: the launch runs before the gate, and the gate still blocks all tab
content until a session exists.

## Stage 4 skill applicability

| Skill | Applied? | Reason |
| --- | --- | --- |
| `convio/web-security-review` | Yes | Used the secrets, dependencies, static-analysis, auth/session, input, data/privacy, and reporting steps. Web-only checks (CSP, cookies, CORS, payments, ZAP) do not apply to a native Compose app. |
| `mukul975/performing-android-app-static-analysis-with-mobsf` | Manual equivalent | MobSF’s image could not be pulled because the host resolver failed for the registry. The same checklist was applied directly: manifest components, debuggable/backup flags, hardcoded-secret scan, storage review, network-security config, and APK inspection. |
| `google/gke-platform-security` | No | No GKE, Kubernetes, or cloud cluster is part of Lingo. |
| `mukul975/performing-web-application-vulnerability-triage` | No | No DAST/web application findings to triage; Semgrep results were triaged manually. |
| `mukul975/implementing-web-application-logging-with-modsecurity` | No | No web server or ModSecurity/WAF deployment exists. |

## Findings and fixes

The v1.8 findings remain fixed and were rechecked against the v1.9 tree and APK:

| ID | Severity | Finding | Status in v1.9 |
| --- | --- | --- | --- |
| S-01 | High | Supabase refresh token stored in plaintext preferences. | Fixed in v1.8; `SecureSessionStorage` and backup exclusions unchanged. Session survived the v1.9 over-install without re-login. |
| S-02 | High | Broad table DML grants left RLS as the only barrier. | Fixed in v1.8; table grants and RLS policies unchanged. |
| S-03 | High | Direct profile updates bypassed `sync_profile` validation. | Fixed in v1.8; policy remains dropped. |
| S-04 | Medium | Internal `SECURITY DEFINER` helpers exposed as RPCs. | Fixed in v1.8; helper grants unchanged. |
| S-05 | Medium | LiveKit merged manifest added camera/foreground/screen-capture entries. | Fixed in v1.8; v1.9 merged manifest still has none. |
| S-06 | Medium | Sign-up metadata could place arbitrary text in profile fields. | Fixed in v1.8; validation constraints unchanged. |
| S-07 | Medium | Failed friend-code call leaked a raw PostgREST error. | Fixed in v1.8; `friendlyError` allow-list unchanged. |
| S-08 | Low | GitHub Actions pinned to mutable tags. | Fixed in v1.8; workflow pins unchanged. |

**No new v1.9 findings.** The new code was reviewed for intent injection, exported
components, unsafe intents, logging of learner data, and unbounded input. The cutscene
has no input field, no intent handling, and no logging. The creative chips are static
bundled strings selected in Compose state.

**No new v1.10 findings at code review.** The review queue was checked for
injection through preference data, unbounded decode, stale or unknown exercise
IDs, and duplicate entries; each has a bound or filter. The theme preference
was checked for unsafe enum parsing and falls back to Warm paper. Neither
feature logs learner data, handles intents, or adds a network path. The v1.8
findings S-01 to S-08 and the accepted risks A-01 to A-04 still apply; the
scanner results for the final v1.10 APK are recorded below.

## Scanner and manual evidence

| Check | Result |
| --- | --- |
| `gitleaks git` | 0 findings across 19 commits. |
| `gitleaks dir` | 11 findings, all in ignored build outputs, `local.properties`, or `supabase/.env.local`; none tracked, none in the APK. |
| Semgrep (`p/default` + `p/secrets`, Docker, 4 GB) | 1 warning: the expected exported launcher activity (A-04). No secrets or injection findings. |
| OSV `querybatch` | The v1.8 run found 0 vulnerabilities in 204 resolved Maven modules. v1.9 adds no dependencies, so the dependency set is unchanged. |
| Android lint | 0 errors, 42 warnings. Remaining warnings are KTX style suggestions, newer-dependency notices, and an obsolete-SDK notice. |
| Unit tests | 28 tests, 0 failures, including the new cutscene policy and content tests. |
| MobSF-equivalent Android static checks | Release manifest is not debuggable; `allowBackup=true` but both backup rule files exclude `lingo_secure_auth.xml` and the legacy prefs; no custom `TrustManager`/`HostnameVerifier`; no cleartext HTTP URL; only the expected exported launcher activity and DUMP-guarded profile receiver; no hardcoded secret material. MobSF itself was not run because the Docker image pull was blocked by the host resolver. |
| APK content scan | One JWT: the expected public Supabase anon token (`role=anon`). No service-role key, LiveKit secret, SMTP password, Supabase access token, database password, or private-key material. |
| Merged release manifest | Eight normal permissions (`INTERNET`, `ACCESS_NETWORK_STATE`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `BLUETOOTH`, `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`) plus the AndroidX dynamic-receiver permission; no camera, foreground-service, media-projection, or screen-capture entries. Only `MainActivity` and the DUMP-guarded profile installer receiver are exported. |
| 1.9.1 patch build | `:app:testDebugUnitTest` 28/28; `:app:lintRelease` 0 errors, 43 non-blocking warnings; release APK 72,997,297 bytes. |
| 1.9.1 signature / badging | APK Signature Scheme v2, `CN=Android Debug`, certificate SHA-256 `3888438c2652dff2a67d2953996eddc0b83ff8f6f4167a5161e6794c766c1df3`; version `1.9.1` (`11`), min SDK 26, target SDK 37. |
| 1.9.1 phone regression | Installed with `adb install -r` over the debug build; opened the real `u2-l1` creative step with the owner’s 2,089-line draft, checked it, finished the lesson, and reached `Lesson complete!` with no crash. The pre-test progress backup was restored afterward. |
| gitleaks git (v1.10) | 0 findings across 29 commits, including the two v1.10 feature commits and the docs commit. |
| gitleaks dir (v1.10) | 11 findings, all in ignored build outputs, `local.properties`, or `supabase/.env.local`; none tracked, none in the APK. The same 11 as v1.9. |
| OSV querybatch (v1.10) | 204 resolved Maven modules from `releaseRuntimeClasspath`, 0 vulnerabilities. |
| Semgrep v1.10 (`p/default` + `p/secrets`, Docker, 4 GB) | 1 warning: the expected exported launcher activity (A-04). No secrets or injection findings. 270 rules over 89 tracked files. |
| MobSF 4.6.5 (v1.10) | Security score 60. 1 high (debug certificate, A-01), 9 warnings. Triage below: no unaccepted high or medium app-code finding. |
| Android lint (v1.10) | `:app:lintDebug` 0 errors, 44 warnings; `:app:lintRelease` 0 errors, 45 warnings (37 `UseKtx`, 3 newer-dependency notices, 2 Compose width/height, 1 obsolete-SDK, 1 Gradle dependency, 1 AGP version). No security category. |
| APK content scan (v1.10) | One JWT: the expected public Supabase anon token (`role=anon`, project ref `sfhfoffubbqdfpcvpjer`). No service-role key, LiveKit secret, SMTP password, Supabase access token, database password, or private-key material. |
| Merged release manifest (v1.10) | The same eight normal permissions as v1.9 plus the AndroidX dynamic-receiver permission; no camera, foreground-service, media-projection, or screen-capture entries. Only `MainActivity` and the DUMP-guarded profile installer receiver are exported; no `debuggable` flag. |
| 1.10 build | `:app:testDebugUnitTest` 47/47; `:app:lintRelease` 0 errors, 45 warnings; release APK 73,046,449 bytes. |
| 1.10 signature / badging | APK Signature Scheme v2, `CN=Android Debug`, certificate SHA-256 `3888438c2652dff2a67d2953996eddc0b83ff8f6f4167a5161e6794c766c1df3`; version `1.10.0` (`12`), min SDK 26, target SDK 37. |
| 1.10 phone regression | Installed with `adb install -r` over the debug build; signed-in session, 296 XP, 4-day streak, 12 review items, and Warm paper preserved; release cold start 587 ms; no `DEBUGGABLE` flag. |
| v1.10 data-flow review | `ReviewQueue` validates IDs, counts, and timestamps; decode is capped at 200 lines and ignores malformed/duplicate entries; `due()` filters unknown IDs. `theme_mode` falls back to Warm paper on an unknown value. Neither path logs, handles intents, or calls the network. |
| Signature / badging (v1.9 baseline) | APK Signature Scheme v2, `CN=Android Debug`, certificate SHA-256 `3888438c2652dff2a67d2953996eddc0b83ff8f6f4167a5161e6794c766c1df3`; version `1.9` (`10`), min SDK 26, target SDK 37. |
| Published asset (v1.9 baseline) | Downloaded `LingoEnglish-1.9.apk` from the v1.9 release: SHA-256 matches `1d37c06a…`, V2 certificate matches, version is `1.9` (`10`), and size is 72,997,289 bytes. Reinstalled with `adb install -r` over the existing 1.9 build; version and local data were preserved. |
| Cutscene data flow review | `CutscenePolicy` reads a version code and lesson ID sets; `CutsceneContent` is a static map; `CutsceneScreen` has no network, storage, or permission calls. The only write is the local “seen” marker. |
| Recovery-link review | `lingo://reset-password` still skips the launch, is handled in memory, and does not mark the version seen. The custom-scheme risk remains A-02. |

## MobSF 4.6.5 triage (v1.10)

MobSF scanned the release APK and reported a security score of 60. The score is
driven by the accepted debug signing key, the Android 8.0 minimum, and the
intentional local backup. Each finding was checked against the source and the
APK:

| MobSF finding | Severity | Where | Triage |
| --- | --- | --- | --- |
| Application signed with debug certificate | High | APK signature | Accepted A-01. The debug key is required for update continuity in this sideload line; a managed release key remains the next signing migration. |
| App can be installed on Android 8.0 (minSdk 26) | Warning | Manifest | Accepted platform floor. Android 8.0 is the documented product requirement; the app targets API 37. |
| Application data can be backed up (`allowBackup=true`) | Warning | Manifest | Accepted A-05. The auth preferences and legacy auth file are excluded; local learning progress is backed up by design. |
| Exported `ProfileInstallReceiver` protected by `DUMP` | Warning | Manifest / AndroidX | Accepted A-04. `android.permission.DUMP` is a system-only signature permission; the receiver performs profile installation only. |
| Files may contain hardcoded sensitive information | Warning | Mostly bundled libraries; `SecureSessionStorage` | False positive. The flagged `SecureSessionStorage` string is the file name `lingo_secure_auth`; no credential is hardcoded. |
| IP address disclosure | Warning | Bundled SIP, Ktor, JNA libraries | False positive for app code. Lingo does not log or store learner IP addresses. |
| Insecure random number generator | Warning | Bundled NIST SIP library | False positive for app code. Session tokens and recovery tokens come from Supabase Auth; the app does not generate security tokens with `java.util.Random`. |
| SHA-1 is a weak hash | Warning | Bundled Ktor/SIP libraries | False positive for app code. No app auth, password, or data-integrity path uses SHA-1. |
| App creates a temp file | Warning | Bundled JNA library | False positive for app code. Lingo writes drafts and progress to preferences, not temp files. |
| App logs information | Info | Bundled libraries | No `Log`/`println` calls exist in the review, theme, or progress paths. |
| App copies data to clipboard | Info | `CallHubScreen` | The copied value is the learner’s friend code, which is meant to be shared. No token or account secret is copied. |
| This app may contain hardcoded secrets | Warning | APK strings | Triaged: the only JWT is the public Supabase anon token; the remaining matches are public library constants and test vectors. No service-role key, LiveKit secret, SMTP password, or private key. |
| No privacy trackers | Secure | APK | Confirmed: no analytics, ads, or crash SDK. |

MobSF’s “hardcoded secrets” list was checked value by value against the known
public anon JWT and library constants. The full APK scan found exactly one JWT
and no secret material.

## Open risks accepted for v1.10

| ID | Severity | Risk | Why it is open | Mitigation / next step |
| --- | --- | --- | --- | --- |
| A-01 | Medium | The release is signed with the local Android debug key (`CN=Android Debug`) instead of a protected release key. The key is not committed, but it is not stored to release-key standards. | Rotating the signing key would prevent 1.10 from installing over existing 1.9.1 builds and could strand learners’ local progress. The owner chose update continuity for this release. | Keep the keystore offline and backed up. Introduce a managed release key with an explicit migration/reinstall plan in a later version; do not lose the current key while updates are expected. |
| A-02 | Medium | Password recovery uses the custom scheme `lingo://reset-password`. Another installed app can register the same scheme and may receive the recovery link. Supabase recovery tokens remain valid for their normal lifetime (about one hour), so possessing the link again before expiry allows another password change even after it has been used once. | The Supabase project has no first-party HTTPS domain for Android App Links, and adding one would expand this release’s scope. The recovery token is kept in memory only and is never persisted by the app; it is discarded after a successful change, but the server-side token lifetime is unchanged. | Move recovery to a verified HTTPS App Link when a project domain is available, or exchange the link for a one-time server-side nonce. Users should ignore unexpected apps offered to open a recovery link and request a fresh link if one may have leaked. |
| A-03 | Low | Email confirmation is disabled (`mailer_autoconfirm=true`), so an address is not proven to belong to the signer. | Google Workspace SMTP migration is deferred; confirmation email delivery is not yet reliable. Auth email sending is rate-limited. | Re-enable confirmation after the SMTP migration, then test sign-up and recovery on a real device. |
| A-04 | Low | The launcher activity is exported and flagged by Semgrep; `androidx.profileinstaller.ProfileInstallReceiver` is also exported. | Android requires the launcher activity to be exported. The profile installer receiver is guarded by the system-only `android.permission.DUMP` permission. | No change. The activity opens the normal login-gated UI; it grants no privileged action. Re-check the merged manifest each release. |
| A-05 | Low | `android:allowBackup=true` lets a device owner with USB debugging extract local app data, including XP, streaks, review items, and writing drafts. | Android backup is how learners keep local progress across a device migration; disabling it would lose that benefit. The Supabase session and legacy auth file are excluded, so a backup cannot be replayed to sign in. | Keep the backup exclusions for `lingo_secure_auth.xml` and `com.lingo.app_preferences.xml`. Re-check the rules each release; consider a user-facing export instead of broad backup in a later version. |

## Verification limits

- Two-phone live-audio quality was not tested. The call signalling, LiveKit room join,
  mute, timer, End, rating, report, and block paths retain v1.8 evidence; real
  speaker/microphone quality remains a physical-device check.
- Fully signed-out testing on the physical phone was not run; the owner stays signed in.
  The signed-out gate retains v1.8 evidence and is covered by code review.
- Recovery email delivery was not tested because the test account uses an unroutable
  `example.com` address. App-side deep-link handling retains v1.8 evidence.
- No external penetration test or OWASP ZAP run was performed. ZAP targets web
  deployments; Lingo’s public web surface is limited to Supabase Auth and the static auth
  callback.
- MobSF 4.6.5 was run against the release APK in Docker. Its one high and nine
  warning findings are triaged above; none is an unaccepted app-code issue.
- Semgrep was run in the Docker image with 4 GB. The host binary was not used
  because earlier Kotlin-rule runs hit a memory fault.
- The Android 8.0 / API 26 runtime was not exercised; `minSdk` is verified by
  build and lint. The physical phone runs Android 16 / API 36.

## Conclusion

No critical findings remain. The eight v1.8 findings (S-01 to S-08) remain fixed and were
rechecked against the v1.9 tree and APK. The v1.9 cutscene and lesson-intro changes add no
new permissions, network paths, accounts, exported components, or learner-data fields.
Four risks remain accepted for v1.9 (A-01 to A-04), with the debug signing key and the
custom-scheme recovery link being the two medium items. Release SHA-256 is
`1d37c06a82d8f8ecb54e3e5ac9ff650929567d9bee63a4f759fb2764552cbaec`. The release is
published at [v1.9](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9); the
downloaded asset hash and signature match the reviewed APK. The signing-key migration is
planned for a later version.

The 1.9.1 patch changes only the creative field's line cap and adds no new permissions,
dependencies, network paths, storage fields, or exported components. Its release
SHA-256 is `60dd5d0903448bc980ab1a836ea56dc58d8b70541323c3feeb2f728f61e76cba`;
the local release-candidate build and the full phone regression are recorded above.

The v1.10 release adds two local preferences and a pure-UI theme. It adds no
permissions, dependencies, network endpoints, accounts, exported components, or
backend schema. The queue decoder is bounded and validates every field, and the
theme enum falls back safely. Gitleaks, OSV, Semgrep, Android lint, and MobSF
were run against the release candidate; the only open high finding is the
accepted debug signing key (A-01), and each MobSF warning is either an accepted
product choice or a bundled-library false positive. The release SHA-256 is
`9ea0ce05b867b07c4dc150bae8a7f0183e9a8883ede159eff77d058ddd20f1c1`; the
release-candidate build, scanner evidence, and the data-preserving over-install
are recorded above and in `TEST-REPORT.md`.
