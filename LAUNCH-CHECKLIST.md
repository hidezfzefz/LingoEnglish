# Launch checklist — Lingo English 1.10

- Reviewed: 2026-11-07
- Release: `1.10.0` (`versionCode 12`), APK signed with the project debug key
- Distribution: public GitHub Release, sideloaded APK (no Play Store listing)
- Status: **shipped** — v1.10.0 published and verified on 2026-11-07

## 1.10 release

| Item | Status | Evidence / notes |
| --- | --- | --- |
| Smart Review and Night complete | **Done** | Smart Review queue, session, Home/Practice/quiz-result entry points, and the Warm paper/Night appearance setting are implemented and walked on the physical phone. The queue clears only after two consecutive correct answers; review XP uses the quiz formula and counts toward daily XP, goal, streak, and active days. |
| Unit tests and lint | **Done** | `:app:testDebugUnitTest` 47/47; `:app:lintDebug` 0 errors, 44 warnings (all pre-existing `UseKtx` suggestions; no new category). |
| Release build and artifact | **Done** | `:app:assembleRelease` successful; `app-release.apk` is 73,046,449 bytes with SHA-256 `9ea0ce05b867b07c4dc150bae8a7f0183e9a8883ede159eff77d058ddd20f1c1`. |
| Phone QA and data preservation | **Done** | Release APK installed over the debug build with `adb install -r`; signed-in session, 296 XP, 4-day streak, 12 queued review items, completed lessons, drafts, reminders, daily goal, exam results, and weekly scores preserved. Final release cold start 587 ms (`am start -W`); offline 1,963 ms. The pre-test backup was restored byte-for-byte after the Night/reset probes. |
| Privacy and permissions unchanged | **Done** | No new permissions, camera, location, payments, network endpoints, backend schema, or exported components. Review state and appearance choice stay in local preferences. `PRIVACY.md` still describes the data accurately; the review queue contains exercise IDs and counters only, and the appearance choice is a device preference. |
| CI and tag | **Done** | [Content audit 42408086000](https://github.com/hidezfzefz/LingoEnglish/actions/runs/42408086000) is green on `83f1153`; annotated tag `v1.10.0` points to that commit. |
| Public release published and smoke-tested | **Done** | [v1.10.0](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.10.0) published with `LingoEnglish-1.10.0.apk` (73,046,449 bytes) and `.sha256`. The downloaded asset’s SHA-256 `9ea0ce05…`, V2 certificate `3888438c…`, size, and version match the tested APK. It installed with `adb install -r` over the existing 1.10.0 build; 296 XP, 4-day streak, 12 review items, Warm paper, and the signed-in session were preserved; cold start 399 ms. |
| Release page smoke test | **Done** | Convio browser check: HTTP 200 at 390 and 1440 px; title and release notes rendered, no horizontal overflow. GitHub’s own `payloads.json` 404 and two `<h1>` elements are platform artifacts, not app defects. |
| Handover | **Done** | README covers download, build, backend configuration, secrets, release/rollback, and accounts; test and security reports are linked; open items are listed below. |

The v1.9.1 and v1.9 sections below remain as baseline evidence for the release line.

## 1.9.1 patch

| Item | Status | Evidence / notes |
| --- | --- | --- |
| A2 Travel finish crash fixed | **Done** | The owner’s `u2-l1` creative draft is 112,356 characters across 2,089 lines. The unbounded `OutlinedTextField` measured 262,994 px, above Compose’s 262,143 px constraint limit. `ExerciseView.kt` now caps the field at 8 visible lines with internal scrolling; the full draft is preserved. |
| Patch build and phone QA | **Done** | `:app:testDebugUnitTest` 28/28; `:app:lintRelease` 0 errors, 43 non-blocking warnings. Debug and release candidates both opened the real creative step with the 2,089-line draft, checked it, finished the lesson, and reached `Lesson complete!` with `+35 XP` / `4/4`; no `FATAL EXCEPTION` in logcat. |
| Owner data preserved | **Done** | The pre-test progress backup was restored byte-for-byte (SHA-256 `0d7b986e…`). Final phone state: release `1.9.1` (`11`), signed-in session, 1-day streak, 179 XP, and `u2-l1` still incomplete. |
| Patch artifact verified | **Done** | Published at [v1.9.1](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9.1); CI [41923416312](https://github.com/hidezfzefz/LingoEnglish/actions/runs/41923416312) is green on `2c97acf`. The downloaded `LingoEnglish-1.9.1.apk` matches SHA-256 `60dd5d09…`, V2 certificate `3888438c…`, version `1.9.1` (`11`), and 72,997,297 bytes. It installed with `adb install -r` over the existing 1.9.1 build; cold start 1.470 s, with 179 XP and the signed-in session preserved. |

The v1.9 checklist below remains as the baseline evidence for the release line.
Web-only items are marked **N/A** with the reason, rather than left implicit.

## Deploy

| Item | Status | Evidence / notes |
| --- | --- | --- |
| Production build passes with understood warnings | **Done** | `:app:testDebugUnitTest` 28/28; `:app:lintDebug` 0 errors, 42 non-blocking warnings; release APK `1d37c06a…`, 72,997,289 bytes. |
| Live release published and smoke-tested | **Done** | [Lingo English 1.9](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9) published 2026-11-03. CI [41730852766](https://github.com/hidezfzefz/LingoEnglish/actions/runs/41730852766) is green on `94e4c78`. The downloaded `LingoEnglish-1.9.apk` matches SHA-256 `1d37c06a…`, V2 certificate `3888438c…`, version `1.9` (`10`), and 72,997,289 bytes. The same APK was installed over the phone’s debug build with session and 133 XP / 3 lessons preserved. Post-release check (2026-11-04): the published asset was re-downloaded, hash-verified, and installed with `adb install -r` over the existing 1.9 build; version `1.9` (`10`), 1.079 s cold start, and 179 XP plus the signed-in session preserved. |
| Secrets on host; production keys distinct; nothing secret in client | **Done** | Only the public Supabase `anon` JWT is in the APK. Service-role, LiveKit secret, SMTP password and database password stay in `supabase/.env.local`. There is one Supabase project rather than separate staging/production projects; the anon key is public by design and RLS/RPCs enforce access. |
| Database migrations applied; backup/PITR enabled | **Done, PITR not enabled** | Migration `20261026000000_security_hardening` remains in Supabase migration history. Physical backups (`walg_enabled=true`) are on; point-in-time recovery is off on the current plan. v1.9 adds no migration. |
| Domain, HTTPS, canonical host, HSTS | **N/A** | No web deployment. APK and release assets are served by GitHub over HTTPS. |
| Preview/staging not indexed | **N/A** | No preview web deployment. |

## Search and sharing

| Item | Status | Evidence / notes |
| --- | --- | --- |
| Unique titles, descriptions, canonical URL, `lang` | **N/A** | Native Android app; no HTML pages. |
| Open Graph / Twitter cards | **N/A** | No social web pages. GitHub renders repository metadata. |
| `robots.txt`, sitemap, structured data | **N/A** | No website. |
| Icons and a useful 404 | **Done / N/A** | Launcher and notification icons are bundled and checked by Android lint. A 404 page does not apply; GitHub owns release-URL errors. |

## Commerce

| Item | Status | Evidence / notes |
| --- | --- | --- |
| Live payments, webhooks, tax, receipts, returns | **N/A** | Lingo is free, has no purchases, and accepts no payments. |

## Email

| Item | Status | Evidence / notes |
| --- | --- | --- |
| SPF, DKIM, DMARC; transactional mail arrives | **Partial, accepted** | Supabase SMTP login to `smtp.gmail.com:587` is verified. Sending uses a `gmail.com` address, so SPF/DKIM/DMARC are Google-managed. The branded Google Workspace sender migration is deferred and email confirmation remains off (`mailer_autoconfirm=true`). Recovery-link handling was tested with a server-generated link; real delivery to a routable mailbox is still an owner check. |

## Legal and privacy

| Item | Status | Evidence / notes |
| --- | --- | --- |
| Privacy policy and terms | **Done** | [PRIVACY.md](PRIVACY.md) documents local progress, cutscene markers, account data, call audio, retention and deletion. The app has no paid terms, so a separate terms page is not required for this free build. |
| Cookie consent | **N/A** | No cookies, WebView or web analytics in the app. |
| Contact / imprint | **Done** | GitHub Issues on the public repository is the support and privacy contact. |

## Observability

| Item | Status | Evidence / notes |
| --- | --- | --- |
| Error tracking and uptime alerts | **N/A by design** | No analytics, ads or third-party crash SDK is bundled. Supabase and LiveKit provide managed service status. Android system crash reporting and GitHub Issues are the support path. |
| Analytics respects consent | **N/A** | No analytics is installed. |

## Store and distribution

| Item | Status | Evidence / notes |
| --- | --- | --- |
| Store listing, screenshots, privacy labels | **N/A** | Sideloaded GitHub release, not a Play Store listing. README carries the download and verification instructions. |
| TestFlight / Play internal testing | **N/A** | Android-only sideload. The release APK installed over the phone’s debug build with data preserved. |
| Signed release artifact and checksum | **Done** | V2 signature `CN=Android Debug`, cert SHA-256 `3888438c…`; SHA-256 `1d37c06a82d8f8ecb54e3e5ac9ff650929567d9bee63a4f759fb2764552cbaec` published beside the APK after the release. Debug signing is accepted risk A-01 in `SECURITY-REVIEW.md`. |

## Handover

| Item | Status | Evidence / notes |
| --- | --- | --- |
| README: run, deploy, secrets, rollback, accounts | **Done** | [README.md](README.md) and [BUILDING.md](BUILDING.md); backend details in [supabase/README.md](supabase/README.md). |
| Test and security reports linked, open items stated | **Done** | [TEST-PLAN.md](TEST-PLAN.md), [TEST-REPORT.md](TEST-REPORT.md), [SECURITY-REVIEW.md](SECURITY-REVIEW.md). |
| Live release and verification evidence | **Done** | CI [41730852766](https://github.com/hidezfzefz/LingoEnglish/actions/runs/41730852766); tag `v1.9`; release `https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9`; downloaded asset hash, V2 signature, size, and version verified; over-install evidence above. |
| User knows first-week checks and version 2 work | **Done** | Final handover below. |

## Open at launch

1. **Physical-phone reminder tap and signed-out pass.** The phone stays signed in as the
   owner, so a fully signed-out journey was not run. The 00:29 reminder is preserved from
   v1.8; its on-phone tap remains an owner check. Both are owner actions, not code gaps.
2. **Two-phone audio quality.** Signalling, room join, mute, timer, rating, report and
   block retain v1.8 evidence. Speaker/microphone quality and echo still need two real
   phones.
3. **Email confirmation and branded SMTP.** Confirmation stays off until the Google
   Workspace migration is completed. Recovery flow is tested app-side.
4. **Debug signing key.** Accepted for v1.10 to preserve update continuity. A dedicated
   release keystore is the next signing migration.
5. **No point-in-time recovery.** Daily physical backups are enabled; PITR is not. Export
   important data before risky migrations.
6. **Physical-phone accessibility settings.** Font scale, reduced motion, and TalkBack
   were covered on the emulator because the owner’s phone settings must not be changed.

## First week

- Watch GitHub Issues for install and sign-in reports.
- Check Supabase Auth logs for unusual sign-up or recovery traffic.
- Check LiveKit usage and call completion counters.
- Confirm the phone’s 00:29 reminder is delivered and opens Practice.
- Re-check the published SHA-256 before announcing future releases.
- Spot-check that the full launch appears once after the update and the sting on later
  cold starts; confirm a completed lesson still opens directly.
- Spot-check that a missed question joins Smart Review, clears after two consecutive
  correct answers, and that Night remains selected after a learning reset.

## Version 2 candidates

- Branded Google Workspace SMTP and confirmation on by default.
- Dedicated release keystore and Play internal testing if distribution grows.
- Verified HTTPS App Link for password recovery (closes accepted risk A-02).
- Two-phone and fully signed-out QA on a fixed device matrix.
- Optional richer cutscene variants, kept local, silent, and skippable.
