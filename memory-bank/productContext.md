# Product Context

## Primary experience

The device orientation is the top-level mode selector:

- **Portrait:** calculator home. Users can open history, settings, tax, traditional units, or practice as overlays/screens.
- **Landscape:** interactive soroban. Users manipulate rods, inspect numeric/Kanji readings, invoke Japanese TTS, clear, shake to reset, share an image, or open usage information.
- **Settings active:** settings temporarily replaces the orientation-selected screen until the user goes back.

`MainActivity` handles orientation changes itself through manifest `configChanges`; Compose observes `LocalConfiguration` and animates between the two modes.

## Feature behavior

### Calculator and history

- The keypad supports digits, one decimal point per operand, four basic operators, `C`, `AC`, and `=`.
- Consecutive operators replace the pending operator.
- Evaluation uses normal multiplication/division precedence. Parser support includes parentheses and unary signs even though the current keypad exposes no parentheses.
- Integral results are displayed without a decimal; fractional results are limited to four decimal places with trailing zeros removed.
- Successful normal calculations are inserted into Room history. Tax actions also create history entries. Current soroban changes are not stored in history.
- Selecting a history row loads its result into the calculator.

### Soroban

- Each rod is a decimal digit: the heaven bead contributes five and up to four earth beads contribute one each.
- Rods are ordered most-significant to least-significant from left to right.
- Japanese readings use four-digit Kanji units (`万`, `億`, `兆`, `京`). Romaji is intentionally not generated or shown.
- Sharing renders a fixed 1200×750 warm-paper PNG in the app cache, exposes it through `FileProvider`, and launches the Android chooser.

### Tax

- Standard tax is 10%; reduced tax is 8%.
- Adding tax rounds the tax amount down to a whole yen.
- Removing tax rounds the derived pre-tax amount down to a whole yen.
- Inputs must be finite and non-negative.

### Traditional units

- Length: metres to `尺`, `寸`, and `間`.
- Area: square metres to `坪`, with `畳` displayed as the rough convention `2 × 坪`.
- Volume: litres to `升` and `合`.
- Weight: kilograms to `貫` and `匁`.
- Domain utilities also provide reverse conversions, although the current UI is metric-input only.

### Practice

- A session lasts 60 seconds.
- Problems randomly use addition or subtraction with operands from 1 through 99.
- Subtraction is ordered so the answer is non-negative.
- Each problem accepts one submission, shows feedback, locks input for 1.2 seconds, then advances while the session remains active.

## Design language

- Wabi-sabi quiet luxury: warm, minimal, tactile, and deliberately restrained rather than decorative.
- Light palette: washi, sumi ink, moss green, aizome indigo, restrained sakura, ochre, and layered wood tones.
- Dark palette: charcoal paper and raised warm-black surfaces with softened natural pigments.
- System serif is reserved for expressive headings; system sans-serif handles controls, body copy, and numeric displays so Android can select reliable Japanese glyphs without bundled fonts.
- Shared Compose components provide the procedural washi texture, ensō mark, cards, choice pills, screen headers, and metric tiles. Functional UI does not depend on raster assets.
- Primary touch targets should remain at least 48dp-class, safe drawing insets must be respected, and wider portrait layouts cap the calculator keypad rather than scaling it indefinitely.
- Motion and feedback reinforce state changes without becoming busy: short fades for screen changes, spring bead motion, confirmation before destructive history clearing, and preference-gated haptics/sound/TTS.

## Localization state

The product interface is Japanese-only. Japanese strings live in the default `values/strings.xml`, so the app remains Japanese regardless of the device locale; there is no English locale overlay. User-visible copy, accessibility descriptions, history modes, dates, generated share cards, errors, and number readings must remain Japanese. Mathematical and international measurement symbols such as `m²`, `kg`, `+`, and `÷` are allowed.
