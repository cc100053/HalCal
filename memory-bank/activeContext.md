# Active Context

Last updated: 2026-09-02

## Current product state

The branch is `main`. The current implementation includes:

- `AppPreferences`, `CalculatorEngine`, and `PracticeSession` as focused persistence/domain components.
- JVM coverage for calculator, parser, practice, soroban Kanji readings, tax, and unit conversion.
- Persistent bead-sound and haptic preferences. The rod-count and Japanese-TTS preferences, the settings rows that drove them, and the `TextToSpeech` owner in `MainActivity` have all been removed; the board is a fixed `SorobanEngine.ROD_COUNT` of 7.
- Safer calculator operator/history behavior, practice submission locking, tax validation, share failure handling, and sensor/share lifecycle fixes.
- Calculator repeat-equals/no-op handling, capped readable input, and rejection of non-finite loaded results.
- Practice answer auto-focus, explicit ready/active/finished phases, race-safe timer stopping, capped answer input, and a retained score/accuracy result screen.
- Soroban clear undo, single-flight share generation (including the zero state), fitted share-card text, responsive unboxed landscape control rail, visual reading guide, bounded snackbar, per-rod adjustable accessibility semantics, and disabled unavailable actions.
- Native Android Back handling in settings, full-row accessible settings toggles, rounded rod slider values, and current edge-to-edge system-bar handling.
- Tatami planner domain/UI removed; the approximate `畳` area conversion remains.
- A cohesive premium UI across calculator, tool sheets, practice, settings, history, and landscape soroban: refined light/dark palettes, serif/sans type hierarchy, procedural washi texture and ensō mark, shared card/pill/metric components, responsive safe-area handling, and a quiet unboxed control rail.
- The soroban instrument follows the 黒檀と骨 (Ebony & Bone) design handoff: ebony-lacquer frame with a brass inlay hairline, bone reckoning field, brass rods and diamond unit markers, and black-lacquer bi-conical beads with an equator ridge, specular sheen and contact shadow. `SorobanCanvas.kt` is written in the handoff's 768 x 352 coordinate space and scaled by the board's real height, which is also why `sorobanBoardAspect` now returns a wide, shallow board (2.18 at seven rods) rather than the old squarer one. The palette is fixed in both themes; `ShareUtility` uses a flattened version of it.
- Bead travel is the handoff's 360 ms `cubic-bezier(.2, 1.5, .34, 1)`, with the overshoot intact, and snaps instead when the device has animations turned off.
- Mode switching without a window rotation. The activity is pinned to portrait (`android:screenOrientation="portrait"`) and `MainActivity.TurnedFrame` draws the soroban turned a quarter turn, measured with the window's width and height swapped. Turning the phone and pressing そろばん/電卓 write the same boolean, so a button's choice simply stands until the phone is next turned; `requestedOrientation`, the hold, and the release are gone. The transition is a cross-fade the app owns, with each side of it keeping its own frame so the outgoing screen is never re-measured into the incoming shape.
- Soroban mode hides the system bars (swipe to reveal) — pinned in portrait they would otherwise run down a vertical edge with their text on its side — and `TurnedFrame` pads for the display cutout while consuming the rest of the insets.
- The cross-fade never lets anything show between the two screens. The incoming screen fades in over an outgoing one held at full opacity and dropped only once covered, and the root `Surface` is painted `colorScheme.background` rather than the lighter `colorScheme.surface`. Screen recordings of the earlier symmetric fade showed roughly five bleached frames where the outgoing screen had reached zero alpha while the incoming one was still around a third, letting the lighter surface through.
- The soroban guide is an inline overlay (`SorobanGuideOverlay`), and share failures use the existing snackbar. Both replaced window-owning equivalents (`AlertDialog`, `Toast`) that would have come up in the window's portrait orientation rather than the reader's.
- Washi `windowBackground` in `values/themes.xml` and `values-night/themes.xml`, matching `LightBg`/`DarkBg`.
- `DisplayFormat` in `domain`, formatting calculator text for reading only — parser glyphs to `×`/`÷`, spaced binary operators, thousands grouping — with the engine still working from its own raw strings. JVM-tested.
- A traditional-unit converter where any unit in a category can be tapped to become the input unit, backed by a `UnitSpec` table rather than per-pair conversion functions.
- A Japanese-only product interface. Default Android resources, accessibility descriptions, dates, history labels, errors, Kanji readings, and generated share cards are Japanese on every device locale. English resources and Romaji output have been removed.

`firebase-debug.log` is a local runtime artifact and must remain untracked.

## Last verification

- `./gradlew test assembleDebug lint` — **passed** on 2026-09-02.
- The turned-content design was verified on an API 36 Pixel 7 emulator on 2026-09-02 by driving the virtual accelerometer (`adb emu sensor set acceleration`): both sideways turns render the soroban the right way up, bead taps land correctly through the rotation, accessibility bounds are transformed, the そろばん button works from an upright phone and 電卓 from a sideways one, a button's choice survives until the phone is next turned, an upside-down phone stays on the calculator, a 45-degree hold keeps the current mode, rapid flapping settles correctly, the guide overlay and settings both render turned, back handling is ordered correctly, the bars stay hidden across a background/resume, and light and dark both render.
- The window is now 1080x2400 in every mode, which is the direct evidence that no window rotation happens any more. The old ghost came from the platform's own rotation transition (`snapshot=Surface(name=RotationLayer)`, `finishDrawing of orientation change ... 135ms`); `ROTATION_ANIMATION_JUMPCUT` had not suppressed it. Removing the rotation removed the snapshot.
- `./gradlew test assembleDebug lint` — passed on 2026-07-17 after the AI-selected hybrid landscape redesign.
- Landscape emulator verification on 2026-07-17 used an API 35 Medium Tablet AVD and covered the zero state, a multi-trillion active value, bead tap, clear, visual guide, zero-state sharing availability, light/dark themes, and a 560 dpi compact-height stress pass without clipping. The final visual comparison has no remaining P0, P1, or P2 findings.
- Emulator verification on 2026-07-16 covered practice keyboard focus and scored results, settings system-Back behavior, soroban clear/undo restoration, live accessibility descriptions, share-card creation and chooser launch, and light/dark system-bar rendering.
- Source audit found no user-visible English or Romaji literals.
- Emulator visual verification completed on 2026-07-16 using an API 36 Pixel 7 AVD with the device locale set to `en-US`. The calculator still rendered Japanese-only labels, including the new `全消` and `一字` keys, without clipping.
- Earlier emulator checks on API 35/36 covered portrait and landscape, light and dark modes, calculator, tax sheet, settings, responsive tablet keypad, safe drawing insets, and soroban rendering.
- Physical-device-only behavior (accelerometer shake, audible bead output, haptic feel, Android share chooser/file delivery, and the true feel of the rotation transition) still requires a real-device pass.

## Immediate cautions

- Do not restore the deleted Tatami planner or the removed text-to-speech readout unless the user explicitly requests them.
- Do not reintroduce `requestedOrientation` or an orientation hold. The stretched ghost that motivated them came from the system rotating the window; with the window pinned there is no rotation to race, and writing the mode state directly is now the correct and only path.
- Do not add a `Dialog`, `AlertDialog`, `ModalBottomSheet`, or `Toast` to a screen that can appear inside `TurnedFrame`. Each opens its own window in the app's portrait orientation, which is not the reader's. Use an inline overlay and the snackbar host.
- Keep the sensor's dead band. Committing on every reading would flap the mode for a phone held at an angle, and `physicalAngle` starting null is what lets a saved mode survive a recreation that happens inside that band.
- Do not make the mode transition a symmetric cross-fade, and do not let the root `Surface` keep its default colour. Either one puts a pale flash in the middle of the transition. Verify this with `adb shell screenrecord` and a frame contact sheet, not by eye.
- Keep calculator semantics in `CalculatorEngine` and practice submission rules in `PracticeSession`.
- Keep the interface Japanese-only; mathematical and international measurement symbols are the only intended Latin-symbol exceptions.
- Build currently emits the AGP/SDK compatibility warning documented in `techContext.md`.
- A test run may need permission to access the user's Gradle cache outside the repository sandbox.

## Likely next engineering opportunities

These are observations, not an approved roadmap:

- Add ViewModel tests for history, preferences, timers, and feature orchestration.
- Add Compose/instrumentation coverage for orientation, sheets, settings, and Japanese-only rendering.
- Decide whether to update AGP for official SDK 35 support.
- Modernize system-bar handling for current edge-to-edge Android APIs.
- Enable Room schema export and define a migration/testing policy before schema version 2.
- Consider typed feature/category/history-mode models if those contracts expand.

## Handoff checklist

Before ending a substantial future task:

1. Re-run relevant Gradle checks.
2. Record verification results and any new warnings here.
3. Update `progress.md` if capability or coverage changed.
4. Update durable context files if architecture, scope, or toolchain changed.
5. Commit and push every logical change, splitting distinct features into focused commits when useful.
6. Leave a concise account of manual device checks that remain.
