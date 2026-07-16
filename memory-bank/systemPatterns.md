# System Patterns

## Runtime map

```text
MainActivity
├── HistoryDatabase singleton ──> HistoryDao ──> Room calculation_history
├── AppPreferences ───────────────────────────> SharedPreferences
├── TextToSpeech lifecycle owner
└── ZenViewModel (activity scoped)
    ├── CalculatorEngine ──> MathEvaluator
    ├── PracticeSession
    ├── SorobanEngine
    ├── TaxCalculator
    ├── StateFlow UI state / SharedFlow TTS events
    └── viewModelScope database jobs and timers
        │
        ├── portrait ──> CalculatorScreen
        │                └── tax / units / practice / history sheets
        ├── landscape ─> SorobanScreen
        │                ├── SorobanCanvas
        │                ├── ShakeResetListener
        │                └── ShareUtility
        └── override ───> SettingsScreen
```

## Ownership boundaries

### Activity

`MainActivity` constructs the Room database, preferences wrapper, and `ZenViewModelFactory`; owns `TextToSpeech`; applies the Compose theme; and chooses the top-level screen based on orientation/settings state. There is no dependency-injection framework and no `NavHost`.

### ViewModel

`ZenViewModel` is the feature coordinator and single UI state holder. It exposes immutable flows, delegates deterministic work to domain classes, persists history/settings, emits TTS events, and owns practice timer jobs. It intentionally survives the activity's handled orientation changes.

### Domain

- `CalculatorEngine`: stateful keypad semantics and display formatting.
- `MathEvaluator`: stateless recursive-descent parser returning `Double.NaN` for invalid expressions.
- `SorobanEngine`: stateless Kanji and Romaji conversion.
- `TaxCalculator`: validated `BigDecimal`-based tax breakdowns.
- `UnitConverter`: stateless conversion constants/functions.
- `PracticeSession`: stateful submission guard and score/total progression, independent of Android timers.

### Data

- `HistoryEntity` maps to `calculation_history` with generated ID, expression, result, mode string, and timestamp.
- `HistoryDao` exposes newest-first history as `Flow` and suspend insert/clear operations.
- `HistoryDatabase` is a process singleton named `soroban_zen_database`, schema version 1, without exported schemas or declared migrations.
- `AppPreferences` wraps `soroban_zen_preferences` and synchronously updates rod count, sound, haptics, and TTS settings.

### UI and platform components

Composables collect ViewModel flows and forward actions. `SorobanCanvas` owns only transient bead animation state; authoritative rod values remain in the ViewModel. `ShakeResetListener` owns sensor registration through `DisposableEffect`. `ShareUtility` owns bitmap/file/intent creation on `Dispatchers.IO`.

## Important invariants

- `rodsCount` is always `7..17`; changing it resets the rod array and current soroban value.
- A rod value is coerced to `0..9`.
- `rodValues` is copied before mutation so `StateFlow` observers receive a new array instance.
- Soroban numeric value is formed left-to-right as base 10 and fits in `Long` for the supported maximum of 17 rods.
- Calculator parser errors and division by zero become `Double.NaN`; calculator UI converts non-finite results to `Error` and does not save them.
- TTS only emits when the preference is enabled; the activity only speaks after successful Japanese initialization.
- Practice timer and delayed-next-problem jobs are cancelled on stop and `ViewModel.onCleared()`.
- Practice UI moves through explicit ready, active, and finished phases; an active sheet disposal completes the session instead of leaving a hidden timer running.
- Soroban undo restores a size-matched defensive copy of the previous rod state, then recomputes the numeric value.
- Shared images live only under `cacheDir/shared_images`, the exact path exposed by `file_paths.xml`.

## Extension guidance

- Add a pure domain type before expanding ViewModel/composable logic for a rule-heavy feature.
- If `ZenViewModel` grows further, split by feature only with a deliberate navigation/state-owner design; do not create competing sources of truth casually.
- Prefer typed enums/sealed types over adding new magic mode/category strings. Existing history modes and tool/category tokens are legacy string contracts.
- A future Room schema bump should enable schema export and add migrations plus migration tests.
