# Lingo English v1.8: design

## Direction

**Warm editorial, calm momentum.** Lingo is a daily language ritual, not a arcade. The existing identity already carries that: cream paper, deep forest ink, a fresh lime accent, a literary serif for big statements, and hand-drawn stickers for warmth. v1.8 keeps it and tightens the craft instead of rebranding.

Borrowed from references:
- **Aesop** — one idea per section, generous vertical rhythm, and copy that feels written rather than generated.
- **Airbnb** — friendly rounded surfaces and one obvious next action, especially on small screens.

Set aside:
- **Convio Design** — its dark palette and Inter-only type conflict with the existing warm-paper brand the user chose to keep.
- **Linear** — its dense, keyboard-first layout is wrong for a learning app used in short, tired moments.
- **Web-only guidance** (CSS selectors, ARIA, URL state) — translated to Compose semantics, state hoisting, and Android back handling.

## Design tokens

Tokens live in `app/src/main/java/com/lingo/app/ui/Theme.kt`. Colour and type are semantic; spacing and radius are the 4 dp / shape scale already used across the app.

### Colour

| Token | Value | Use | Contrast check |
| --- | --- | --- | --- |
| `Cream` | `#FAF6EE` | App background | — |
| `Paper` | `#FFFCF6` | Sheets, cards | — |
| `Ink` | `#17251F` | Primary text | 14.75:1 on Cream |
| `InkSoft` | `#32443B` | Secondary headings | 9.62:1 on Cream |
| `Forest` | `#143D31` | Hero surfaces, primary buttons | Cream 11.19:1 |
| `Emerald` | `#2A7D5F` | Progress, success | 4.89:1 on Paper |
| `Lime` | `#D9F27C` | Primary action on Forest | 9.73:1 on Forest |
| `Muted` | `#59685F` | Secondary body text | 5.46:1 Cream · 5.74:1 Paper · 4.88:1 Sand |
| `Line` | `#E8E0D2` | Hairline borders | decorative |
| `Coral` | `#EC7256` | Decorative accent, sticker fills | not for small text |
| `CoralDeep` | `#8C3B27` | Coral text on light surfaces, filled actions | 7.03:1 on Cream · 6.27:1 on Peach |
| `Danger` | `#C2493D` | Destructive actions | 4.52:1 on Cream |
| `Gold` | `#E9B949` | Achievement accents | decorative |

Rules:
- Never put `Cream` or white text on `Coral` (2.89:1). Use `CoralDeep` or `Ink` for text and filled actions.
- Never use `Muted` below 12 sp without checking the surface; the darkening above is the accessible default.
- Colour is never the only signal: pair success/error with an icon, label, or copy.

### Type

| Role | Token | Family | Size / line | Use |
| --- | --- | --- | --- | --- |
| Display | `displayLarge` / `displayMedium` | Serif bold | 46/50 · 38/42 | Greeting, one per screen |
| Headline | `headlineLarge` / `headlineMedium` | Serif bold | 30/36 · 26/32 | Hero title, section lead |
| Title | `titleLarge` / `titleMedium` | Sans bold / semibold | 20/26 · 16/22 | Card titles, labels |
| Body | `bodyLarge` / `bodyMedium` | Sans regular | 16/25 · 14/21 | Reading copy |
| Small | `bodySmall` | Sans regular | 12/18 | Captions, helper text |
| Label | `labelLarge` / `labelMedium` | Sans bold | 14/18 · 12/16 | Buttons, pills |
| Eyebrow | `labelSmall` | Sans bold, 1.3 sp tracking | 10/14 | Section tags only |

Keep one serif display moment per screen. Do not use all-caps for sentences; only short tags such as `TODAY'S LESSON`.

### Spacing

4 dp base, with these steps used in the app:

| Token | Value | Use |
| --- | --- | --- |
| `xs` | 4 dp | Icon-to-label |
| `sm` | 8 dp | Tight stacks, pill gaps |
| `md` | 12 dp | Card inner gaps |
| `lg` | 16 dp | Default padding |
| `xl` | 20 dp | Screen gutters |
| `2xl` | 24 dp | Section padding |
| `3xl` | 32 dp | Large breaks |

Screen gutter 20 dp. Card padding 20–24 dp. Section gap 22–24 dp. Minimum touch target 48×48 dp, even when the visual is smaller.

### Radius

`LingoShapes`: 8 / 12 / 18 / 24 / 32 dp. Hero cards 30 dp, standard cards 26 dp, buttons 18 dp, pills and stickers fully round, inputs 14–18 dp. Do not use one radius for everything; hierarchy comes from the size difference.

### Motion

`LingoMotion`: `quick` 150 ms, `standard` 240 ms, `expressive` 600 ms, `celebration` 800 ms. Motion shows cause and effect (answer feedback, progress fill, sheet open), never decoration. Compose respects the system animation scale; no custom infinite motion on the Home path.

## Core components

| Component | Spec | States |
| --- | --- | --- |
| `PrimaryButton` | 54 dp high, 18 dp radius, 22 dp horizontal padding, label 15 sp | default, pressed, disabled |
| `SecondaryButton` | 54 dp high, hairline border, transparent fill | default, pressed |
| `SurfaceCard` | Paper or tinted surface, 1 dp border, 26 dp radius, 20–24 dp padding | static, clickable |
| `Tag` | pill, 10 sp tracked label, tinted background | static |
| `StatPill` | 42 dp high, sticker + label, hairline border | static |
| `CircleStickerButton` | 42–48 dp visual, 48 dp touch area, content description required | default, pressed |
| `SectionHeader` | title 20 sp bold + optional text action | static |
| `GoalRing` | 86–88 dp, 7 dp stroke, lime on forest | animated, complete |
| **New** `NextStepCard` | Mint surface, 26 dp radius, sticker, title, one line, arrow action | weekly, exam, practice |
| **New** reminder row | 48 dp row, bell sticker, title + time, Material 3 switch | off, on, blocked |

## Key screens

### Home (existing + one conditional card)

```
┌────────────────────────────────────┐
│ [L] LINGO                    [gear]│
│ Good evening.                      │
│ A little English goes a long way.  │
│ [streak] [XP] [level]              │
│ ┌────────────────────────────────┐ │
│ │ TODAY'S LESSON          Begin  │ │
│ │ Introducing Yourself           │ │
│ │ Say hello with confidence      │ │
│ │ (ring) 40 XP to go · 5 min     │ │
│ │ [ Continue lesson → ]          │ │
│ └────────────────────────────────┘ │
│ ┌────────────────────────────────┐ │  ← only when weekly/exam/practice
│ │ [bell] Weekly check-in         │ │
│ │ 10 questions · about 10 min    │ │
│ │ [ Start weekly test → ]        │ │
│ └────────────────────────────────┘ │
│ Today's plan                0 of 3 │
│ [lesson] [words] [quiz]            │
│ Word of the day                    │
│ Your courses                       │
└────────────────────────────────────┘
```

Decision: the lesson hero is already the recommendation when the next action is a lesson. The extra card appears only when the state calls for a weekly test, an exam, or free practice, so there are never two competing lesson CTAs. It hides when the daily goal is complete.

### Settings (reminder section inserted)

```
Settings
Make Lingo fit your day.
DAILY GOAL
[20] [30] [40] [60] [80]
40 XP a day · about 1 lesson

REMINDERS
[bell] Daily practice reminder      [switch]
       Every day at 19:00           Change
       A gentle nudge on this phone. No email.

Pronunciation
Reset progress
Lingo · English, every day
Version 1.8
```

If Android blocks notifications, the row stays visible but the switch returns off and a short inline panel says: “Notifications are blocked. Open system settings to allow them.” with one action. The learner can always keep using Lingo without reminders.

### Notification

```
Lingo · now
A few minutes of English?
Your 4-day streak is waiting. Tap to practise.
```

No name, email, level, or call details. Tap opens the Practice tab; if signed out, the sign-in gate appears first.

### Empty and error states

- **All lessons complete:** hero switches to “Keep it warm” and practice, with the same ring and CTA.
- **Goal complete:** `NextStepCard` is hidden; the hero says “Daily goal complete”.
- **Reminder blocked:** inline explanation + system settings action; never a dead switch.
- **Offline call/sign-in:** existing error card with a retry action; lessons and progress still work.
- **Weekly test already taken:** card is replaced by the normal path; no “retake” promise.

## Visual checks

- 1080×2340 phone: 20 dp gutters, no horizontal scroll, bottom nav unobscured.
- 200% font scale: hero title wraps without clipping; buttons grow or wrap; no fixed-height text containers.
- Landscape: Home scrolls; hero ring and copy stay side by side only when width allows.
- TalkBack: logo, settings, stickers, switches, and cards have meaningful labels; section titles are headings.
- Contrast: `Muted` and `CoralDeep` token changes bring body and coral text above 4.5:1.

## Approval note

The user dismissed the discovery option card and asked to continue, so the recommended direction above is adopted: keep the Lingo identity, refine it, and add only the reminder and recommendation surfaces. No rebrand, no dark mode, no new font.
