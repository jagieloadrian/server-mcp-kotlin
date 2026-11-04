Readable Interface

UI/UX rules for my applications

1. [01 Diagnosis](#diagnosis)
2. [02 Typography](#typography)
3. [03 Color and contrast](#color)
4. [04 Layout and spacing](#layout)
5. [05 Interaction and states](#interaction)
6. [06 Forms](#forms)
7. [07 Content and feedback](#content)
8. [08 Accessibility and structure](#accessibility)
9. [09 UX laws](#laws)
10. [10 Checklist](#checklist)

Design guide

# An interface you can read the first time

A set of concrete rules: sizes, contrasts, spacing and behavior patterns. Collected from Material Design 3, the Laws of UX, Baymard Institute research and general UI/UX best practices. Every rule has a number or a test, so it can be checked, not just agreed with.

- [M3 Material Design 3](https://m3.material.io/)
- [LAW Laws of UX](https://lawsofux.com/)
- [BAY Baymard: UX principles](https://baymard.com/blog/ux-design-principles)
- [BP UI/UX best practices (Medium)](https://medium.com/@orbix.studiollc/ui-ux-design-best-practices-guidance-6460067d895e)

Content from M3, Laws of UX and Baymard checked against the source pages (as of October 2026). The Medium article blocks automated fetching, so it was not verified.

## 01 Where unreadability comes from

The same form in two versions. On the left, the typical mistakes that make a screen feel "somehow hard to read". On the right, the same content after applying the rules from this guide.

Before: 7 problems

New project

fill in the details below to continue the creation process

Error!

- **1.** Gray text on a gray background: contrast about 2:1
- **2.** Text 9–11 px, weight 300
- **3.** Placeholder instead of a label disappears once you type
- **4.** No hierarchy: everything centered, same weight
- **5.** Three button colors, none of them primary
- **6.** Touch targets about 20 px high
- **7.** "Error!" doesn't say what to fix

After: compliant with the rules

New project

This will take less than a minute.

Project name Enter a name, e.g. "Shop – version 2".

Budget (optional) You can add it later.

## 02 Typography

The most common cause of unreadability is text that is too small, a weight that is too thin and no clear levels. The M3 scale has 5 roles (Display, Headline, Title, Body, Label) with 3 sizes each, in two sets: 15 baseline styles and 15 **emphasized** styles (heavier). M3 states outright that no product uses all of them; pick 5–6 and stick to them across the whole app.

| M3 role | Size / line height | Weight | Used for |
| --- | --- | --- | --- |
| Display S | 36 / 44 | 400 | Large numbers, welcome screen. Rarely. |
| Headline M | 28 / 36 | 400 | Screen title |
| Title L | 22 / 28 | 400 | Top app bar, dialog title |
| Title M | 16 / 24 | 500 | Card or section header |
| Body L | 16 / 24 | 400 | Default running text |
| Body M | 14 / 20 | 400 | Supporting text, lists, tables |
| Label L | 14 / 20 | 500 | Buttons, tabs |
| Label M | 12 / 16 | 500 | Chips, badges. Minimum for anything. |

### Rules

- **Running text min. 16 px** (Body L), never below 14 px on mobile. 11–12 px only for short labels, not for sentences.
- **Line height 1.4–1.6×** for text, 1.1–1.3× for headings.
- **Line length 45–75 characters.** On a wide screen, limit the column (e.g. `max-width: 68ch`) instead of stretching text across the full width.
- **Build hierarchy with size and weight, not color.** Between levels, at least about 1.25× size difference or a weight jump of 400 → 600.
- **Avoid weights 100–300** in text below 24 px; they disappear on low-density screens.
- **Align text to the left.** Center only 1–2 lines (empty state, success screen).
- **Sizes in `sp`/`rem`,** so system text scaling works. Check the layout at 200%.
- **Maximum two typefaces.** M3 splits them into *brand* (Display, Headline: brand expression) and *plain* (Body, Label: readability). Both default to Roboto.
- **Emphasized styles sparingly:** for a selected list or menu item, an unread message, the primary button. Not for regular text.
- **Capital letter only at the start** (sentence case) in all titles, labels and buttons: "Add new project", not "Add New Project". Avoid text written in ALL CAPS.

## 03 Color and contrast

In M3, color is not picked "by eye" for each element, but assigned to a **role**. There are 26 standard roles in six groups: primary, secondary, tertiary, error, surface and outline. Every background role has an `on-…` pair for the text and icons placed on it. The pairs give at least 3:1, but only when you combine them as intended.

**primary / on-primary**: The main action of the screen: one filled button, FAB, active state.

**primary-container**: Less insistent emphasis: selected chip, active navigation item.

**secondary-container**: Less urgent elements: tonal button, filter, selected navigation icon.

**tertiary-container**: Accent balancing primary and secondary: badge, highlighted input field.

**error / error-container**: Errors only. Don't use red for decoration.

**surface / on-surface**: Background and main text. Most of the screen.

**surface-container / on-surface-variant**: Cards, panels; secondary text. Still ≥ 4.5:1.

### Check the contrast of your colors

Text color

Background color

Card title 24 px

Supporting text 14 px, this is what a description under a field looks like.

2.62:1

**Regular text**: requires 4.5:1

**Large text ≥ 24 px**: requires 3:1

**Icons, borders**: requires 3:1

### Rules

- **WCAG AA:** 4.5:1 for text, 3:1 for text ≥ 24 px (or ≥ 18.5 px bold), 3:1 for icons, field borders and focus indicators. Disabled states don't have to meet contrast requirements.
- **Combine roles only in their intended pairs.** Text on `primary` is `on-primary`, not `primary-container`. `…-container` roles are fills; don't use them as a text or icon color.
- **Buttons in a group need 3:1 between the container and the background,** so they can be told apart. A standalone, prominent element (e.g. FAB) doesn't need this.
- **Never convey information by color alone.** Error = red + icon + text. Status = color + label.
- **One accent color** (primary) for actions. Semantic colors (error, warning, success) are separate and reserved.
- **Dark mode is not an inversion.** Dark gray background (e.g. `#121212`–`#1a1c1e`), not pure black; accents lighter and less saturated; show elevation with a lighter surface, not a shadow.
- **Generate the palette from one color** (Material Theme Builder) instead of picking colors by hand.

## 04 Layout and spacing

A screen feels chaotic when spacing is random. All dimensions are multiples of 4 dp, and most of them of 8 dp. Related elements sit closer to each other than unrelated ones (law of proximity).

**4** icon ↔ its label

**8** label ↔ field

**16** card padding, mobile margin

**24** between form fields

**32** between groups

**48** between sections

### M3 breakpoints (formerly window size classes)

| Breakpoint | Width | Typical device | Panes | Navigation |
| --- | --- | --- | --- | --- |
| Compact | \< 600 dp | Phone in portrait | 1 | Navigation bar (bottom) |
| Medium | 600–839 dp | Tablet in portrait, unfolded foldable | 1 (recommended) or 2 | Navigation bar or modal expanded rail |
| Expanded | 840–1199 dp | Phone and tablet in landscape | 1 or 2 (recommended) | Expanded rail, modal or standard |
| Large | 1200–1599 dp | Desktop | 1 or 2 (recommended) | Expanded rail, modal or standard |
| Extra-large | ≥ 1600 dp | Desktop, ultra-wide monitor | 1–3 (recommended) | Expanded rail, modal or standard |

When moving to a larger breakpoint, M3 asks you to answer these questions: what to reveal, how to divide the screen, what to enlarge, rearrange or swap for a different component. A bigger window doesn't mean "the same thing, just wider".

### Rules

- **Design from compact up.** On larger screens add panes (list + detail) instead of stretching one column.
- **Align to shared edges.** Every element on the screen should sit on one of a few vertical lines.
- **Group with spacing, not borders.** M3 distinguishes implicit grouping (proximity and white space) from explicit grouping (outline, divider, shadow). Use explicit grouping where the content is a separate object or is clickable.
- **Rhythm:** the same spacing between similar elements, aligned thumbnails, avatars and icons at the start of rows. Buttons sit close to the content they act on.
- **Fixed focal points.** Key actions and information in the same place on every screen. The most important content gets the most space and the brightest surface.
- **The M3 corner radius scale has 10 steps:** 0, 4, 8, 12, 16, 20, 28, 32, 48 dp and full. Don't give large radii to dense data cards, because they clip content.
- **Nested corner radii:** inner radius = outer radius − padding (e.g. 48 − 14 = 34 dp). The same radius inside and outside looks crooked.
- **Elevation sparingly.** M3 has 6 levels (0 to +5); at rest, elements sit at 0–3, and 4–5 are for states like dragging. By default, surfaces are separated by a difference in tone, not a shadow. The scrim under a modal window has 32% opacity.

## 05 Interaction and states

The user must immediately see what is clickable, what is active right now and what just happened. M3 shows states with a semi-transparent **state layer** in the content color, overlaid on the element.

| State | M3 value | Notes |
| --- | --- | --- |
| Hover | 8% layer | Cursor only; don't move important information into hover |
| Focus | 10% + outline | Visible focus outline, contrast ≥ 3:1 |
| Pressed | 10% + ripple | Immediate response to touch |
| Dragged | 16% layer | Plus raised elevation |
| Disabled | 38% | Explain why it is inactive, or don't disable at all |

### Rules

- **Touch target min. 48 × 48 dp** (about 9 mm on any screen), even if the icon is 24 dp. For mouse and stylus min. 44 × 44 dp. Spacing between targets min. 8 dp. The state layer is 40 dp and sits inside the 48 dp target.
- **One state layer at a time,** in the element's content color (usually the `on-…` role).
- **One emphasized action per screen.** M3 button hierarchy: Filled → Tonal → Outlined → Text. A destructive action is never the default.
- **Response within 100 ms** to every action (visual state), result within about 400 ms or a progress indicator (Doherty threshold).
- **Links look like links,** buttons look like buttons. Don't underline text that isn't clickable.
- **Gestures have a visible alternative** (e.g. swipe-to-delete + a button in the menu).
- **Respect `prefers-reduced-motion`.** Animations 150–300 ms, they serve to explain a change, not to decorate.
- **Undo instead of asking.** A "Deleted · Undo" snackbar is better than an "Are you sure?" dialog for reversible actions.

## 06 Forms and flows

Baymard lists 10 practices every site should have: an established design process, a well-thought-out home page, simplicity and consistency, planned forms, accessibility, regular user testing, clear navigation, mobile, list filters and a simplified checkout. The numbers from their research show what neglect costs:

**11.8** fields in the average checkout form; most sites can remove 20–60% of them

**63%** of mobile users abandoned a product or site because of preventable problems

**94%** of 33 large sites had errors in 4 accessibility basics: links, images, fields, keyboard

**61%** of sites had a search that doesn't match how people actually type queries

**18%** of users abandoned an order because the checkout was too complicated

Baymard's most important principle is **consistency**: similar elements look the same, navigation is always in the same place, and more important text is larger and bolder. Users judge a site within seconds; if it is hard to read, they leave. Navigation should tell them: where am I, what is here, how do I find what I'm looking for.

### Form fields

##### Do

- Label always visible, above the field
- One column of fields
- Mark *optional* fields (and mark required fields with an asterisk with a legend)
- Validate on leaving the field, not while typing
- Error message under the field: what is wrong and how to fix it
- Right keyboard: `type="email"`, `inputmode="numeric"`
- Autofill: `autocomplete="email"`, `"tel"`…
- Field width hints at the answer length

##### Don't

- Placeholder as the only label
- Clearing entered data after an error
- A generic "Invalid data" at the top of the page
- Forcing a format (e.g. spaces in a phone number) that could be accepted and normalized
- Asking for data you don't need right now (every field lowers conversion)
- A "Clear" button next to "Submit"
- A disabled submit button with no information about what is missing

## 07 Content and feedback

People don't read an interface, they scan it. Short, concrete text in the user's words does more than another graphic element.

- **Name things the way the user says them,** not the way the database names them ("Notifications", not "Webhook configuration").
- **A button says what will happen:** "Create project", "Send invoice". Not "OK", "Confirm", "Next".
- **The first 2–3 words carry the meaning** of a heading, label and list item, because that's all you see when scanning.
- **Every state is designed:** loading (skeleton, not a blank screen), empty (what will be here + how to add the first item), error (what happened + how to fix it), success, partial data.
- **Error message:** no apologies and no blaming the user. "Couldn't save, no connection. Try again" instead of "Error 500".
- **Describe consequences neutrally** (M3): what will happen after the action and how to undo it. No scaring, no condescending tone and no manipulating the decision.
- **Write words in full.** Avoid abbreviations ("e.g.", "etc.", "pcs.") when there is room; they are harder to understand, also for screen readers.
- **System status always visible:** what is being saved, how many steps are left, whether the data is up to date.
- **Consistency over creativity.** The same pattern, the same icon and the same word for the same thing across the whole app.

**5-second test.** Show someone the screen for 5 seconds and ask: what is this screen and what can you do here? If they can't answer, the hierarchy needs fixing before you touch colors or icons.

## 08 Accessibility and structure

Readability is also how the screen is "read" by a screen reader and a keyboard. M3: every added button, image and line of text increases complexity, and key information should be visible at first glance.

- **Headings follow the content hierarchy, not the look.** One H1 per page, no skipping levels (H2 → H4).
- **Order in code = reading order.** A screen reader goes through the HTML from the top; CSS doesn't change that order.
- **Landmarks:** `header`, `nav`, `main` (one), `aside`, `footer`, `form`, `search`. When there are several `nav` elements, each gets a unique label.
- **Labels describe the purpose, not the look:** "Voice search", not "microphone". Don't add the role name ("Save button" will be read as "Save button, button").
- **A label is needed** for icon-only buttons, generic links ("Learn more"), repeated "Save" buttons and status icons. Hide decorative images from screen readers.
- **Important actions at the top or bottom of the screen,** elements of similar weight next to each other.
- **Support browser zoom.** Baymard: users with moderate vision impairments rely on zoom; the layout must not fall apart when they use it.

## 09 UX laws

All 30 principles from lawsofux.com, grouped by what they concern. For each one: how to apply it in an app.

## 10 Pre-release checklist

Go through every new screen. Checkmarks are saved in this browser.

0 / 0

Sources: Material Design 3 (m3.material.io: typography, color roles, contrast, breakpoints, spacing, states, shape, elevation, accessibility, UX writing), Laws of UX (lawsofux.com, Jon Yablonski), Baymard Institute: "The Best Practices and Key Principles of UX Design" (updated June 2026), WCAG 2.2. The Orbix Studio article on Medium was not verified.
