# Design System Specification: The Meditative Canvas

## 1. Overview & Creative North Star

The core philosophy of this design system is **"The Meditative Canvas."** In designing a Japanese learning platform, we must move beyond the utility of a "tool" and create an environment that mimics the focus of a traditional _dojo_ or the serenity of a _karesansui_ (zen garden).

We break the "generic SaaS" mold by embracing **intentional asymmetry** and **high-contrast typography scales**. This system rejects the rigid, boxy layouts of standard ed-tech. Instead, we use expansive whitespace as a functional element—giving the user's mind "room to breathe" between complex kanji characters. Elements should feel like they are resting on fine paper or floating in a clear pond, rather than being trapped inside containers.

---

## 2. Colors & Surface Philosophy

The palette is rooted in soft neutrals with sophisticated tonal depth. We use a "Zen Blue" (`primary`) and a "Sakura Wood" (`secondary`) to provide weight and focus without inducing eye fatigue.

### The "No-Line" Rule

**Explicit Instruction:** You are prohibited from using 1px solid borders to define sections. Traditional borders create visual noise that disrupts the "Zen" state. Boundaries must be defined solely through:

- **Background Tonal Shifts:** A `surface-container-low` (#f2f4f4) card sitting on a `surface` (#f9f9f9) background.
- **Negative Space:** Using the spacing scale to separate concepts.

### Surface Hierarchy & Nesting

Treat the UI as a series of physical layers—like stacked sheets of frosted glass or hand-pressed paper.

- **Base Layer:** `surface` (#f9f9f9).
- **Secondary Content Areas:** `surface-container-low` (#f2f4f4).
- **Interactive/Primary Cards:** `surface-container-lowest` (#ffffff) to provide a soft "pop."
- **Nesting:** Never nest more than three levels deep. Each inner container should move one tier higher in the surface scale (e.g., a `surface-container-highest` search bar inside a `surface-container` navigation rail).

### The "Glass & Gradient" Rule

To elevate the platform from "clean" to "premium," use Glassmorphism for floating elements (like vocabulary popovers).

- **Recipe:** Semi-transparent `surface` color + `backdrop-blur: 12px`.
- **Signature Gradients:** For main CTAs and Hero progress states, use a subtle linear gradient transitioning from `primary` (#45617d) to `primary-container` (#cfe5ff) at a 135-degree angle. This adds a "soul" to the UI that flat colors cannot replicate.

---

## 3. Typography

We pair the geometric precision of **Plus Jakarta Sans** for headlines with the utilitarian clarity of **Inter** for body content.

- **The Hero Scale:** Use `display-lg` (3.5rem) for single Kanji characters. The sheer scale of the character against a vast `surface` background creates a moment of reverence for the language.
- **Editorial Hierarchy:** Use `headline-sm` (1.5rem) in `on-primary-fixed` (#25415c) for lesson titles. Pair this with `body-md` (0.875rem) in `on-surface-variant` (#5a6061) for descriptions to create a high-contrast, editorial feel.
- **Visual Breath:** Always increase line-height for body text (1.6 or higher) to ensure Japanese characters (which are more dense than Latin) remain legible.

---

## 4. Elevation & Depth

In this design system, depth is a whisper, not a shout. We replace structural lines with **Tonal Layering**.

### The Layering Principle

Achieve lift by "stacking." A `surface-container-lowest` card placed on a `surface-container-low` background creates a natural, soft lift without needing a single pixel of shadow.

### Ambient Shadows

When an element must float (e.g., a modal or a primary action button), use **Ambient Shadows**:

- **Color:** A tinted version of `on-surface` (#2d3435) at 4%–6% opacity.
- **Blur:** Large values (20px–40px) to mimic natural, diffused light. Avoid dark, tight "drop shadows" at all costs.

### Ghost Borders

If a border is required for accessibility (e.g., input states), use the **"Ghost Border"**:

- **Token:** `outline-variant` (#adb3b4) at 15% opacity. It should be felt rather than seen.

---

## 5. Components

### Buttons

- **Primary:** Pill-shaped (`rounded-full`). Background: `primary` (#45617d). Text: `on-primary` (#f5f8ff). Use the Signature Gradient on hover to create "energy."
- **Tertiary:** No background or border. Use `title-sm` typography with an icon. Padding should be generous (12px 24px) to ensure the hit area is comfortable.

### Learning Cards

- **Construction:** Use `surface-container-lowest` (#ffffff) with a `xl` (1.5rem) corner radius.
- **Rule:** Forbid divider lines. Use `body-sm` labels and 24px vertical padding to separate the Japanese term from its English definition.

### Input Fields

- **Style:** Use a "soft-fill" approach. Background: `surface-container-high` (#e4e9ea).
- **States:** On focus, transition the background to `surface-container-lowest` (#ffffff) and apply a Ghost Border of `primary` (#45617d) at 20% opacity.

### Selection Chips

- **Design:** Use `md` (0.75rem) rounded corners.
- **Unselected:** `surface-container-highest` (#dde4e5).
- **Selected:** `primary-container` (#cfe5ff) with `on-primary-container` (#385470) text.

### Custom Learning Components

- **The "Stroke Trace" Canvas:** A `surface-container-lowest` square with an `xl` corner radius, using a very faint `outline-variant` (10% opacity) grid to guide Kanji writing.
- **Focus Mode Toggle:** A glassmorphic bar that slides into view, blurring everything except the current study card.

---

## 6. Do's and Don'ts

### Do

- **Do** embrace asymmetry. A headline justified to the left with a primary action button floating far to the right creates a sophisticated, modern balance.
- **Do** use `display-sm` for numbers and progress percentages to make them feel like design elements.
- **Do** utilize the `secondary` (#7c5556) "Sakura" tones specifically for error states or "streak" reminders to keep the palette warm.

### Don't

- **Don't** use 100% black. The darkest color should be `on-surface` (#2d3435). Pure black is too aggressive for a Zen aesthetic.
- **Don't** use `none` or `sm` rounded corners unless for specific utility icons. This system thrives on the softness of `lg` and `xl` radii.
- **Don't** crowd the interface. If you feel like you need a divider line, you actually need more whitespace.
