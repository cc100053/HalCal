# Active Context

Last updated: 2026-09-03

## Current product state

The branch is `main`. The current implementation includes:

- `AppPreferences` and `CalculatorEngine` as focused persistence/domain components.
- JVM coverage for calculator, display formatting, parser, soroban Kanji readings and bead hit targets, tax, and unit conversion.
- Persistent bead-sound and haptic preferences. The rod-count and Japanese-TTS preferences, the settings rows that drove them, and the `TextToSpeech` owner in `MainActivity` have all been removed; the board is a fixed `SorobanEngine.ROD_COUNT` of 7.
- Safer calculator operator/history behavior, tax validation, and sensor lifecycle fixes.
- Calculator repeat-equals/no-op handling, capped readable input, and rejection of non-finite loaded results.
- Soroban clear undo, responsive unboxed landscape control rail, visual reading guide, bounded snackbar, per-rod adjustable accessibility semantics, and disabled unavailable actions.
- Native Android Back handling in settings, full-row accessible settings toggles, rounded rod slider values, and current edge-to-edge system-bar handling.
- Tatami planner domain/UI removed; the approximate `畳` area conversion remains.
- A cohesive premium UI across calculator, tool sheets, settings, history, and landscape soroban: refined light/dark palettes, serif/sans type hierarchy, procedural washi texture and ensō mark, shared card/pill/metric components, responsive safe-area handling, and a quiet unboxed control rail.
- The soroban instrument follows the 黒檀と骨 (Ebony & Bone) design handoff: ebony-lacquer frame with a brass inlay hairline, bone reckoning field, brass rods and diamond unit markers, and black-lacquer bi-conical beads with an equator ridge, specular sheen and contact shadow. `SorobanCanvas.kt` is written in the handoff's 768 x 352 coordinate space and scaled by the board's real height, which is also why `sorobanBoardAspect` now returns a wide, shallow board (2.18 at seven rods) rather than the old squarer one. The palette is fixed in both themes. The frame's four members and the beam carry a fine lengthwise ebony grain from a seeded pool, in the same shape `ZenComponents` uses for the washi sheet; the rails run the board's full width and the stiles sit between them, so each piece's grain runs along its own length.
- One deliberate departure from that handoff: `BEAM_TOP` is 110 rather than the specified 140. The lower deck carries a 135-unit stack of four beads against the upper deck's one, so at 140 each earth bead had 15 units of travel while the heaven bead had 66, and the lower deck read as packed. At 110 the earth beads travel 45 and the heaven bead 36. Do not "restore" the handoff value; the reasoning is on the constant.
- Bead travel is the handoff's 360 ms `cubic-bezier(.2, 1.5, .34, 1)`, with the overshoot intact, and snaps instead when the device has animations turned off.
- Mode switching without a window rotation. The activity is pinned to portrait (`android:screenOrientation="portrait"`) and `MainActivity.TurnedFrame` draws the soroban turned a quarter turn, measured with the window's width and height swapped. Turning the phone and pressing そろばん/電卓 write the same boolean, so a button's choice simply stands until the phone is next turned; `requestedOrientation`, the hold, and the release are gone. The transition is a cross-fade the app owns, with each side of it keeping its own frame so the outgoing screen is never re-measured into the incoming shape.
- Soroban mode hides the system bars (swipe to reveal) — pinned in portrait they would otherwise run down a vertical edge with their text on its side — and `TurnedFrame` pads for the display cutout while consuming the rest of the insets.
- The cross-fade never lets anything show between the two screens. The incoming screen fades in over an outgoing one held at full opacity and dropped only once covered, and the root `Surface` is painted `colorScheme.background` rather than the lighter `colorScheme.surface`. Screen recordings of the earlier symmetric fade showed roughly five bleached frames where the outgoing screen had reached zero alpha while the incoming one was still around a third, letting the lighter surface through.
- 使い方 is a chaptered, stepped guide (`ui/screens/UsageGuideSheet.kt`) following the 使い方 handoff, one idea per step, with a read-only five-rod board that animates between steps, an optional note chip and formula caption, step dots and 戻る/次へ/閉じる. The chapters are the skills a beginner picks up, in the order each one needs the last — 読み方 / 数を置く / 足す引く / 5をつかう / 10をつかう / かけ算・わり算 — not the four arithmetic operations. The handoff's operation chapters taught the 5- and 10-complements twice, in たし算 and again reversed in ひき算, under names that hid what was being learned; these name each technique as its own chapter and reach かけ算・わり算 only once everything it needs is in place. That last chapter teaches what the two operations are — 3 × 4 as 3 added four times, 12 ÷ 4 as 4 subtracted until nothing is left — on numbers small enough to watch. Partial products and quotient placement are a lesson of their own and are deliberately not here; they need every chapter above them first, and the jargon (部分積, 商, 一桁ずらす) was the whole reason the first draft of this chapter was unreadable to a beginner. 読み方 keeps its first three points verbatim and its fourth now reads a two-digit number, because every chapter after it carries between rods.
- The guide is one lesson in order: 次へ and 戻る run through the whole thing rather than stopping at a chapter's edge. On a chapter's last step the primary button turns ochre and names where it is going (「次は 数を置く」), so leaving a chapter is never a surprise; only the last step of the last chapter turns back into もう一度. The header carries the 使い方 title alone; the handoff's romanized chapter name is not shown. The lesson content is data (`LESSONS`, string resources plus the exact rod state per step), so a copy review lands in `strings.xml` without touching layout — and that review, by someone who teaches soroban, has not happened yet. The deliberate departures from the handoff: the sheet has one fixed height for every step (560 rather than content-driven ~600) so the footer never reflows, and it is laid out at its design size with `requiredWidth`/`requiredHeight` and scaled as one piece to whatever the turned frame has room for. There is deliberately no rail tab for it: the rail is the mode switch, and a teaching tab there would read as a fifth mode.
- The guide draws the real instrument, not a smaller lookalike. `BoardMetrics` holds the board's geometry for one size and rod count, and `drawInstrument` draws frame, grain, inlay, field, rods, beam, markers and beads from it; `SorobanCanvas` (interactive, seven rods) and `SorobanGuideBoard` (read-only, five rods, plus the focus wash) are the same drawing at two sizes and cannot drift apart. The guide's board is also the only caller that passes a wash.
- The guide is an inline overlay rather than an `AlertDialog`, which would have opened a window of its own in the app's portrait orientation rather than the reader's. Any future transient message in soroban mode belongs in the existing snackbar host for the same reason, never a `Toast`.
- Like the instrument, the guide sheet is a designed surface with a fixed paper/ink/ochre palette in both themes rather than a themed one, and its constants are written in the handoff's coordinates.
- Washi `windowBackground` in `values/themes.xml` and `values-night/themes.xml`, matching `LightBg`/`DarkBg`.
- `DisplayFormat` in `domain`, formatting calculator text for reading only — parser glyphs to `×`/`÷`, spaced binary operators, thousands grouping — with the engine still working from its own raw strings. JVM-tested.
- A traditional-unit converter where any unit in a category can be tapped to become the input unit, backed by a `UnitSpec` table rather than per-pair conversion functions.
- A Japanese-only product interface. Default Android resources, accessibility descriptions, dates, history labels, errors, and Kanji readings are Japanese on every device locale. English resources and Romaji output have been removed.

`firebase-debug.log` is a local runtime artifact and must remain untracked.

## Last verification

- `./gradlew test assembleDebug lint` — **passed** on 2026-09-03 after the 使い方 guide rewrite and its re-teach.
- The guide was verified on the running API 36 Pixel 7 emulator on 2026-09-03, sideways: chapter tabs switch and reset to step 0, 次へ/戻る/step dots step the beads, the focus wash follows the step's rod, note chips and formula captions appear only where the lesson has them, the last step swaps 次へ for もう一度, and 閉じる dismisses. The six-chapter re-teach, the two-digit 読み方 step, and the ochre 「次は …」 chapter hand-off were checked the same way. Two layout faults were found and fixed there: a plain `height`/`width` sheet was clamped by the turned frame and squashed the board, and an active tab's `fillMaxWidth` underline measured against the row and pushed the other four tabs off screen.
- `./gradlew test assembleDebug lint` — passed on 2026-09-02.
- The turned-content design was verified on an API 36 Pixel 7 emulator on 2026-09-02 by driving the virtual accelerometer (`adb emu sensor set acceleration`): both sideways turns render the soroban the right way up, bead taps land correctly through the rotation, accessibility bounds are transformed, the そろばん button works from an upright phone and 電卓 from a sideways one, a button's choice survives until the phone is next turned, an upside-down phone stays on the calculator, a 45-degree hold keeps the current mode, rapid flapping settles correctly, the guide overlay and settings both render turned, back handling is ordered correctly, the bars stay hidden across a background/resume, and light and dark both render.
- The window is now 1080x2400 in every mode, which is the direct evidence that no window rotation happens any more. The old ghost came from the platform's own rotation transition (`snapshot=Surface(name=RotationLayer)`, `finishDrawing of orientation change ... 135ms`); `ROTATION_ANIMATION_JUMPCUT` had not suppressed it. Removing the rotation removed the snapshot.
- Earlier API 35/36 emulator passes covered calculator, tax, settings, safe drawing insets, soroban undo/accessibility, compact tablet layout, and light/dark rendering.
- Source audit found no user-visible English or Romaji literals; the Japanese-only interface also rendered correctly under an `en-US` device locale.
- Physical-device-only behavior (accelerometer shake, audible bead output, haptic feel, and the true feel of the mode transition) still requires a real-device pass.

## Immediate cautions

- Do not restore the deleted Tatami planner, the removed text-to-speech readout, or the removed soroban sharing unless the user explicitly requests them. Sharing took `ShareUtility`, the manifest `FileProvider`, `res/xml/file_paths.xml`, and the four `share*` strings with it.
- Do not reintroduce `requestedOrientation` or an orientation hold. The stretched ghost that motivated them came from the system rotating the window; with the window pinned there is no rotation to race, and writing the mode state directly is now the correct and only path.
- Do not add a `Dialog`, `AlertDialog`, `ModalBottomSheet`, or `Toast` to a screen that can appear inside `TurnedFrame`. Each opens its own window in the app's portrait orientation, which is not the reader's. Use an inline overlay and the snackbar host.
- Keep the sensor's dead band. Committing on every reading would flap the mode for a phone held at an angle, and `physicalAngle` starting null is what lets a saved mode survive a recreation that happens inside that band.
- Do not make the mode transition a symmetric cross-fade, and do not let the root `Surface` keep its default colour. Either one puts a pale flash in the middle of the transition. Verify this with `adb shell screenrecord` and a frame contact sheet, not by eye.
- Keep calculator semantics in `CalculatorEngine`.
- Keep the interface Japanese-only; mathematical and international measurement symbols are the only intended Latin-symbol exceptions.
- Build currently emits the AGP/SDK compatibility warning documented in `techContext.md`.
- A test run may need permission to access the user's Gradle cache outside the repository sandbox.

## Likely next engineering opportunities

These are observations, not an approved roadmap:

- Add ViewModel tests for history, preferences, and feature orchestration.
- Add Compose/instrumentation coverage for orientation, sheets, settings, and Japanese-only rendering.
- Decide whether to update AGP for official SDK 35 support.
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
