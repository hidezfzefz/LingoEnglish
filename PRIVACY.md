# Privacy — Lingo English 1.8

Last updated: 2026-10-26

Lingo is an open-source Android app for learning English. This note explains
what the app stores, what leaves the phone, and how to ask for deletion. It
covers the GitHub-distributed build; a future store build will link to the same
policy.

## What stays on your phone

- Lessons completed, XP, streak, active days, CEFR level, weekly scores,
  saved words, sticker choices and creative-writing drafts.
- Your daily goal and reminder time. Reminders are local Android alarms and
  never send data to a server.
- Your Supabase session token, encrypted with an Android Keystore-backed key.
  It is excluded from Android backups.

You can erase this local data from **Settings → Reset progress** inside the app.
That does not delete your account.

## What is sent to Supabase

If you create an account, Supabase Auth receives your email address and a
password hash. Lingo's database stores:

- your display name, friend code, CEFR level, avatar sticker, optional country,
  rating and call counters;
- weekly-test scores;
- call session metadata (participants, timestamps, status and end reason);
- ratings, reports and blocks you submit for safety.

Lingo uses Supabase as its account and database provider. Supabase's own privacy
terms apply to its infrastructure.

## Microphone and live calls

Live calls need microphone access. Audio is transmitted in real time through
LiveKit Cloud to the other participant. Lingo does not record calls, does not
store audio, and does not use the microphone outside an active call. LiveKit's
privacy terms apply to the relay service.

## What Lingo does not do

- No advertising, analytics, tracking SDKs or sale of personal data.
- No access to contacts, photos, files, location, camera or screen capture.
- No background reading of your microphone.
- No email marketing.

## Retention and deletion

Local progress stays until you reset it or uninstall the app. Account, profile,
call-metadata, rating, report and block rows stay while the account exists.
To delete your account and related server data, open an issue at
<https://github.com/hidezfzefz/LingoEnglish/issues> from the address or with the
account details needed to identify it. Deletion requests are handled manually;
the maintainer will confirm when the Auth user and related rows are removed.

## Children

Lingo is intended for learners aged 13 and over. It is not directed at younger
children, and accounts should not be created for them.

## Changes and contact

This policy is versioned with the app. Material changes will be noted in the
release notes. Questions and deletion requests:
<https://github.com/hidezfzefz/LingoEnglish/issues>.
