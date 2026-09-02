# Progress

Last reviewed: 2026-09-02

## Capability status

| Area | Status | Notes |
| --- | --- | --- |
| Portrait calculator | Implemented | Four operations, decimals, clear controls, precedence, formatted results |
| Calculation history | Implemented | Room-backed newest-first list; normal and tax entries; load and clear actions |
| Landscape soroban | Implemented | Fixed 7 rods, tap/drag, 黒檀と骨 ebony/bone/brass/lacquer Canvas materials on the handoff's own geometry, seeded ebony grain on the frame and beam, overshoot bead travel that snaps under reduced motion, quiet unboxed control rail, visual reading guide, sound/haptics, clear undo, and adjustable per-rod accessibility semantics |
| Japanese readings | Implemented | Kanji through `京`; core case unit-tested; Romaji removed from product and domain |
| Japanese TTS | Removed | Voice readout, its preference, and the `TextToSpeech` owner were deleted from the current working tree |
| Mode switching | Implemented | Window pinned to portrait; the content turns instead. Sensor and そろばん/電卓 buttons write one mode state. The incoming screen fades in over a fully opaque outgoing one, so there is neither a platform rotation snapshot nor a bleached frame |
| Shake reset | Implemented | Accelerometer listener with threshold and one-second cooldown |
| Share card | Removed | The rail's 共有 button and everything that served it — `ShareUtility`, the manifest `FileProvider`, `file_paths.xml`, and the share strings — were deleted on request |
| Tax tool | Implemented | 10%/8%, add/remove, yen round-down rules, history integration |
| Traditional units | Implemented | Any unit in a category can be tapped to become the input unit; length, area, volume, and weight |
| Practice mode | Implemented | 60-second add/subtract session with auto-focused answers, single-submit guard, and retained score/accuracy results |
| Settings persistence | Implemented | Bead sound and haptics in SharedPreferences; rod count and TTS preferences removed |
| Light/dark theme | Implemented | Refined washi/charcoal schemes; emulator-verified in both modes |
| Responsive UI system | Implemented | Shared washi, ensō, card, pill, header, and metric components; safe insets, tablet keypad cap, and compact-height landscape handling |
| Japanese-only interface | Implemented | Default resources, dates, history modes, errors, and accessibility copy remain Japanese on every device locale |
| Tatami planner | Removed | Deleted in current working tree; approximate `畳` conversion remains |

## Automated coverage

Current local JVM test classes:

- `CalculatorEngineTest`: operator replacement and negative history-result loading.
- `MathEvaluatorTest`: precedence and malformed parentheses.
- `PracticeSessionTest`: duplicate submission guard and blank input.
- `SorobanEngineTest`: four-digit Japanese Kanji units.
- `TaxCalculatorTest`: invalid input and add/remove rounding behavior.
- `UnitConverterTest`: representative round trips.

All pass under `./gradlew test` as of 2026-07-17, including Japanese calculator error display, repeat-equals stability, no-op equals, finite history loading, and readable input-length coverage.

`assembleDebug` and `lint` also pass as of 2026-07-17. Emulator visual checks covered phone/tablet sizing, portrait/landscape orientation, light/dark themes, calculator, tax, settings, practice focus/results, soroban undo/accessibility, and share generation. A 2026-07-17 API 35 Medium Tablet pass covered the AI-selected hybrid soroban, zero and multi-trillion values, clear, the visual guide, compact-height rendering at 560 dpi, and zero-state share availability. Side-by-side design QA found no remaining P0, P1, or P2 visual issues. An earlier API 36 Pixel 7 check under an `en-US` device locale confirmed that the app still renders Japanese-only labels without clipping.

## Coverage gaps

- No tests for `ZenViewModel`, Room DAO/database behavior, or SharedPreferences persistence.
- No Compose UI or Android instrumentation tests.
- No automated coverage of canvas hit-testing/animation, the turned frame and its cross-fade, sensors, haptics, sounds, theme changes, or localization rendering.
- Domain edge-case coverage is intentionally small: calculator repeat/result flows, `Long` boundaries, more Japanese readings, invalid unit input, and timer races merit tests when those areas change.
- Physical-device verification remains for sensors, haptic character, sound output, and how the mode cross-fade reads in the hand while the phone is actually being turned.

## Known technical debt

- `ZenViewModel` coordinates all features and may become a maintenance hotspot.
- Several UI mode/category/history identifiers are raw strings.
- Some imports/dependencies appear unused, including Navigation Compose without a navigation graph.
- Room schema export/migrations are not configured.
- Backup rules are still template defaults.
- A build warning remains for the AGP/compile SDK pairing.
- There is no continuous-integration pipeline.
