# Launch checklist — Lingo English 1.9

- Reviewed: 2026-11-03
- Release: `1.9` (`versionCode 10`), APK signed with the project debug key
- Distribution: public GitHub Release, sideloaded APK (no Play Store listing)
- Status: **published** — CI green, tag `v1.9`, and release assets published and verified

This checklist is adapted from `convio/launch-checklist` for an Android app.
Web-only items are marked **N/A** with the reason, rather than left implicit.

## Deploy

| Item | Status | Evidence / notes |
| --- | --- | --- |
| Production build passes with understood warnings | **Done** | `:app:testDebugUnitTest` 28/28; `:app:lintDebug` 0 errors, 42 non-blocking warnings; release APK `1d37c06a…`, 72,997,289 bytes. |
| Live release published and smoke-tested | **Done** | [Lingo English 1.9](https://github.com/hidezfzefz/LingoEnglish/releases/tag/v1.9) published 2026-11-03. CI [41730852766](https://github.com/hidezfzefz/LingoEnglish/actions/runs/41730852766) is green on `94e4c78`. The downloaded `LingoEnglish-1.9.apk` matches SHA-256 `1d37c06a…`, V2 certificate `3888438c…`, version `1.9` (`10`), and 72,997,289 bytes. The same APK was installed over the phone’s debug build with session and 133 XP / 3 lessons preserved. |
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
4. **Debug signing key.** Accepted for v1.9 to preserve update continuity. A dedicated
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

## Version 2 candidates

- Branded Google Workspace SMTP and confirmation on by default.
- Dedicated release keystore and Play internal testing if distribution grows.
- Verified HTTPS App Link for password recovery (closes accepted risk A-02).
- Two-phone and fully signed-out QA on a fixed device matrix.
- Optional richer cutscene variants, kept local, silent, and skippable.
