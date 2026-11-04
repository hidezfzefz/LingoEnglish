# Lingo English v1.10: build plan

**Baseline:** `main` at `6e7482c`, tag `v1.9.1` at `2c97acf`, release 1.9.1
(versionCode 11) installed and signed in on the physical phone. The only
working-tree change at plan time is the approved `BRIEF.md`.

**Goal:** Ship v1.10.0 (versionCode 12) with the Smart Review queue and the
optional Night appearance, preserving every v1.9.1 feature and all local data.

## Architecture

- **Keep the current shape.** One `MainActivity`, `LingoTheme`, `LingoApp`,
  application-scoped `ProgressStore` and `CallStore`. No Navigation library,
  no ViewModel layer, and no module split. The app is small and the existing
  overlay state already survives rotation.
- **Pure review domain.** Add `data/ReviewQueue.kt` with no Android imports:
  - `ReviewItem(exerciseId, misses, correctStreak, lastSeenAt)`
  - `ReviewQueue.recordMiss(items, id, now)`
  - `ReviewQueue.recordCorrect(items, id, now)`
  - `ReviewQueue.due(items, validIds, limit = 5)`
  - `ReviewQueue.encode(items)` / `ReviewQueue.decode(raw)`
  - `ReviewQueue.reviewXp(correct, total)` for the quiz-style XP formula.
- **Content lookup.** Add `LingoContent.exerciseById(id)` and
  `LingoContent.reviewableExercises` (all auto-scorable lesson exercises plus
  the practice bank; exclude `WRITE`). IDs are the stable content keys.
- **Persistence.** `ProgressStore` owns one new preference string
  (`review_items`) and one new appearance preference (`theme_mode`). It exposes
  Compose state and functions, so every screen sees the same queue immediately.
- **Review screen.** A single `ReviewScreen` composable snapshots up to five
  valid exercises when opened, then runs a normal check/continue loop with
  `ExerciseView`. It uses a dedicated result screen and never mutates the
  snapshot mid-session.
- **Navigation.** Add `showReview` to the existing overlay state in `LingoApp`.
  `openReview()` clears other overlays, `BackHandler` closes review, and every
  entry point uses the same doorway.
- **Theme tokens.** Add `ThemeMode { PAPER, NIGHT }`, `LingoPalette`,
  `LocalLingoPalette`, and `LingoTheme(mode)`. Convert the existing top-level
  colour values to composable palette getters so the 943 existing references
  become theme-aware without a screen-by-screen rewrite. Fix the few
  non-composable helpers (`paletteFor`, `stickerColors`) and the `ColorScheme`
  builders explicitly.
- **Offline behaviour.** Review state and the appearance choice are local. The
  review queue, review session, theme switch, and all existing lessons work in
  airplane mode. No new network call, permission, or service is introduced.
- **Native permissions.** The manifest stays unchanged: existing internet,
  network state, microphone, Bluetooth, notifications, and boot permissions
  only. Camera and screen-capture permissions remain removed.

## Data rules

- Only `CHOICE`, `LISTENING`, and `FILL_BLANK` exercises are reviewable.
  Creative `WRITE` tasks are never queued.
- A miss adds the exercise or increments its `misses`, resets
  `correctStreak` to zero, and records `lastSeenAt`.
- A correct answer increments `correctStreak`. At two, the item is removed.
  A later miss starts it again.
- `due()` filters to IDs still present in bundled content, sorts by
  `misses` descending, then `lastSeenAt` ascending, then ID, and takes five.
- Encoded format: one item per line, tab-separated
  `exerciseId, misses, correctStreak, lastSeenAt`. Decoding ignores malformed
  lines, duplicate IDs, and out-of-range values. Cap decoded input at 200
  lines to avoid a corrupt preference causing a large allocation.
- Review XP: `correct * 4 + 10` when `correct / total >= 0.8`, matching the
  existing quiz economy. It updates total XP, daily XP, streak, active days,
  and the activity chart. It does **not** update `quizAttempts`,
  `quizCorrect`, `perfectQuizzes`, or `todayQuizzes`.
- `reset()` clears `review_items`. The appearance choice is a display
  preference, not learning progress, so it survives a learning reset.

## Build order and verification

### M0 — Baseline and safety net

- [ ] Confirm `main`, tag, installed version, signed-in state, XP, streak, and
      `u2-l1` progress on the phone. Record the baseline in `TEST-REPORT.md`.
- [ ] Run the existing unit tests and content audit:
      `./gradlew :app:testDebugUnitTest --no-daemon --max-workers=2 --console=plain`.
- [ ] Build a debug APK and confirm it installs over the release build without
      losing the session. The release and debug builds share the debug signing
      key, so the data directory is preserved.
- [ ] Capture a private progress backup with `run-as` while the debuggable
      build is installed. Store it only under `/tmp/opencode`, never in the
      repo, never print its contents. Verify a restore round-trip before any
      destructive test.
- [ ] Bump `versionCode` to 12 and `versionName` to `1.10.0`.
- [ ] Add empty test files for the new pure logic and a release-notes stub.

**Verify:** tests green; debug install opens signed in; backup file exists
outside the repo and restores the same XP/streak.

### M1 — Review domain and tests

- [ ] Add `ReviewItem`, `ReviewQueue`, and the XP helper.
- [ ] Add `LingoContent.exerciseById` / `reviewableExercises`; assert every
      reviewable ID is unique and non-creative.
- [ ] Write `ReviewQueueTest`:
      miss adds; one correct keeps; two correct clears; miss resets streak;
      ordering; limit five; unknown IDs pruned; encode/decode round trip;
      malformed lines ignored; duplicate IDs deduped; XP formula.
- [ ] Write a content test that every reviewable exercise resolves back to an
      `Exercise`.

**Verify:** `./gradlew :app:testDebugUnitTest --tests "com.lingo.app.ReviewQueueTest"`.

### M2 — Persistence and capture hooks

- [ ] Add `reviewItems`, `reviewDueCount`, `reviewExercises(limit)`,
      `recordReviewMiss`, `recordReviewCorrect`, and `recordReviewSession` to
      `ProgressStore`.
- [ ] Load and save the encoded queue in `init`/each mutation. Clear it in
      `reset()`.
- [ ] Hook `LessonScreen`: when a non-creative answer is checked, record the
      result exactly once. Do not record creative tasks.
- [ ] Hook `QuizScreen`: when an answer is checked, record the result exactly
      once.
- [ ] Add a small test for the XP helper and the reset-clears-queue rule where
      it can be expressed without an Android context.

**Verify:** wrong lesson answer increments the queue; wrong quiz answer
increments it; a correct answer does not add an item; force-stop and relaunch
keeps the count.

### M3 — Review session screen

- [ ] Add `ReviewScreen` with a snapshot of up to five exercises, header
      progress, `ExerciseView`, check/continue, and a result screen.
- [ ] On check: record miss or correct against the queue. On finish: call
      `recordReviewSession(correct, total)` once.
- [ ] Result screen: score, XP, remaining queue count, "Review again" when
      items remain, and "Back to practice". Reuse the existing result visual
      language without duplicating the whole quiz result composable.
- [ ] Add `showReview` to `LingoApp`, `openReview()`, and `BackHandler`.
- [ ] Empty/unknown queue state: friendly message and a way back; never crash.

**Verify:** seed two wrong answers, open review, answer one right and one
wrong, confirm the queue count changes correctly and XP/streak update once.
Rotate and force-stop mid-session; the session restarts safely and the queue
is intact.

### M4 — Entry points

- [ ] Home: compact `ReviewCard` only when `reviewDueCount > 0`, with count,
      estimated time, and a single review action. It sits below the daily hero
      and does not replace the primary lesson action.
- [ ] Practice: "Tricky questions" card with count, or a friendly empty state.
- [ ] Quiz result: "Review tricky questions" action when the queue is
      non-empty; hidden for a perfect quiz with an empty queue.
- [ ] Pass `onOpenReview` through `LingoApp` to Home, Practice, and Quiz.
- [ ] TalkBack labels, 48 dp targets, and no color-only status.

**Verify:** on the phone, a wrong answer produces the Home card and Practice
count; each entry opens the same review; clearing the queue hides the Home
card and shows the Practice empty state.

### M5 — Night appearance

- [ ] Add `ThemeMode`, `LingoPalette`, `LingoPalettes.Paper`,
      `LingoPalettes.Night`, `LocalLingoPalette`, and `LingoTheme(mode)`.
- [ ] Convert the top-level colour values to composable getters; fix
      `paletteFor`, `stickerColors`, the light/dark `ColorScheme` builders, and
      any non-composable call sites the compiler finds.
- [ ] Add `themeMode` to `ProgressStore` and an Appearance section in Settings
      with Warm paper / Night choices and a short explanation.
- [ ] Update `MainActivity` to pass the mode and switch system bar icon
      appearance. Warm paper remains the default.
- [ ] Add a pure contrast test for the key text/background pairs in both
      palettes (4.5:1 for body text, 3:1 for large text and UI components).
- [ ] Isolate the theme work in its own commits so it can be reverted without
      touching Smart Review if it fails QA.

**Verify:** switch to Night, walk every screen (Home, Learn, Practice, Call,
Progress, Settings, lesson, quiz, flashcards, word bank, roleplay, sentence
tiles, level path, test session, sign-in, cutscene), rotate, force-stop, and
relaunch. Switch back to Warm paper and confirm the original look. Run the
contrast test.

### M6 — Docs and release candidate

- [ ] Update `README.md` with Smart Review, Night appearance, version, APK
      name, and checksum instructions.
- [ ] Write `RELEASE-NOTES-1.10.md`.
- [ ] Update `SECURITY-REVIEW.md`, `TEST-REPORT.md`, and
      `LAUNCH-CHECKLIST.md` with this release's evidence.
- [ ] Build debug and release with the low-resource flags; stop daemons.
- [ ] Install the release over v1.9.1 and verify the signed-in session, XP,
      streak, completed lessons, drafts, reminders, goal, exams, and weekly
      scores.

**Verify:** `:app:testDebugUnitTest`, `:app:lintDebug`,
`:app:assembleDebug`, and `:app:assembleRelease` all pass; the over-install
opens signed in with the baseline data intact.

## Convio stage checkpoints

- [ ] **Design:** settle the review card, session, result, Night palette,
      contrast pairs, motion, and accessibility specs. Show the user, then call
      `next_stage` for the Scaffold brief.
- [ ] **Scaffold:** confirm the existing project/tooling, version bump, palette
      scaffolding, and test harness. Then call `next_stage` for Build screens.
- [ ] **Build screens:** complete M1–M6. Then call `next_stage` for Security
      review.
- [ ] **Security review:** run the required checks, fix findings, update
      `SECURITY-REVIEW.md`, then call `next_stage` for Test thoroughly.
- [ ] **Test thoroughly:** run the full matrix below, record evidence in
      `TEST-REPORT.md`, then call `next_stage` for Ship.
- [ ] **Ship:** CI green, release published, asset verified, docs updated, and
      the published APK over-installed. Then finish the Convio pipeline.

## Verification matrix

- **Unit:** `ReviewQueueTest`, `ReviewXpTest`, `ThemeContrastTest`, plus the
  existing content, reminder, next-step, cutscene-policy, and cutscene-content
  tests.
- **Build:** debug APK, release APK, Android Lint, content audit, GitHub
  Actions.
- **Device:** physical `RZCW30HF88J` (Samsung SM-A546E, Android 16/API 36),
  release build, signed-in owner, display/density/font/rotation unchanged.
- **Smart Review:** miss a lesson question; miss a quiz question; see the Home
  and Practice counts; open from all three entry points; one correct keeps;
  two correct clears; a later miss restores; empty state; unknown ID safe;
  rotation; force-stop; offline.
- **XP/streak:** review XP matches the formula, daily XP and streak update
  once, accuracy and the daily "quick challenge" do not change.
- **Night:** both appearances on every screen; 200% font; TalkBack; reduced
  motion; status/navigation bar icons; contrast test.
- **Regression:** launch ≤5 s and skippable; lesson intro one Begin tap and
  ≤5 s; lessons; creative drafts; level exams; weekly tests; accounts and
  recovery; reminders; calls; word bank; flashcards; roleplay; sentence tiles.
- **Upgrade:** over-install release 1.10.0 over 1.9.1; session and all local
  progress preserved; review queue empty on first launch after upgrade.
- **Security:** no new permissions, no secrets in the APK or logs, backup rules
  unchanged, exported components unchanged, local review data contains only
  content IDs and counters.

## Risks and fallbacks

- **Night theme breadth.** The app has many hardcoded palette references.
  Mitigation: palette getters, contrast tests, a screen-by-screen sweep, and
  isolated commits. If Night cannot pass QA without destabilising the release,
  revert the Night commits and ship Smart Review on Warm paper; tell the user
  before shipping.
- **Review queue corrupt data.** Mitigation: pure encode/decode tests, malformed
  line skipping, ID pruning, and a 200-line cap.
- **Double-recording.** Mitigation: record only inside the one-shot check
  action, never in composition; verify with a force-stop/relaunch count check.
- **Data loss during destructive QA.** Mitigation: private backup/restore
  round-trip before clearing anything, and never print or commit the backup.
- **Build resource limits.** Keep `--no-daemon --max-workers=2`, stop daemons,
  and avoid parallel Gradle work.
