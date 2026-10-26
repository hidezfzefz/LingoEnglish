# Security review — Lingo English 1.8

Reviewed: 2026-10-26  
Scope: the Android app, its Supabase schema/RLS/Edge Functions, LiveKit token issuing, and the v1.8 release APK.  
Reviewer: Lingo maintainers (self-review, no external penetration test).

## Release under review

| Item | Value |
| --- | --- |
| Package | `com.lingo.app` |
| Version | `1.8` (`versionCode 9`) |
| APK | `app-release.apk`, 72,948,079 bytes |
| Published | [v1.8 GitHub release](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.8) |
| CI | [Run 40412239645](https://github.com/hidezfzefz/LingoEnglish/actions/runs/40412239645) passed on `67ae40b` |
| SHA-256 | `0394199900e79bf2ebdf744c72725bf0334a8be73efde7235a31eac89d7b39b7` |
| Signature | APK Signature Scheme v2, `CN=Android Debug` (see A-01) |
| Minimum / target SDK | 26 / 37 |

The app is distributed as a sideloaded APK from GitHub. There is no Play Store listing, payment flow, file upload, WebView, admin area, or server-side HTML rendering.

## Attack surface

- **Public Android entry point:** the launcher activity. It also accepts `lingo://reset-password` recovery links.
- **Supabase Auth:** email/password sign-up and sign-in, sign-out, password-recovery email, session refresh.
- **Supabase PostgREST:** `SELECT` on seven RLS-protected tables plus `SECURITY DEFINER` RPCs for every write, matchmaking, blocking, reporting, ratings, and the weekly board.
- **Supabase Edge Functions:** `livekit-token` (requires a Supabase JWT) and `auth-callback` (a public post-confirmation page).
- **LiveKit Cloud:** audio-only rooms over WSS. Tokens are minted by the Edge Function; the LiveKit API secret never reaches the APK.
- **Local device:** progress in ordinary preferences, the Supabase session in Keystore-backed storage, one optional reminder alarm, and notification intents.
- **Secrets:** the public Supabase URL/anon key are compiled into the APK by design. The service-role key, LiveKit API secret, SMTP password, and Supabase access token live only in `supabase/.env.local` and are not tracked.

## Findings and fixes

| ID | Severity | Finding | Where | Fix | Status |
| --- | --- | --- | --- | --- | --- |
| S-01 | High | The Supabase refresh token was stored in plaintext in the default preferences file, readable by backup/root/debug access. | `CallStore.kt`, session storage | Added `SecureSessionStorage` using Android Keystore-backed `EncryptedSharedPreferences`; migrates the old key once, then clears the plaintext file; falls back to memory-only if the Keystore is unavailable. Both backup rule files now exclude `lingo_secure_auth.xml` and the legacy preferences file. | Fixed |
| S-02 | High | `anon` and `authenticated` held full table DML grants (`INSERT`, `UPDATE`, `DELETE`, `TRUNCATE`, …), leaving RLS as the only barrier. | `public` tables, Supabase grants | Revoked all table privileges from `anon` and `authenticated`, then granted only `SELECT` to `authenticated`. The app writes exclusively through RPCs. Verified live: direct `PATCH`/`INSERT` now return `403` / `42501`. | Fixed |
| S-03 | High | The `profiles update own` policy let a signed-in client write profile columns directly, bypassing `sync_profile` validation. | `public.profiles` RLS | Dropped the policy. The app already used the RPC, so no client behaviour changed. | Fixed |
| S-04 | Medium | Internal `SECURITY DEFINER` helpers were exposed as PostgREST RPCs. `is_active_user` and `is_blocked_pair` could disclose ban/block relationships; `touch_last_seen` and `expire_stale_calls` were never intended for clients. | `public` functions | Revoked `EXECUTE` from `PUBLIC`, `anon`, and `authenticated` on those four helpers. Kept `my_level()` executable because the weekly-board RLS policy needs it; it only reads the caller's level. Verified helper RPC now returns `403` / `42501`. | Fixed |
| S-05 | Medium | LiveKit's merged manifest added `CAMERA`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PROJECTION`, and `ScreenCaptureService` even though Lingo is audio-only. This widened permissions and exposed an unused screen-capture surface. | `AndroidManifest.xml` | Removed all three permissions and the service with manifest merger rules. Confirmed absent from the merged release manifest; audio permissions (`RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `BLUETOOTH_CONNECT`) remain. | Fixed |
| S-06 | Medium | Sign-up metadata could place arbitrary text in `avatar_sticker` or an unbounded string in `country`; the trigger copied it without validation. | `public.profiles`, `handle_new_user()` | Added a sticker-key constraint (`^[A-Z_]{1,24}$`), a 56-character country limit, and a trigger that trims/validates display name, sticker, and country before insert. Existing rows passed validation. | Fixed |
| S-07 | Medium | A failed friend-code call rendered the raw PostgREST/HTTP error in the call sheet. The message included the request URL and redacted `Authorization` metadata (token prefix and length), exposing backend details to the learner. | `CallStore.friendlyError()` | Replaced the fall-through raw message with a closed allow-list of known server conditions and short canonical text. Unknown failures now return “Something went wrong. Please try again.” Tested invalid code, own code, and blocked pair. | Fixed |
| S-08 | Low | Semgrep flagged four GitHub Actions steps pinned to mutable version tags, allowing a tag to be moved to different code after review. | `.github/workflows/content-audit.yml` | Pinned `actions/checkout`, `actions/setup-java`, `android-actions/setup-android`, and `gradle/actions/setup-gradle` to full commit SHAs with version comments. | Fixed |

### S-01 session storage evidence

- Encrypted file: `lingo_secure_auth.xml`, key alias managed by Android Keystore; session key `session`.
- Legacy key observed on-device: `sb-sfhfoffubbqdfpcvpjer-supabase-co-session` in `com.lingo.app_preferences.xml`.
- Over-install test: session survived an install over the previous build; the encrypted store loaded it, the legacy key disappeared, and the signed-in Home screen opened without re-authentication.
- If the Keystore cannot be opened, the app clears the plaintext copy and uses memory-only storage. The learner signs in again rather than leaving a usable refresh token on disk.
- Backup exclusions are present in both `backup_rules.xml` (Android 11 and below) and `data_extraction_rules.xml` (Android 12+ cloud backup and device transfer).

### S-02 / S-03 / S-04 database evidence

Live catalog checks after the migration:

- All seven public tables have RLS enabled.
- The only policies are `SELECT` policies; no client `INSERT`/`UPDATE`/`DELETE` policy remains.
- `authenticated` has only `SELECT` table grants; `anon` has none.
- `my_level()` retains `EXECUTE` for `authenticated`. `is_active_user`, `is_blocked_pair`, `touch_last_seen`, and `expire_stale_calls` are executable only by the owner and `service_role`.
- A temporary account called the normal RPCs (`my_profile`, `sync_profile`, `queue_size`, `claim_match`, `leave_call_queue`, `my_call_state`, `weekly_leaderboard`, `submit_weekly_score`) successfully, while direct profile update, direct queue insert, and helper RPC calls were denied with `403` / `42501`. The account was deleted after the test.
- The migration is applied as `20261026000000_security_hardening` and is tracked in Supabase migration history.

### S-07 error-message evidence

The original unknown-code reply showed the PostgREST request URL plus `Headers: {Authorization=[Bearer ey... (len=968)] ...}`. After the fix, the same flow shows only:

- `No learner has that friend code.`
- `That is your own friend code.`
- `This learner is blocked.`

The mapping is an allow-list; it never echoes the exception. Unit/lint builds pass after the change.

### S-08 workflow evidence

All four `uses:` references now point at the commit SHA behind the reviewed tag (for example, `gradle/actions/setup-gradle@3f5f9ada… # v6`). A fresh Semgrep run reports only the accepted exported-launcher warning.

## Open risks accepted for v1.8

| ID | Severity | Risk | Why it is open | Mitigation / next step |
| --- | --- | --- | --- | --- |
| A-01 | Medium | The release is signed with the local Android debug key (`CN=Android Debug`) instead of a protected release key. The key is not committed, but it is not stored to release-key standards. | Rotating the signing key would prevent v1.8 from installing over existing v1.7/v1.8 builds and could strand learners' local progress. The owner chose update continuity for this release. | Keep the keystore offline and backed up. Introduce a managed release key with an explicit migration/reinstall plan in a later version; do not lose the current key while updates are expected. |
| A-02 | Medium | Password recovery uses the custom scheme `lingo://reset-password`. Another installed app can register the same scheme and may receive the recovery link. Supabase recovery tokens remain valid for their normal lifetime (about one hour), so possessing the link again before expiry allows another password change even after it has been used once. | The Supabase project has no first-party HTTPS domain for Android App Links, and adding one would expand this release's scope. The recovery token is kept in memory only and is never persisted by the app; it is discarded after a successful change, but the server-side token lifetime is unchanged. | Move recovery to a verified HTTPS App Link when a project domain is available, or exchange the link for a one-time server-side nonce. Users should ignore unexpected apps offered to open a recovery link and request a fresh link if one may have leaked. |
| A-03 | Low | Email confirmation is disabled (`mailer_autoconfirm=true`), so an address is not proven to belong to the signer. | Google Workspace SMTP migration is deferred; confirmation email delivery is not yet reliable. Auth email sending is rate-limited. | Re-enable confirmation after the SMTP migration, then test sign-up and recovery on a real device. |
| A-04 | Low | The launcher activity is exported and flagged by Semgrep; `androidx.profileinstaller.ProfileInstallReceiver` is also exported. | Android requires the launcher activity to be exported. The profile installer receiver is guarded by the system-only `android.permission.DUMP` permission. | No change. The activity opens the normal login-gated UI; it grants no privileged action. Re-check the merged manifest each release. |

## Scanner and manual evidence

| Check | Result |
| --- | --- |
| `gitleaks git` (13-commit tracked history) | 0 findings. |
| `gitleaks dir` (working tree) | 11 findings, all in ignored build outputs, `local.properties`, or `supabase/.env.local`; none tracked, none in the APK. |
| OSV `querybatch` (204 resolved Maven modules) | 0 known vulnerabilities. |
| Semgrep (`p/default` + `p/secrets`, Docker) | 1 warning after S-08: exported launcher activity (A-04). No secrets or injection findings. |
| Android lint | 0 errors, 39 warnings. Remaining warnings are KTX style suggestions, newer-dependency notices, and an obsolete-SDK notice. |
| APK content scan | One JWT: the expected public Supabase anon token (`role=anon`). No service-role key, LiveKit secret, SMTP password, Supabase access token, database password, or private key material. |
| Merged release manifest | Eight normal permissions (`INTERNET`, `ACCESS_NETWORK_STATE`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `BLUETOOTH`, `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`) plus the AndroidX dynamic-receiver permission; no camera, foreground-service, media-projection, or screen-capture entries. Only `MainActivity` and the DUMP-guarded profile installer receiver are exported; reminders and the startup provider are not. |
| Signature / badging | APK Signature Scheme v2, `CN=Android Debug`, certificate SHA-256 `3888438c2652dff2a67d2953996eddc0b83ff8f6f4167a5161e6794c766c1df3`; version `1.8` (`9`), min SDK 26, target SDK 37. |
| Auth/session code review | Password recovery tokens stay in memory and are cleared after use; sign-in errors do not reveal whether an email exists; the UI is gated until a session exists. |
| Edge Function review | `livekit-token` requires a valid Supabase JWT, confirms the caller is a participant in a live session, checks bans, and returns a short-lived room token. `auth-callback` is a static public page only. |
| Notification review | All `PendingIntent`s are immutable; the reminder action is package-scoped; tapping a reminder still passes the login gate. |
| Transport | Supabase and LiveKit use HTTPS/WSS; cleartext traffic is not enabled. No certificate pinning is used, so the platform trust store applies. |

## Verification limits

- Two-phone live-audio quality was not tested. The call signalling, LiveKit room join, mute, timer, End, rating, report, and block paths were exercised with a second headless participant; real speaker/microphone quality remains a physical-device check.
- Fully signed-out testing on the physical phone is still pending; the published build
  was installed over 1.7 with the owner's session preserved, then navigated
  Home/Practice/Call/Progress on the phone and emulator.
- Recovery email delivery was not tested because the test account uses an unroutable `example.com` address. The in-app deep-link handling, mismatch validation, successful password change, old-password rejection, new-password sign-in, invalid-link message, and link reuse before expiry were exercised with a server-generated recovery link.
- No external penetration test or OWASP ZAP run was performed. ZAP targets web deployments; Lingo's public web surface is limited to Supabase Auth and the static auth callback.

## Conclusion

No critical findings remain. The eight findings above (S-01 to S-08) were fixed and verified, including all high-severity items. Four risks remain accepted for v1.8 (A-01 to A-04), with the debug signing key and the custom-scheme recovery link being the two medium items. Release SHA-256 is `0394199900e79bf2ebdf744c72725bf0334a8be73efde7235a31eac89d7b39b7`. The release is approved for GitHub distribution on that basis, with the signing-key migration planned for a later version.
