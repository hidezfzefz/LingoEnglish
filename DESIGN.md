# Lingo English v1.10: design spec

> The previous v1.9 design is retained in `DESIGN-1.9.md`. This file is the
> current design spec for v1.10.

**Direction:** Warm paper stays the product's face. Smart Review borrows the
existing coral "tricky" accent, the hand-drawn sticker language, and the
editorial serif/sans pairing. Night is a second palette for the same product,
not a rebrand: deep forest surfaces, warm cream text, and one bright lime
brand accent.

The tokens are in code in
`app/src/main/java/com/lingo/app/ui/DesignTokens.kt`. `Theme.kt` now aliases the
existing Paper colours to `LingoPalettes.Paper`, so there is one source of
truth for the default appearance.

## What we set aside from Convio Design

The user chose the existing brand with an optional Convio-inspired dark
appearance. Convio Design's **Inter type, translucent cards, pill-first
buttons, and dark-by-default** are therefore not applied. We keep Lingo's
serif/sans type scale, solid cards, 18 dp buttons, and sticker motifs. We do
borrow Convio's calm dark discipline: a deep background, one confident accent,
restrained borders, and generous negative space.

Hallmark's gate about invented facts is applied: the only counts shown are real
content counts (83 reviewable questions: 60 lesson questions plus 23 practice
questions), and time estimates are labelled "about" and derived from the
five-question session length.

## Colour tokens

| Token | Warm paper | Night | Role |
|---|---|---|---|
| `cream` | `#FAF6EE` | `#0E1713` | App background |
| `paper` | `#FFFCF6` | `#16221C` | Cards and sheets |
| `ink` | `#17251F` | `#F3EEE2` | Primary text |
| `inkSoft` | `#32443B` | `#D5CFC1` | Secondary text on tints |
| `forest` | `#143D31` | `#D9F27C` | Brand fill and brand text |
| `emerald` | `#2A7D5F` | `#86D3AB` | Success and progress |
| `mint` | `#E3F1E7` | `#1E3329` | Soft success surface |
| `mintDeep` | `#CFE7D7` | `#2C4637` | Success border |
| `lime` | `#D9F27C` | `#33421F` | Accent container |
| `limeSoft` | `#EEF7CD` | `#2A351C` | Quiet accent surface |
| `coral` | `#EC7256` | `#F08A70` | Tricky/attention fill |
| `coralDeep` | `#8C3B27` | `#FFB4A1` | Tricky text on paper/peach |
| `peach` | `#FCE5DB` | `#3A211A` | Tricky surface |
| `blue` | `#3B6E9E` | `#8CB8E0` | Listening accent |
| `sky` | `#E3EDF8` | `#1C2C3A` | Listening surface |
| `purple` | `#6E5AA6` | `#B9A7E8` | Grammar accent |
| `lavender` | `#ECE7F8` | `#2A2338` | Grammar surface |
| `teal` | `#2F7D7A` | `#7FD1CC` | Roleplay accent |
| `seafoam` | `#DFF3EE` | `#17302E` | Roleplay surface |
| `deepTeal` | `#1F504C` | `#A9E3DF` | Roleplay text |
| `gold` | `#E9B949` | `#F2C766` | Saved-star fill |
| `sand` | `#F1E9DA` | `#242019` | Neutral inset |
| `line` | `#E8E0D2` | `#33453C` | Decorative hairline |
| `muted` | `#59685F` | `#A8B5AC` | Secondary text |
| `danger` | `#C2493D` | `#FF8A7A` | Destructive text |
| `pureWhite` | `#FFFFFF` | `#0E1713` | On-colour for coloured fills |
| `onCoral` | `#17251F` | `#0E1713` | Text on coral fills |
| `onBrandMuted` | `#BAC2B9` | `#556438` | Quiet text on brand fills |
| `focus` | `#143D31` | `#D9F27C` | 3:1 focus ring |

**Night rationale.** The app uses `Forest` both as a dark fill and as brand
text. In Night, `Forest` becomes the light lime brand fill, and `Cream` becomes
the dark background/on-brand colour. That keeps existing `Forest`/`Cream`
pairings readable (hero card, primary buttons, logo) without a risky
screen-by-screen rewrite. `Lime` becomes the dark accent container, so
`Ink`-on-`Lime` also stays readable. The result is a deep forest interface with
one bright lime call to action, not a generic grey dark mode.

## Contrast guardrails

Checked with the WCAG 2.2 relative-luminance formula. Normal text needs 4.5:1;
large text and UI boundaries need 3:1.

| Pair | Paper | Night |
|---|---:|---:|
| `ink` on `cream` | 14.75 | 15.76 |
| `ink` on `paper` | 15.53 | 14.17 |
| `muted` on `cream` | 5.46 | 8.58 |
| `muted` on `paper` | 5.74 | 7.71 |
| `forest` on `paper` | 11.77 | 13.24 |
| `cream` on `forest` | 11.19 | 14.72 |
| `ink` on `lime` | 12.83 | 9.35 |
| `forest` on `lime` | 9.73 | 8.73 |
| `coralDeep` on `peach` | 6.27 | 8.71 |
| `coralDeep` on `paper` | 7.40 | 9.61 |
| `onCoral` on `coral` | 5.38 | 7.45 |
| `blue` on `sky` | 4.54 | 6.83 |
| `purple` on `lavender` | 4.72 | 6.99 |
| `teal` on `seafoam` | 4.20 | 7.94 |
| `deepTeal` on `seafoam` | 7.88 | 9.84 |
| `danger` on `cream` | 4.52 | 7.97 |
| `onBrandMuted` on `forest` | 6.61 | 5.19 |
| `focus` on `paper` | 11.77 | 13.24 |

**Rules that fall out of the table:**

- Small coral text uses `CoralDeep`, never `Coral` on paper. `Coral` is a fill
  and a decorative mark. Existing small-text call sites are audited in M5.
- Coral-filled buttons use `OnCoral` (ink/dark), not white. This fixes the
  existing 2.95:1 white-on-coral pair in the paper theme.
- Text on `Forest`/`Lime` fills uses `OnBrandMuted` for quiet copy instead of
  `Cream.copy(alpha = 0.55f)`, which falls below 4.5:1 in Night.
- Small text on `Seafoam` uses `DeepTeal` (7.88:1), not `Teal` (4.20:1).
  `Teal` is for large text, icons, and fills.
- `Line` is decorative only. Focus rings use `FocusRing`; selected states also
  carry a checkmark or label, never colour alone.
- `Gold` on paper is 1.78:1, so the saved star is treated as decorative and
  always paired with a content description that states the selected state.

## Type, spacing, radius, motion

- **Type:** existing `LingoTypography`. Serif display/headline, sans body and
  labels. No new font, no Inter. Review uses `labelSmall` for the eyebrow,
  `titleLarge` for the card title, `bodySmall` for supporting copy, and
  `headlineSmall` for the review question.
- **Spacing:** `LingoSpacing` adds `xxs 4`, `xs 8`, `sm 12`, `md 16`, `lg 20`,
  `xl 24`, `xxl 32`, `screen 20`, `card 18`, `section 22` (dp). Existing screens
  keep their current values; new surfaces use the scale.
- **Radius:** `LingoRadii` adds `button 18`, `panel 20`, `card 24`,
  `cardLarge 26`, `hero 30`, `sheet 28` (dp). `LingoShapes` remains the
  Material shape set.
- **Motion:** existing `LingoMotion` (`quick 150`, `standard 240`,
  `expressive 600`, `celebration 800` ms). Review uses `quick` for option
  selection, `standard` for the feedback reveal, and no looping animation.
  Compose honours the system animation scale, so reduced motion removes the
  reveal movement. The review result does not use confetti.

## Core components

### 1. `ReviewCard` — Home

- Surface: `Peach`, 1 dp `Coral.copy(alpha = 0.25f)` border, 24 dp radius,
  full width, minimum height 96 dp, padding 16–18 dp.
- Left: `StickerKind.TARGET`, 44 dp.
- Eyebrow: `SMART REVIEW`, `labelSmall`, `CoralDeep`.
- Title: `"12 tricky questions"`, `titleLarge`, `Ink`.
- Detail: `"Start with 5 · about 2 min"`, `bodySmall`, `InkSoft`.
- Right: `DUE` tag (`Paper` background, `CoralDeep` text) and a `→` in
  `CoralDeep`.
- Whole card clickable with `onClickLabel = "Review 12 tricky questions"`;
  48 dp target; no nested click targets.
- Hidden when the queue is empty. It sits below the daily hero and any
  `NextStepCard`, above `TodayPlanCard`, so the lesson remains the primary
  action.

### 2. `ReviewWorkoutCard` — Practice

- Non-empty: same palette and sticker as `ReviewCard`, full width, 26 dp
  radius, title `"Tricky questions"`, detail `"12 waiting · start with 5"`,
  trailing `→`. Entire card clickable.
- Empty: `Mint` surface, `MintDeep` border, title `"Nothing tricky right now"`,
  detail `"Missed questions will collect here for a quick review."`, no arrow,
  not clickable.
- Placed at the top of the "Choose a workout" section, above the two-column
  `ModeCard` grid.

### 3. `ReviewResultLink` — quiz result

- Shown only when the quiz had at least one miss **and** the queue is non-empty.
- Slim `Peach` surface, 18 dp radius, `StickerKind.TARGET` 30 dp, copy
  `"2 tricky questions"` / `"Review the ones you missed."`, trailing
  `"Review →"` in `CoralDeep`.
- Sits above the primary "Try another round" button; it never replaces it.
- A perfect quiz with an empty queue shows nothing.

### 4. `ReviewScreen` — session

- Top bar: 48 dp back button, eyebrow `SMART REVIEW` in `CoralDeep`, title
  `"Question 2 of 5"`, and a `ProgressLine` (`Forest` on `Sand`).
- Body: the existing `ExerciseView`, unchanged, so options, listening playback,
  feedback banner, and explanation behave exactly like a lesson or quiz.
- Bottom: 54 dp `PrimaryButton` "Check answer" (`Forest`/`Cream`); after check
  it becomes "Next question" or "See results". Navigation-bar padding.
- Snapshot: the five exercises are captured when the screen opens. The queue
  may change underneath; the session never swaps a question mid-answer.
- Back closes the session and returns to the previous surface. `BackHandler`
  does the same.

### 5. `ReviewResultScreen`

- Score ring (`GoalRing`) with the session percentage; ring colour is
  `Emerald` at 80%+, `Gold` at 60–79%, `Coral` below 60%.
- Title: 100% "Sharp work."; 60–99% "Progress, not perfection."; below 60%
  "These need another pass."
- Subtitle states how many questions remain in the queue.
- Two stat tiles: correct/total (`Mint`) and XP earned (`LimeSoft`).
- If items remain: primary "Review again" and secondary "Back to practice".
- If the queue is clear: primary "Back to practice" only.
- If the queue is empty or all IDs were pruned when opened: sticker, title
  "Nothing tricky right now.", the empty-state copy, and "Back to practice".
  No error dialog, no crash.

### 6. `AppearanceSetting` — Settings

- Section label `APPEARANCE` in `labelSmall`/`Muted`.
- One `Paper` card, 20 dp radius, 1 dp `Line` border, containing two selectable
  rows in a `selectableGroup` with `Role.RadioButton`:
  - **Warm paper** — swatch of fixed `LingoPalettes.Paper` values
    (`Cream`/`Paper`/`Forest`/`Lime`); detail "Cream paper, forest ink";
    selected by default.
  - **Night** — swatch of fixed `LingoPalettes.Night` values
    (`#0E1713`/`#16221C`/`#D9F27C`/`#F08A70`); detail "Calm dark, same
    accents". The swatches must not use the active palette, or both choices
    would look the same in Night.
- Selected row: `Mint` background, `Forest` border, checkmark. Unselected:
  `Paper` background, `Line` border. 48 dp minimum row height.
- Helper text: "Applies right away. Saved on this phone."
- The choice applies immediately, persists across restarts and updates, and
  does not affect learning progress. Existing installs stay on Warm paper.

## Key screen sketches

### Home — review card present

```text
┌────────────────────────────────────┐
│ LINGO                        [gear]│
│ Good evening.                      │
│ A little English goes a long way.  │
│ [streak] [XP] [level]              │
│ ┌────────────────────────────────┐ │
│ │ TODAY'S LESSON                 │ │
│ │ Ordering Food & Drink          │ │
│ │ 5 min · +30 XP                 │ │
│ │ [Continue lesson →]            │ │
│ └────────────────────────────────┘ │
│ ┌────────────────────────────────┐ │
│ │ ◉ SMART REVIEW           DUE   │ │
│ │ 12 tricky questions            │ │
│ │ Start with 5 · about 2 min     │ │
│ │ Review now                  →  │ │
│ └────────────────────────────────┘ │
│ Today's plan                       │
│ ...                                │
└────────────────────────────────────┘
```

### Practice — non-empty and empty

```text
Choose a workout
┌────────────────────────────────────┐
│ ◉ Tricky questions           12 →  │
│ Start with 5 · about 2 min         │
└────────────────────────────────────┘
[Flashcards] [Listening]
[Grammar]    [Word bank]

Empty:
┌────────────────────────────────────┐
│ ◉ Nothing tricky right now         │
│ Missed questions will collect here │
│ for a quick review.                │
└────────────────────────────────────┘
```

### Review session

```text
[←]  SMART REVIEW             2 of 5
     Question 2 of 5
     ▓▓▓▓▓▓░░░░░░░░░░
     CHOOSE
     Choose the most natural reply
     “Hi, I'm Leo. Nice to meet you.”
     A  Nice to meet you too. I'm Ana.
     B  Yes, I am fine, thank you.
     C  I am go to school.
     D  Nice meet you, Leo.
     [Check answer]
```

### Review result

```text
              ◯ 80%
         Review complete
   You cleared 4 of 5 tricky questions.
   2 questions are still waiting.

   [ 4 / 5 ]        [ +18 XP ]

   [ Review again ]
   [ Back to practice ]
```

### Settings — appearance

```text
APPEARANCE
┌────────────────────────────────────┐
│ ● Warm paper                  ✓    │
│   Cream paper, forest ink          │
│ ○ Night                            │
│   Calm dark, same accents          │
└────────────────────────────────────┘
Applies right away. Saved on this phone.
```

### Night — same hierarchy, different palette

```text
background  #0E1713   deep forest ink
card        #16221C   slightly lifted
hero/button #D9F27C   bright lime, dark text
tricky      #3A211A   dark peach, light coral text
text        #F3EEE2   warm cream
```

The hero card and primary buttons invert to a light lime fill with dark text.
Cards, sheets, and the settings surface stay dark. The result is a calm dark
interface with one confident brand accent, not a grey Material dark mode.

## Accessibility and states

- All icon-only controls have content descriptions; review cards use
  `onClickLabel` with the real count.
- 48 × 48 dp minimum touch targets; the review card is a single target.
- No colour-only meaning: counts are text, selected appearance has a checkmark
  and a radio role, feedback has a title and explanation.
- 200% font scale: review cards use `Column`/`Row` with weight and wrapping,
  never fixed-height text boxes. The session header can wrap to two lines.
- TalkBack order: eyebrow, title, detail, action. The trailing arrow is
  decorative (`clearAndSetSemantics` or `contentDescription = null`).
- Reduced motion: feedback uses `AnimatedVisibility` with the system scale; no
  looping or parallax. The result is static.
- Empty state: friendly copy and a way back. Corrupt or unknown queue IDs are
  pruned by the data layer before the screen is composed.
- Error state: there is no network error surface because review is local. The
  only failure mode is an empty/pruned queue, handled by the empty state.

## Approval checklist

- [ ] Warm paper stays the default; Night is a palette choice in Settings.
- [ ] Review card, Practice card, quiz-result link, session, result, and empty
      state match the specs above.
- [ ] Night uses the token inversion described here and keeps the existing
      type, stickers, and shapes.
- [ ] Coral-filled buttons use dark ink text, fixing the existing contrast
      issue without changing the brand.
