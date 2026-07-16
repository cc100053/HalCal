# Progress

Last reviewed: 2026-07-16

## Capability status

| Area | Status | Notes |
| --- | --- | --- |
| Portrait calculator | Implemented | Four operations, decimals, clear controls, precedence, formatted results |
| Calculation history | Implemented | Room-backed newest-first list; normal and tax entries; load and clear actions |
| Landscape soroban | Implemented | 7–17 rods, tap/drag, spring animation, sound/haptics, decimal value |
| Japanese readings | Implemented | Kanji and Romaji through `京`; core cases unit-tested |
| Japanese TTS | Implemented | Preference-gated; device voice/data availability required |
| Shake reset | Implemented | Accelerometer listener with threshold and one-second cooldown |
| Share card | Implemented | Cached 1200×750 PNG through `FileProvider`; device chooser required |
| Tax tool | Implemented | 10%/8%, add/remove, yen round-down rules, history integration |
| Traditional units | Implemented | Metric input for length, area, volume, and weight |
| Practice mode | Implemented | 60-second add/subtract session with single-submit guard |
| Settings persistence | Implemented | Rod count, bead sound, haptics, and TTS in SharedPreferences |
| Light/dark theme | Implemented | Follows Android system theme |
| English/Japanese resources | Partial | Resource sets exist; some UI strings remain hard-coded/bilingual |
| Tatami planner | Removed | Deleted in current working tree; approximate `畳` conversion remains |

## Automated coverage

Current local JVM test classes:

- `CalculatorEngineTest`: operator replacement and negative history-result loading.
- `MathEvaluatorTest`: precedence and malformed parentheses.
- `PracticeSessionTest`: duplicate submission guard and blank input.
- `SorobanEngineTest`: four-digit Japanese units and irregular Romaji readings.
- `TaxCalculatorTest`: invalid input and add/remove rounding behavior.
- `UnitConverterTest`: representative round trips.

All pass under `./gradlew test` as of 2026-07-16.

## Coverage gaps

- No tests for `ZenViewModel`, Room DAO/database behavior, or SharedPreferences persistence.
- No Compose UI or Android instrumentation tests.
- No automated coverage of canvas hit-testing/animation, rotation transitions, sensors, haptics, sounds, TTS, sharing, theme changes, or localization rendering.
- Domain edge-case coverage is intentionally small: calculator repeat/result flows, `Long` boundaries, more Japanese readings, invalid unit input, and timer races merit tests when those areas change.
- `assembleDebug` and `lint` were not run during the memory-bank creation task; `test` did compile both debug and release unit-test variants.

## Known technical debt

- `ZenViewModel` coordinates all features and may become a maintenance hotspot.
- Several UI mode/category/history identifiers are raw strings.
- Some imports/dependencies appear unused, including Navigation Compose without a navigation graph.
- User-visible copy and content descriptions are not fully resource-backed.
- Room schema export/migrations are not configured.
- Backup rules are still template defaults.
- Build warnings exist for the AGP/compile SDK pairing and deprecated system-bar color APIs.
- There is no continuous-integration pipeline.
