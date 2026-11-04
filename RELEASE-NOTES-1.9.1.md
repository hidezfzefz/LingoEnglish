# Lingo English 1.9.1

This patch fixes the crash reported when finishing the A2 Travel lesson **At the
Airport** with a very long saved **Create** draft.

## What was wrong

The lesson restores the learner’s saved writing when the creative step opens.
The writing field grew to fit every line. On the owner’s phone, the saved
`u2-l1` draft is 112,356 characters across 2,089 lines, so the field measured
262,994 px high. Compose can represent at most 262,143 px in a layout
constraint, and the mismatch crashed the app with:

```text
java.lang.IllegalArgumentException: Can't represent a width of 962 and height of 262994 in Constraints
```

## What changed

- The creative writing field now shows at least 4 and at most 8 lines, and
  scrolls internally for longer drafts.
- The full saved draft is kept and remains editable. Nothing is truncated,
  migrated, or cleared.
- The fix is limited to `app/src/main/java/com/lingo/app/ui/ExerciseView.kt`.
  The launch cutscene, lesson intros, creative chips, lesson content, XP rules,
  and all v1.8 features are unchanged.

## Upgrade

`1.9.1` (`versionCode 11`) installs directly over `1.9` (`versionCode 10`) with
`adb install -r`. The signed-in session, XP, streak, completed lessons, writing
drafts, reminders, and daily goal are preserved.

## Verification

- 28 unit tests pass; Android lint reports 0 errors and 43 non-blocking warnings.
- Debug and release builds opened the real `u2-l1` creative step with the
  2,089-line draft, checked the writing, finished the lesson, and reached
  `Lesson complete!` with `+35 XP` and `4/4 tasks met`.
- No `FATAL EXCEPTION` or `AndroidRuntime` entry appeared in logcat, and the app
  process stayed alive.
- The owner’s pre-test progress was restored after QA; the phone is back on the
  release build with 179 XP and `u2-l1` still incomplete.

## Artifact

- `LingoEnglish-1.9.1.apk`
- Size: 72,997,297 bytes
- SHA-256: `60dd5d0903448bc980ab1a836ea56dc58d8b70541323c3feeb2f728f61e76cba`
- Signature: APK Signature Scheme v2, `CN=Android Debug` (accepted sideload risk
  A-01)
- Minimum Android: 8.0 (API 26)
