# Product Context

## Primary experience

How the phone is being held is the top-level mode selector:

- **Upright:** calculator home. Users can open history, settings, tax, traditional units, or practice as overlays/screens.
- **Sideways:** interactive soroban, full screen with the system bars hidden. Users manipulate rods, inspect numeric/Kanji readings, toggle the sign, clear, shake to reset, share an image, or open usage information.
- **Settings active:** settings temporarily replaces the mode-selected screen until the user goes back, and is drawn in whichever way the phone is being held.

The window never rotates. It is pinned to portrait (`android:screenOrientation="portrait"`) and the content turns instead: `MainActivity.TurnedFrame` measures the child with the window's width and height swapped and draws it rotated a quarter turn, so the soroban gets true landscape constraints out of a portrait window. Both modes therefore live in one window that never changes shape, and moving between them is a plain cross-fade the app owns end to end.

That is what removed the ghost. Asking the system to rotate produced a `RotationLayer` snapshot of the outgoing frame, drawn by the platform and not suppressible from the app; with no rotation requested there is no snapshot, no re-layout of the outgoing screen into the incoming shape, and nothing to show through.

Mode is a single piece of state. `OrientationEventListener` reports the phone's real attitude — the configuration cannot, since the window is pinned — and writes the quarter turn the content needs; the calculator's そろばん button and the soroban's 電卓 button write that same state directly. A button's choice therefore stands until the phone is next turned, with no orientation request to make and no hold to release. Readings within 35 degrees of an axis commit; the gap between leaves the mode alone, so a phone held at an angle cannot flap between the two screens. A phone held upside down still reads as upright.

One consequence to design around: anything that opens its own window — `Dialog`, `AlertDialog`, `ModalBottomSheet`, `Toast` — comes up in the window's portrait orientation rather than the reader's. Inside soroban mode, use an inline overlay (`SorobanGuideOverlay`) and the snackbar host instead.

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
- Clearing from the button or shake gesture offers a short undo action that restores the complete prior rod state.
- Share generation is single-flight and fits long numeric/Kanji readings within the generated card.

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
- Starting or advancing focuses the answer field and opens the numeric keyboard.
- Stopping or reaching zero seconds retains a score and accuracy result screen; dismissing or rotating away ends an active session cleanly.

## Design language

- Wabi-sabi quiet luxury: warm, minimal, tactile, and deliberately restrained rather than decorative.
- Light palette: washi, sumi ink, moss green, aizome indigo, restrained sakura, ochre, and layered wood tones.
- Dark palette: charcoal paper and raised warm-black surfaces with softened natural pigments.
- System serif is reserved for expressive headings; system sans-serif handles controls, body copy, and numeric displays so Android can select reliable Japanese glyphs without bundled fonts.
- Shared Compose components provide the procedural washi texture, ensō mark, cards, choice pills, screen headers, and metric tiles. Functional UI does not depend on raster assets.
- Primary touch targets should remain at least 48dp-class, safe drawing insets must be respected, and wider portrait layouts cap the calculator keypad rather than scaling it indefinitely.
- Motion and feedback reinforce state changes without becoming busy: short fades for screen changes, spring bead motion, confirmation before destructive history clearing, and preference-gated haptics and sound.

## Localization state

The product interface is Japanese-only. Japanese strings live in the default `values/strings.xml`, so the app remains Japanese regardless of the device locale; there is no English locale overlay. User-visible copy, accessibility descriptions, history modes, dates, generated share cards, errors, and number readings must remain Japanese. Mathematical and international measurement symbols such as `m²`, `kg`, `+`, and `÷` are allowed.
