# Lingo English v1.10: brief

**What it is:** The existing Android app that takes learners from A1 to confident, practical English through short lessons, creative production tasks, level exams, weekly tests, and live voice calls with matched learners.

**For:** Existing Lingo learners on Android 8.0+ who want a steady daily habit, and new learners who need a clear next step without a placement interview. The first thing they do is sign in, then continue the next lesson on the path.

**Success:** A v1.10.0 APK on GitHub Releases with every v1.9.1 feature intact, no loss of local progress, a green content audit and CI run, a completed security review, and two measurable improvements: a Smart Review queue that turns mistakes into a short daily review, and an optional Night appearance that keeps the warm paper identity as the default.

## Discovery answers

- **core:** A learner can see how many tricky questions are waiting, review up to five of them with the same exercise UI and explanations, and clear an item after two consecutive correct answers. The queue survives restarts and updates.
- **framework:** Kotlin with Jetpack Compose (existing app, no rewrite).
- **backend:** Supabase for accounts and existing backend features, LiveKit for voice calls. No schema or service changes in this release.
- **native:** Existing notifications, offline mode, and microphone only. No new permissions, camera, location, or payments.
- **design:** Existing warm paper/forest identity by default, plus an optional Convio-inspired calm dark appearance in Settings.

## Scope (v1.10)

### 1. Smart Review

- **Capture mistakes locally.** When a learner checks an auto-scorable exercise in a lesson or a practice quiz and gets it wrong, record that exercise by its stable content ID. Creative `WRITE` tasks are excluded because they have no single correct answer.
- **Two-right rule.** A missed question enters the queue. One correct answer keeps it for one more rep; a second consecutive correct answer clears it. A later miss starts the count again.
- **Bounded and durable.** The queue holds only IDs that still exist in the bundled content, so content changes cannot crash the review screen. It survives process death, force-stop, and an APK update. A learning reset clears it.
- **Home entry point.** When items are waiting, Home shows one compact card with the count and an estimated review time. The card is hidden when the queue is empty and does not compete with the daily lesson hero.
- **Practice entry point.** Practice gets a "Tricky questions" workout card showing the count, or a friendly empty state when there is nothing to review.
- **Quiz result entry point.** After a quiz with at least one miss, the result screen offers a "Review tricky questions" action that opens the queue. A perfect quiz does not show it.
- **Review session.** Up to five questions, served with the existing `ExerciseView` and explanation banners. The session has its own progress header and a result screen. If items remain, the result offers "Review again"; otherwise it points back to Practice.
- **XP and streaks.** Review uses the same XP formula as a practice quiz: 4 XP per correct answer plus a 10 XP bonus for 80% or more. It counts toward daily XP, the daily goal, the streak, and active days. It does **not** change the Practice accuracy statistic or mark the daily "quick challenge" complete, because the sample is deliberately biased toward past mistakes.
- **No new services.** Review state is local-only. No backend schema, analytics, ads, crash SDK, or network call is added.

### 2. Optional Night appearance

- **Settings > Appearance** with two choices: **Warm paper** (default, unchanged) and **Night** (a calm, Convio-inspired dark palette).
- The choice is saved on the phone, applies app-wide immediately, and survives restarts and updates. Existing installs stay on Warm paper until the learner changes it.
- Night keeps the same serif/sans typography, hand-drawn sticker system, spacing, and forest/lime/coral accents. It is a palette change, not a rebrand.
- System status and navigation bar icons switch with the appearance so they remain readable.
- The setting is manual in this release; automatic system-dark following is deferred.

### 3. Release hygiene

- Version **1.10.0**, versionCode **12**.
- Updated `README.md`, `TEST-REPORT.md`, `SECURITY-REVIEW.md`, `LAUNCH-CHECKLIST.md`, and `RELEASE-NOTES-1.10.md`.
- A data-preserving upgrade over v1.9.1: signed-in session, XP, streak, completed lessons, creative drafts, reminders, daily goal, exams, and weekly scores stay intact.
- GitHub Actions content audit and Android Lint green on the release source; APK checksum and GitHub Release published.

**Not in v1.10:** lesson resume, spaced-repetition scheduling, new curriculum units or exercises, backend schema changes, iOS or cross-platform rewrite, pronunciation scoring, payments, leaderboards, Google Workspace SMTP migration, or a visual rebrand.

## Design

- **Warm paper remains the primary identity.** Smart Review uses the existing Coral "tricky" accent, Mint success surfaces, sticker motifs, and editorial type. No new illustration style or fonts.
- **Night palette:** deep forest/ink background, slightly lighter paper surfaces, cream text, lime and coral accents. All text and icon-only controls must meet WCAG 2.2 AA contrast in both appearances.
- **Accessibility:** at least 48 dp touch targets, TalkBack labels for icon-only controls, no color-only meaning, 200% font-scale layouts, and reduced-motion-safe transitions. Review feedback must be announced and readable.
- **Hierarchy:** Home keeps one primary action (the daily lesson hero). The review card is secondary, compact, and only present when it has something to offer.

## Stack

- **App:** Kotlin + Jetpack Compose + Material 3, min SDK 26, target/compile SDK 37. No new dependency and no module split.
- **Local state:** application-scoped `ProgressStore` on SharedPreferences. Review items are encoded locally with stable exercise IDs, miss counts, consecutive-correct counts, and last-seen time. The appearance choice is a separate local preference.
- **Backend:** Supabase for email/password auth, profiles, matchmaking, weekly scores, and password recovery. LiveKit for voice calls. Existing secrets stay only in `supabase/.env.local` and `local.properties`.
- **Delivery:** GitHub Actions content audit and Android Lint, GitHub Releases with a debug-signed sideload APK. MIT licence remains.

## Content and integrations

- No new lesson content. The existing 6 units, 20 lessons, 80 exercises, 20 creative tasks, exam banks, weekly selection, 36 words, roleplays, and sentence puzzles remain the source of truth.
- Smart Review draws only from existing auto-scorable lesson and practice-bank exercises.
- Local Android notifications only. No analytics, ads, crash SDK, or new third-party service.
- No new account or lent service is required for this release.

## Constraints and risks

- Must not clear or migrate away existing local data; the new APK installs over v1.9.1 and preserves the signed-in owner session.
- Review IDs must remain stable. Unknown IDs are ignored safely; the queue is pruned rather than allowed to crash.
- The Night appearance touches many existing hardcoded palette references. Convert them to theme-aware palette tokens and check every screen, especially lessons, exams, calls, cutscenes, and sign-in.
- The physical phone is the QA device. Do not change its display size, density, font scale, or rotation, and do not sign out the owner.
- Gradle builds stay low-resource (`--no-daemon --max-workers=2`) and daemons are stopped afterwards.
- Deployment secrets must never be printed, committed, or placed in the APK beyond the public Supabase URL and anon key.
- Security review, thorough testing, and the launch checklist are required and must not be skipped.
- Owner-only checks that need a second phone or external mail delivery remain documented as owner actions rather than silently claimed.

## Acceptance

- Cold start remains at or under 5 seconds and is skippable; the lesson intro still takes one Begin tap and at most 5 seconds.
- With a non-empty queue, Home shows the correct count on first load. With an empty queue, no review card appears.
- A wrong lesson or quiz answer appears in the queue. One correct review answer keeps it; a second consecutive correct answer clears it. A miss in review restores it.
- The queue survives force-stop, relaunch, and an over-install of the new APK.
- A review session serves up to five valid questions, shows the correct answer and explanation, awards XP with the quiz formula, and updates daily XP/streak without changing the quiz accuracy stat.
- Warm paper remains the default. Night switches immediately, persists after restart, and keeps text, borders, buttons, and system bar icons readable.
- Offline use, 200% font scale, TalkBack, reduced motion, rotation, and the v1.8/v1.9/v1.9.1 flows still pass.
- No regression in lessons, creative drafts, level exams, weekly tests, accounts, password recovery, reminders, calls, word bank, flashcards, roleplay, or sentence tiles.

## Assumptions (please check)

- "Improve the app" means the existing Lingo English Android app, not a new product.
- Smart Review covers auto-scorable lesson and quiz exercises; creative `WRITE` tasks are not queued because they have no single correct answer.
- Review XP uses the quiz formula but does not change the Practice accuracy stat or count as the daily "quick challenge".
- The queue is naturally bounded by the 83 bundled auto-scorable exercises (60 in lessons, 23 in the practice bank); there is no manual "clear all" in v1.10.
- The Night appearance is manual, with Warm paper as the default. Automatic system-dark following is deferred.
- A debug-signed APK is still acceptable for sideloading; Play Store signing remains out of scope.
- Google Workspace SMTP migration remains deferred.
- The owner's existing signed-in session and local progress are preserved throughout.
