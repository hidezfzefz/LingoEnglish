# Security review — Lingo English 1.9

Reviewed: 2026-11-03  
Scope: the Android app, its Supabase schema/RLS/Edge Functions, LiveKit token issuing, and the v1.9 release APK.  
Reviewer: Lingo maintainers (self-review, no external penetration test).

## Release under review

| Item | Value |
| --- | --- |
| Package | `com.lingo.app` |
| Version | `1.9` (`versionCode 10`) |
| APK | `app-release.apk`, 72,997,289 bytes |
| Published | [v1.9](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9) |
| CI | [Content audit 41730852766](https://github.com/hidezfzefz/LingoEnglish/actions/runs/41730852766) — pass |
| SHA-256 | `1d37c06a82d8f8ecb54e3e5ac9ff650929567d9bee63a4f759fb2764552cbaec` |
| Signature | APK Signature Scheme v2, `CN=Android Debug` (see A-01) |
| Minimum / target SDK | 26 / 37 |

The app is distributed as a sideloaded APK from GitHub. There is no Play Store listing,
payment flow, file upload, WebView, admin area, or server-side HTML rendering.

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
| Signature / badging | APK Signature Scheme v2, `CN=Android Debug`, certificate SHA-256 `3888438c2652dff2a67d2953996eddc0b83ff8f6f4167a5161e6794c766c1df3`; version `1.9` (`10`), min SDK 26, target SDK 37. |
| Published asset | Downloaded `LingoEnglish-1.9.apk` from the v1.9 release: SHA-256 matches `1d37c06a…`, V2 certificate matches, version is `1.9` (`10`), and size is 72,997,289 bytes. |
| Cutscene data flow review | `CutscenePolicy` reads a version code and lesson ID sets; `CutsceneContent` is a static map; `CutsceneScreen` has no network, storage, or permission calls. The only write is the local “seen” marker. |
| Recovery-link review | `lingo://reset-password` still skips the launch, is handled in memory, and does not mark the version seen. The custom-scheme risk remains A-02. |

## Open risks accepted for v1.9

| ID | Severity | Risk | Why it is open | Mitigation / next step |
| --- | --- | --- | --- | --- |
| A-01 | Medium | The release is signed with the local Android debug key (`CN=Android Debug`) instead of a protected release key. The key is not committed, but it is not stored to release-key standards. | Rotating the signing key would prevent v1.9 from installing over existing v1.8 builds and could strand learners’ local progress. The owner chose update continuity for this release. | Keep the keystore offline and backed up. Introduce a managed release key with an explicit migration/reinstall plan in a later version; do not lose the current key while updates are expected. |
| A-02 | Medium | Password recovery uses the custom scheme `lingo://reset-password`. Another installed app can register the same scheme and may receive the recovery link. Supabase recovery tokens remain valid for their normal lifetime (about one hour), so possessing the link again before expiry allows another password change even after it has been used once. | The Supabase project has no first-party HTTPS domain for Android App Links, and adding one would expand this release’s scope. The recovery token is kept in memory only and is never persisted by the app; it is discarded after a successful change, but the server-side token lifetime is unchanged. | Move recovery to a verified HTTPS App Link when a project domain is available, or exchange the link for a one-time server-side nonce. Users should ignore unexpected apps offered to open a recovery link and request a fresh link if one may have leaked. |
| A-03 | Low | Email confirmation is disabled (`mailer_autoconfirm=true`), so an address is not proven to belong to the signer. | Google Workspace SMTP migration is deferred; confirmation email delivery is not yet reliable. Auth email sending is rate-limited. | Re-enable confirmation after the SMTP migration, then test sign-up and recovery on a real device. |
| A-04 | Low | The launcher activity is exported and flagged by Semgrep; `androidx.profileinstaller.ProfileInstallReceiver` is also exported. | Android requires the launcher activity to be exported. The profile installer receiver is guarded by the system-only `android.permission.DUMP` permission. | No change. The activity opens the normal login-gated UI; it grants no privileged action. Re-check the merged manifest each release. |

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
- The host Semgrep binary hit a memory fault on the Kotlin rules; the full scan was run in
  the Docker image with 4 GB and completed successfully.

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
