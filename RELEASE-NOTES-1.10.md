# Lingo English 1.10

Released 2026-11-06. Android 8.0 (API 26) or newer.

## Smart Review

- Questions you miss in a lesson, practice workout, or weekly test join a local
  review queue. A miss adds the question or resets its streak. One correct
  answer keeps it queued; two correct answers in a row clear it. A later miss
  starts it again.
- A review session takes up to five questions from the queue, drawing only on
  the 83 bundled auto-scorable questions. Creative **Create** tasks are never
  queued and are never auto-scored.
- The session reuses the app's existing question UI, check/continue flow, and
  result screen. There is a friendly empty state when nothing is due.
- Review XP matches the quiz economy: 4 XP per correct answer, plus a 10 XP
  bonus when the session score is 80% or better. It counts toward total XP,
  the daily XP goal, the streak, and active days. It does not change quiz
  accuracy, perfect-quiz count, `todayQuizzes`, or the daily quick challenge.
- Entry points: a Home card that appears only while questions are due, a
  **Tricky questions** card on Practice with the live count, and a **Review
  tricky questions** link on a quiz result after a miss. All three open the
  same local session.
- The queue is stored on the device. It survives restarts and app updates and
  is cleared by **Reset progress**. Review works in airplane mode.

## Night appearance

- Settings now has an **Appearance** section with **Warm paper** and **Night**.
  Warm paper is the default and keeps the original look.
- Night keeps the existing type, stickers, shapes, and layout, and swaps the
  paper/forest palette for a dark forest background, light text, lime brand
  fills, and dark ink on coral controls.
- The choice applies immediately to every screen and to the status and
  navigation bar icons, including the Settings sheet. It survives restarts.
  **Reset progress** leaves the appearance choice untouched because it is a
  display preference, not learning progress.

## Preserved from 1.9.1

- Every lesson, weekly test, level exam, practice workout, word bank,
  flashcard, roleplay, sentence-tile, and live-call feature.
- Your signed-in session, XP, level, streak, completed lessons, writing
  drafts, daily goal, reminder, exam results, weekly scores, and local progress.
- The v1.9 launch cutscene, lesson intros, creative chips, and replay rules.
- The 1.9.1 fix for very long saved **Create** drafts.
- Version 1.10 (`versionCode 12`) installs over 1.9.1 with `adb install -r`;
  no uninstall or data clear is needed.

## Install

```bash
adb install -r LingoEnglish-1.10.0.apk
sha256sum -c LingoEnglish-1.10.0.apk.sha256
```

APK SHA-256:
`9ea0ce05b867b07c4dc150bae8a7f0183e9a8883ede159eff77d058ddd20f1c1`
(73,046,449 bytes).

The APK is signed with the project debug key for update continuity; see
accepted risk A-01 in `SECURITY-REVIEW.md`. The build introduces no new
permissions, network calls, accounts, backend schema, or learner-data
collection. Review state and the appearance choice stay local.

## Verification

- 47 unit tests pass, including the two-right review rule, queue
  encode/decode limits, XP formula, theme palette contrast, and content
  coverage.
- Android lint: 0 errors, 44 warnings (all pre-existing `UseKtx` suggestions).
- Night and Warm paper were walked on the physical phone across Home, Learn,
  lesson, lesson quiz, Practice, Real Talk, Word Tiles, Flashcards, Word bank,
  Call, Progress, Settings, level path, and the weekly test session.
- Smart Review was exercised end to end on the phone: two-right clearing,
  wrong-answer reset, XP and streak updates, Home/Practice/quiz-result entry
  points, empty state, force-stop persistence, and Back returning to the
  preserved quiz result.
- The release APK over-installed on the physical phone with the signed-in
  session and owner data intact; cold start measured 892 ms.
