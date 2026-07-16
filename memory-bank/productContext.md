# Product Context

## Primary experience

The device orientation is the top-level mode selector:

- **Portrait:** calculator home. Users can open history, settings, tax, traditional units, or practice as overlays/screens.
- **Landscape:** interactive soroban. Users manipulate rods, inspect numeric/Kanji/Romaji readings, invoke Japanese TTS, clear, shake to reset, share an image, or open usage information.
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
- Japanese readings use four-digit units (`万`, `億`, `兆`, `京`) and include irregular Romaji pronunciations such as `sanzen`, `roppyaku`, and `hassen`.
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

- Wabi-sabi: quiet, warm, minimal, and tactile.
- Light palette: paper white, charcoal, moss green, indigo, sakura, and wood tones.
- Dark palette: matte black/charcoal with muted versions of the same accents.
- The system sans-serif stack is used so Android can select appropriate Japanese glyphs.
- Motion and feedback should reinforce state changes without becoming busy.

## Localization state

Android string resources exist for English (`values`) and Japanese (`values-ja`). Some screen copy and accessibility descriptions remain hard-coded or bilingual. New work should use resources in both locales and gradually remove hard-coded strings when touching nearby UI.
