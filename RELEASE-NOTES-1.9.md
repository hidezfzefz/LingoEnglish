# Lingo English 1.9

Released 2026-11-03. Android 8.0 (API 26) or newer.

## A warmer start

- A branded 3.2-second launch sequence on the first cold start after install or update.
- A 1.2-second logo-and-path sting on later cold starts.
- Tap anywhere or choose **Skip intro** to go straight in.
- Reduced-motion mode shows a still frame and moves on quickly.
- Password-recovery links skip the launch and do not consume the full sequence.

## A thoughtful lesson entry

- A once-per-lesson intro for lessons you have not completed.
- It previews the real first step, so **Begin first step** opens the lesson at step 1.
- A lesson-specific creative prompt with two or three tappable idea chips.
- Chips reveal short examples only — no typing, no answer field, and no new saved data.
- Backing out of the intro does not replay it; completed lessons open directly.

## Preserved from 1.8

- Every lesson, weekly test, level exam, practice workout, and live-call feature.
- Your signed-in session, XP, level, streak, completed lessons, writing drafts, daily
  goal, and reminder.
- Version 1.9 (`versionCode 10`) installs over 1.8 with `adb install -r`; no uninstall or
  data clear is needed.

## Install

```bash
adb install -r LingoEnglish-1.9.apk
sha256sum -c LingoEnglish-1.9.apk.sha256
```

APK SHA-256:
`1d37c06a82d8f8ecb54e3e5ac9ff650929567d9bee63a4f759fb2764552cbaec`
(72,997,289 bytes).

The APK is signed with the project debug key for update continuity; see accepted risk
A-01 in `SECURITY-REVIEW.md`. The build introduces no new permissions, network calls,
accounts, or learner-data fields.

## Verification

- 28 unit tests pass, including launch policy and lesson-spark coverage.
- Android lint: 0 errors.
- Full launch, sting, tap-skip, reduced motion, 200% font, TalkBack semantics, lesson
  intro, chips, Begin routing, back/reopen, completed-lesson direct entry, offline launch,
  and data-preserving over-install were exercised across the emulator and the physical
  phone.
- `gitleaks git` is clean; Semgrep reports only the expected exported launcher activity;
  the APK contains only the public Supabase anon JWT.
