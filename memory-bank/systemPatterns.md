# System Patterns

## Runtime map

```text
MainActivity
├── HistoryDatabase singleton ──> HistoryDao ──> Room calculation_history
├── AppPreferences ───────────────────────────> SharedPreferences
├── OrientationEventListener (phone attitude, onStart/onStop)
└── ZenViewModel (activity scoped)
    ├── CalculatorEngine ──> MathEvaluator
    ├── SorobanEngine
    ├── TaxCalculator
    ├── StateFlow UI state
    └── viewModelScope database jobs
        │
        ├── upright ──> CalculatorScreen
        │                └── tax / units / history sheets
        ├── sideways ─> TurnedFrame ──> SorobanScreen
        │                ├── SorobanCanvas
        │                └── ShakeResetListener
        └── override ───> SettingsScreen
```

## Ownership boundaries

### Activity

`MainActivity` constructs the Room database, preferences wrapper, and `ZenViewModelFactory`; applies the Compose theme; owns `OrientationEventListener` and the mode state it drives; hides and restores the system bars; and chooses the top-level screen from mode/settings state, wrapping it in `TurnedFrame` when the phone is sideways. There is no dependency-injection framework and no `NavHost`.

### ViewModel

`ZenViewModel` is the feature coordinator and single UI state holder. It exposes immutable flows, delegates deterministic work to domain classes, and persists history/settings. A mode change is now only a state change inside one composition, so nothing about it can disturb ViewModel state.

### Domain

- `CalculatorEngine`: stateful keypad semantics and display formatting.
- `MathEvaluator`: stateless recursive-descent parser returning `Double.NaN` for invalid expressions.
- `SorobanEngine`: stateless Japanese Kanji conversion.
- `TaxCalculator`: validated `BigDecimal`-based tax breakdowns.
- `UnitConverter`: stateless conversion constants/functions.

### Data

- `HistoryEntity` maps to `calculation_history` with generated ID, expression, result, mode string, and timestamp.
- `HistoryDao` exposes newest-first history as `Flow` and suspend insert/clear operations.
- `HistoryDatabase` is a process singleton named `soroban_zen_database`, schema version 1, without exported schemas or declared migrations.
- `AppPreferences` wraps `soroban_zen_preferences` and synchronously updates the sound and haptics settings.

### UI and platform components

Composables collect ViewModel flows and forward actions. `SorobanCanvas` owns only transient bead animation state; authoritative rod values remain in the ViewModel. `ShakeResetListener` owns sensor registration through `DisposableEffect`.

## Important invariants

- `rodsCount` is the constant `SorobanEngine.ROD_COUNT` (7); it is no longer user-adjustable.
- A rod value is coerced to `0..9`.
- `rodValues` is copied before mutation so `StateFlow` observers receive a new array instance.
- Soroban numeric value is formed left-to-right as base 10 and fits in `Long`.
- Calculator parser errors and division by zero become `Double.NaN`; calculator UI converts non-finite results to `Error` and does not save them.
- The window is pinned to portrait and never rotates. The screen shown is a function of one boolean in `MainActivity`, written both by `OrientationEventListener` and by the そろばん/電卓 buttons; `requestedOrientation` is never set.
- `TurnedFrame` is the only place that turns content: it measures the child with the window's width and height swapped, draws it rotated, pads for the display cutout, and consumes the remaining insets so the screens inside do not pad the wrong physical edge.
- A screen that can appear inside `TurnedFrame` must not open a window of its own. Dialogs, bottom sheets, and toasts come up in the window's portrait orientation, not the reader's.
- Drawn texture — the washi sheet, the ebony grain — comes from a fixed `Random(seed)` pool built once at class load, never from randomness inside a draw body. A draw body runs every frame, so a roll there would make the surface crawl.
- Soroban undo restores a size-matched defensive copy of the previous rod state, then recomputes the numeric value.

## Extension guidance

- Add a pure domain type before expanding ViewModel/composable logic for a rule-heavy feature.
- If `ZenViewModel` grows further, split by feature only with a deliberate navigation/state-owner design; do not create competing sources of truth casually.
- Prefer typed enums/sealed types over adding new magic mode/category strings. Existing history modes and tool/category tokens are legacy string contracts.
- A future Room schema bump should enable schema export and add migrations plus migration tests.
