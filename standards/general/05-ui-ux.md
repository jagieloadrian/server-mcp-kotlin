# UI and UX

Built on Material Design 3, Laws of UX, Baymard Institute and
WCAG 2.2. Every rule has a number or a test, so it can be checked. 
Compose units: `sp` for text, `dp` for everything else. Web: `rem`.

## Typography

Use 5–6 roles from the M3 type scale through `MaterialTheme.typography`, never raw sizes in screens.

| M3 role | Size / line height | Weight | Use |
|---------|--------------------|--------|-----|
| Display S | 36 / 44 | 400 | Big numbers, welcome screen. Rarely |
| Headline M | 28 / 36 | 400 | Screen title |
| Title L | 22 / 28 | 400 | Top app bar, dialog title |
| Title M | 16 / 24 | 500 | Card or section header |
| Body L | 16 / 24 | 400 | Default running text |
| Body M | 14 / 20 | 400 | Supporting text, lists, tables |
| Label L | 14 / 20 | 500 | Buttons, tabs |
| Label M | 12 / 16 | 500 | Chips, badges. The minimum for anything |

- Running text at least 16 sp (Body L), never below 14 sp on mobile. 11–12 sp only for short labels, never for
  sentences.
- Line height 1.4–1.6× for text, 1.1–1.3× for headings.
- Line length 45–75 characters. On wide screens cap the column width (`widthIn(max = ...)`, `max-width: 68ch`).
- Build hierarchy with size and weight, not colour: at least 1.25× size between levels, or a weight jump from 400
  to 600.
- No weights 100–300 below 24 sp.
- Left-align text. Centre only one or two lines (empty state, success screen).
- Sizes in `sp` / `rem` so system text scaling works. Check the layout at 200 %.
- At most two typefaces: a brand face for Display and Headline, a plain face for Body and Label.
- Emphasized styles only for a selected list or menu item, an unread message or the main button.
- Sentence case everywhere: "Add new project", not "Add New Project". No ALL CAPS text.

## Colour and contrast

Colours come from M3 roles (`MaterialTheme.colorScheme`), never picked per element. Each background role has an
`on…` pair for the text and icons on it.

| Role | Use |
|------|-----|
| `primary` / `onPrimary` | The main action: one filled button, FAB, active state |
| `primaryContainer` | Quieter emphasis: selected chip, active navigation item |
| `secondaryContainer` | Less urgent elements: tonal button, filter, selected navigation icon |
| `tertiaryContainer` | Accent that balances primary and secondary: badge, highlighted input |
| `error` / `errorContainer` | Errors only. No red as decoration |
| `surface` / `onSurface` | Background and main text. Most of the screen |
| `surfaceContainer` / `onSurfaceVariant` | Cards, panels, secondary text. Still at least 4.5:1 |

- WCAG AA: 4.5:1 for text, 3:1 for text at least 24 px (or 18.5 px bold), 3:1 for icons, field borders and focus
  indicators. Disabled states are exempt.
- Combine roles only in their intended pairs. Text on `primary` is `onPrimary`. `…Container` roles are fills,
  never text or icon colours.
- Buttons in a group need 3:1 between their container and the background. A standalone element (FAB) does not.
- Never carry information by colour alone. Error = red + icon + text. Status = colour + label.
- One accent colour (primary) for actions. Semantic colours (error, warning, success) are separate and reserved.
- Dark mode is not an inversion: dark grey background (`#121212`–`#1A1C1E`), lighter and less saturated accents,
  elevation shown by a lighter surface instead of a shadow.
- Generate the palette from one seed colour (Material Theme Builder or the generated palette in the theme), do
  not pick colours by hand.

## Layout and spacing

All dimensions are multiples of 4 dp, most of them of 8 dp. Related elements sit closer than unrelated ones (law
of proximity). Define the scale once in the theme and use only its values.

| Spacing | Between |
|---------|---------|
| 4 dp | Icon and its label |
| 8 dp | Label and field |
| 16 dp | Card padding, mobile margin |
| 24 dp | Form fields |
| 32 dp | Groups |
| 48 dp | Sections |

| Breakpoint | Width | Typical device | Panes | Navigation |
|------------|-------|----------------|-------|------------|
| Compact | < 600 dp | Phone, portrait | 1 | Navigation bar (bottom) |
| Medium | 600–839 dp | Tablet portrait, open foldable | 1 (recommended) or 2 | Navigation bar or modal expanded rail |
| Expanded | 840–1199 dp | Phone and tablet landscape | 1 or 2 (recommended) | Expanded rail, modal or standard |
| Large | 1200–1599 dp | Desktop | 1 or 2 (recommended) | Expanded rail, modal or standard |
| Extra-large | ≥ 1600 dp | Desktop, ultrawide | 1–3 (recommended) | Expanded rail, modal or standard |

- Design from compact up. On bigger windows add panes (list + detail) instead of stretching one column. A bigger
  window is not "the same, wider".
- Align to shared edges. Every element sits on one of a few vertical lines.
- Group by spacing, not borders. Use explicit grouping (outline, divider, shadow) only when the content is a
  separate object or is clickable.
- Same spacing between similar elements; aligned thumbnails, avatars and leading icons. Buttons sit close to the
  content they act on.
- Fixed focal points: key actions and information in the same place on every screen. The most important content
  gets the most space and the brightest surface.
- Shape scale: 0, 4, 8, 12, 16, 20, 28, 32, 48 dp and full. No large radii on dense data cards.
- Nested radius = outer radius − padding (48 − 14 = 34 dp). The same radius inside and outside looks crooked.
- Elevation levels 0–5. At rest elements sit on 0–3; 4–5 are for states such as dragging. Separate surfaces by
  tone, not shadow. Modal scrim at 32 % opacity.

## Interaction and states

States are shown by a translucent state layer in the content colour.

| State | M3 value | Notes |
|-------|----------|-------|
| Hover | 8 % | Pointer only; no important information only on hover |
| Focus | 10 % + outline | Visible focus outline, contrast at least 3:1 |
| Pressed | 10 % + ripple | Immediate response to touch |
| Dragged | 16 % | Plus raised elevation |
| Disabled | 38 % | Explain why it is disabled, or do not disable it |

- Touch targets at least 48 × 48 dp (about 9 mm), even for a 24 dp icon (`minimumInteractiveComponentSize()`).
  Mouse and stylus at least 44 × 44 dp. At least 8 dp between targets.
- One state layer at a time, in the element's content colour (usually the `on…` role).
- One emphasized action per screen. Button hierarchy: Filled → Tonal → Outlined → Text. A destructive action is
  never the default.
- Visual response within 100 ms, result within about 400 ms or a progress indicator (Doherty threshold).
- Links look like links and buttons like buttons. Do not underline text that is not clickable.
- Every gesture has a visible alternative (swipe to delete + a menu item).
- Respect reduced motion. Animations take 150–300 ms and explain a change; they are not decoration.
- Undo instead of asking. A "Deleted · Undo" snackbar beats an "Are you sure?" dialog for reversible actions.

## Forms and flows

Consistency first (Baymard): similar elements look the same, navigation stays in one place, more important text
is bigger and heavier. Navigation answers: where am I, what is here, how do I find what I need. An average
checkout form has 11.8 fields, and most sites can remove 20–60 % of them.

Do:

- Keep the label visible, above the field.
- One column of fields.
- Mark optional fields (or mark required fields with an asterisk and a legend).
- Validate when the field loses focus, not while typing.
- Put the error under the field: what is wrong and how to fix it.
- Use the right keyboard: `KeyboardOptions(keyboardType = KeyboardType.Email)`, `type="email"`,
  `inputmode="numeric"`.
- Enable autofill (`autocomplete="email"`, `"tel"`).
- Make the field width suggest the answer length.
- Submit a single-action form with Enter.

Do not:

- Use a placeholder as the only label.
- Clear the input after an error.
- Show a generic "Invalid data" at the top of the page.
- Force a format (spaces in a phone number) that can be accepted and normalized.
- Ask for data that is not needed now.
- Put "Clear" next to "Submit".
- Disable the submit button without saying what is missing.

## Content and feedback

People scan, they do not read. Short, concrete text in the user's words does more than another graphic.

- Name things the way the user does, not the way the database does ("Notifications", not "Webhook configuration").
- A button says what will happen: "Create project", "Send invoice". Not "OK", "Submit", "Next".
- The first two or three words carry the meaning of every heading, label and list item.
- Every state is designed: loading (skeleton, not a blank screen), empty (what will be here and how to add the
  first item), error (what happened and how to fix it), success, partial data.
- Error messages neither apologise nor blame: "Could not save, no connection. Try again" instead of "Error 500".
- Describe consequences neutrally: what happens after the action and how to undo it. No scaring, no
  condescension, no pushing a decision.
- Write words in full. Avoid abbreviations ("e.g.", "etc.", "pcs") when there is room.
- System status is always visible: what is saving, how many steps are left, whether data is current.
- Consistency over cleverness: the same pattern, icon and word for the same thing across the app.

Five-second test: show the screen for five seconds and ask what it is and what you can do there. If the answer
does not come, fix the hierarchy before touching colours or icons.

## Accessibility and structure

- Headings follow the content hierarchy, not the looks. One H1 per page, no skipped levels. Compose:
  `Modifier.semantics { heading() }`.
- Order in code = reading order. Screen readers follow the code, not the visual layout.
- Web landmarks: `header`, `nav`, `main` (one), `aside`, `footer`, `form`, `search`. Several `nav` elements each
  get a unique label.
- Labels describe the purpose, not the looks: "Search by voice", not "microphone". Do not add the role name ("Save
  button" is read as "Save button, button").
- Labels are required for icon-only buttons, generic links ("Learn more"), repeated "Save" buttons and status
  icons (`contentDescription`). Hide decorative images (`contentDescription = null`).
- Important actions at the top or bottom of the screen; elements of similar weight next to each other.
- Support zoom and text scaling without the layout falling apart.

## Laws of UX

Applied above: law of proximity (spacing) and the Doherty threshold (400 ms). The full set of 30 laws is at
[lawsofux.com](https://lawsofux.com/).

## Checklist before release

Go through every new screen:

- [ ] Body text at least 16 sp, nothing below 12 sp, no weight under 400 below 24 sp.
- [ ] Text contrast at least 4.5:1; large text, icons, field borders and focus at least 3:1.
- [ ] Colour is never the only signal.
- [ ] One emphasized action; no destructive default.
- [ ] Touch targets at least 48 dp, at least 8 dp apart.
- [ ] All spacing comes from the 4 / 8 dp scale.
- [ ] Works at compact width and at 200 % text size.
- [ ] Every field has a visible label; errors sit under the field and say how to fix them.
- [ ] Loading, empty, error and success states are designed.
- [ ] Buttons are named by their outcome, in sentence case.
- [ ] Icon-only buttons have a `contentDescription`; decorative images have none.
- [ ] Reversible actions offer undo instead of a confirmation dialog.
- [ ] The five-second test passes.

## References

- [Material Design 3](https://m3.material.io/)
- [Laws of UX](https://lawsofux.com/)
- [Baymard Institute: UX principles](https://baymard.com/blog/ux-design-principles)
- [WCAG 2.2](https://www.w3.org/TR/WCAG22/)
