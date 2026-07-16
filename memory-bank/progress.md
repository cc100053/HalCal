# Progress

Last reviewed: 2026-07-16

## Capability status

| Area | Status | Notes |
| --- | --- | --- |
| Portrait calculator | Implemented | Four operations, decimals, clear controls, precedence, formatted results |
| Calculation history | Implemented | Room-backed newest-first list; normal and tax entries; load and clear actions |
| Landscape soroban | Implemented | 7–17 rods, tap/drag, spring animation, sound/haptics, decimal value, clear undo, accessibility value description |
| Japanese readings | Implemented | Kanji through `京`; core case unit-tested; Romaji removed from product and domain |
| Japanese TTS | Implemented | Preference-gated; device voice/data availability required |
| Shake reset | Implemented | Accelerometer listener with threshold and one-second cooldown |
| Share card | Implemented | Cached 1200×750 fitted PNG through `FileProvider`; duplicate jobs blocked; device chooser required |
| Tax tool | Implemented | 10%/8%, add/remove, yen round-down rules, history integration |
| Traditional units | Implemented | Metric input for length, area, volume, and weight |
| Practice mode | Implemented | 60-second add/subtract session with auto-focused answers, single-submit guard, and retained score/accuracy results |
| Settings persistence | Implemented | Rod count, bead sound, haptics, and TTS in SharedPreferences |
| Light/dark theme | Implemented | Refined washi/charcoal schemes; emulator-verified in both modes |
| Responsive UI system | Implemented | Shared washi, ensō, card, pill, header, and metric components; safe insets and tablet keypad cap |
| Japanese-only interface | Implemented | Default resources, dates, history modes, errors, accessibility copy, and share card remain Japanese on every device locale |
| Tatami planner | Removed | Deleted in current working tree; approximate `畳` conversion remains |

## Automated coverage

Current local JVM test classes:

- `CalculatorEngineTest`: operator replacement and negative history-result loading.
- `MathEvaluatorTest`: precedence and malformed parentheses.
- `PracticeSessionTest`: duplicate submission guard and blank input.
- `SorobanEngineTest`: four-digit Japanese Kanji units.
- `TaxCalculatorTest`: invalid input and add/remove rounding behavior.
- `UnitConverterTest`: representative round trips.

All pass under `./gradlew test` as of 2026-07-16, including Japanese calculator error display, repeat-equals stability, no-op equals, finite history loading, and readable input-length coverage.

`assembleDebug` and `lint` also pass as of 2026-07-16. Emulator visual checks covered phone/tablet sizing, portrait/landscape orientation, light/dark themes, calculator, tax, settings, practice focus/results, soroban undo/accessibility, and share generation. A fresh API 36 Pixel 7 check under an `en-US` device locale confirmed that the app still renders Japanese-only labels without clipping.

## Coverage gaps

- No tests for `ZenViewModel`, Room DAO/database behavior, or SharedPreferences persistence.
- No Compose UI or Android instrumentation tests.
- No automated coverage of canvas hit-testing/animation, rotation transitions, sensors, haptics, sounds, TTS, sharing, theme changes, or localization rendering.
- Domain edge-case coverage is intentionally small: calculator repeat/result flows, `Long` boundaries, more Japanese readings, invalid unit input, and timer races merit tests when those areas change.
- Physical-device verification remains for sensors, haptic character, sound/TTS output, and end-to-end image sharing.

## Known technical debt

- `ZenViewModel` coordinates all features and may become a maintenance hotspot.
- Several UI mode/category/history identifiers are raw strings.
- Some imports/dependencies appear unused, including Navigation Compose without a navigation graph.
- Room schema export/migrations are not configured.
- Backup rules are still template defaults.
- A build warning remains for the AGP/compile SDK pairing.
- There is no continuous-integration pipeline.
