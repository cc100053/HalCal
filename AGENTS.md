# AGENTS.md

This file is the operating guide for AI agents working in the Soroban Zen repository. It applies to the entire repository.

## Start here

1. Read `memory-bank/README.md`, then the memory-bank files it links.
2. Run `git status --short` before editing. This repository may contain intentional, uncommitted user work; preserve it and never revert or overwrite unrelated changes.
3. Treat the current working tree as the source of truth. `README.md` is useful product documentation, but code and tests win if they disagree.
4. Keep `memory-bank/activeContext.md` and `memory-bank/progress.md` current when a change affects architecture, scope, workflows, risks, or feature status.

## Project at a glance

Soroban Zen is a single-module Android application written in Kotlin and Jetpack Compose. An upright phone presents a calculator and tool sheets; a sideways phone presents an interactive Japanese soroban, drawn turned inside a window that stays in portrait. The package and application ID are `com.sorobanzen.app`.

The main source layers are:

- `app/src/main/java/com/sorobanzen/app/domain/`: pure calculation and session logic. Keep this Android-free where practical.
- `app/src/main/java/com/sorobanzen/app/data/`: Room history storage and SharedPreferences-backed settings.
- `app/src/main/java/com/sorobanzen/app/viewmodel/`: state, orchestration, coroutines, and domain/data coordination.
- `app/src/main/java/com/sorobanzen/app/ui/`: Compose screens, canvas components, sensors, and theme.
- `app/src/main/res/`: Japanese strings, themes, icons, and backup rules.
- `app/src/test/`: local JVM tests for domain behavior.

See `memory-bank/systemPatterns.md` for the runtime and data-flow map.

## Architecture rules

- Put deterministic business rules in `domain`, not inside composables.
- Screens render collected `StateFlow` values and send user actions to `ZenViewModel`; avoid duplicating mutable feature state in UI code.
- Expose ViewModel state as read-only `StateFlow`/`SharedFlow`. Launch database work and timers in `viewModelScope`.
- Preserve the mode contract: an upright phone is calculator mode, a sideways phone is soroban mode, and settings temporarily takes over either. The activity window is pinned to portrait for the life of the app (`android:screenOrientation="portrait"`) and the *content* turns instead — `MainActivity.TurnedFrame` lays the child out with the window's width and height swapped and draws it rotated. Nothing requests a system rotation, so there is no `RotationLayer` snapshot and no ghost of the outgoing screen.
- Mode is one piece of state. `OrientationEventListener` writes the turn the content needs, and the そろばん/電卓 buttons write the same state directly; a button's choice stands until the phone is next turned. Do not reintroduce `requestedOrientation`, a hold, or a release — there is no window rotation left to wait for.
- Anything that opens its own window (`Dialog`, `AlertDialog`, `ModalBottomSheet`, `Toast`) comes up in the window's portrait orientation, not the reader's, so it is unusable while the phone is sideways. Inside the turned frame use the inline `UsageGuideSheet` overlay instead, and use the existing snackbar host in place of a `Toast`.
- `TurnedFrame` pads for the display cutout and consumes the rest of the window insets, because a turned screen would otherwise pad the wrong physical edge. Soroban mode also hides the system bars, which would otherwise run down a vertical edge with their text on its side.
- The mode transition fades the incoming screen in over an outgoing one held at full opacity (`ContentTransform` with `fadeOut(snap(delayMillis = FADE_MILLIS))` and `targetContentZIndex = 1f`). Do not fade both at once: that leaves frames where neither screen is opaque and whatever sits behind them flashes through. For the same reason the root `Surface` is painted `colorScheme.background`, the tone the screens themselves paint, not the lighter `colorScheme.surface`.
- Treat each soroban rod as a decimal digit in `0..9`. Rod arrays are most-significant digit first. The rod count is fixed at `SorobanEngine.ROD_COUNT` (7) and is no longer a user preference.
- Keep calculator display symbols (`×`, `÷`) separate from parser symbols (`*`, `/`). `CalculatorEngine` owns keypad semantics; `MathEvaluator` owns expression parsing and precedence.
- Use `BigDecimal` for tax arithmetic and retain the current yen rounding rule (`RoundingMode.DOWN`). Validate input as finite and non-negative before calling `TaxCalculator`.
- Keep persistence access behind `HistoryDao` and `AppPreferences`. A Room version change requires an explicit migration decision; never silently add destructive fallback behavior.
- Sensor listener lifecycles must remain paired: register and unregister with their Android owners. `OrientationEventListener` in `MainActivity` is enabled in `onStart` and disabled in `onStop`. Text-to-speech has been removed from the product; do not reintroduce it without an explicit request.

## UI and product conventions

- Preserve the wabi-sabi visual language defined in `ui/theme`: warm paper/charcoal surfaces with moss, indigo, and sakura accents.
- The soroban instrument itself follows the 黒檀と骨 (Ebony & Bone) handoff and is deliberately outside that theme: an ebony-lacquer frame, brass inlay and rods, a bone reckoning field, and black-lacquer bi-conical beads, identical in light and dark. The frame and the beam carry a fine lengthwise ebony grain drawn from a fixed seed. A lacquered instrument does not change colour with the room, and its palette is fixed in `SorobanCanvas.kt` rather than branched on `isSystemInDarkTheme`.
- Geometry in `SorobanCanvas.kt` is written in the handoff's own 768 x 352 coordinate space and scaled by `unit = boardHeight / BOARD_HEIGHT`, so the constants can be read straight against the design. Rod pitch is the only thing that depends on the rod count.
- Support both system light and dark themes.
- Keep user-facing text in the default `res/values/strings.xml`; the current product is Japanese-only across device locales. Do not add an English locale overlay unless localization is explicitly reintroduced.
- Respect the sound and haptic preference toggles when adding interactions. They are the only two settings left.
- Canvas behavior, mode changes, and accelerometer reset require device/emulator verification; JVM tests cannot validate them. On an emulator, drive mode changes with `adb emu sensor set acceleration` (`0:9.8:0` upright, `-9.8:0:0` and `9.8:0:0` for the two sideways turns) rather than the rotate control, since the window no longer rotates. Screenshots come out in the window's portrait frame and need turning to be read.

## Build and verification

Prerequisites: JDK 21 and Android SDK tooling for API 35. Use the checked-in Gradle wrapper from the repository root.

```bash
./gradlew test
./gradlew assembleDebug
./gradlew lint
```

Choose checks proportional to the change:

- Domain logic: add focused tests under `app/src/test/java/com/sorobanzen/app/domain/`, then run `./gradlew test`.
- ViewModel/data changes: run unit tests and add fakes or test dependencies when behavior warrants it.
- Resources, manifest, or Compose UI: run `./gradlew assembleDebug`; run `./gradlew lint` when feasible.
- Interaction or platform changes: also manually verify on a device/emulator with the phone held upright and both ways sideways.

`./gradlew test` is the current passing baseline. Known non-fatal build warnings are recorded in `memory-bank/techContext.md`.

## Change discipline

- Prefer small, coherent edits that follow existing Kotlin and Compose style.
- Do not edit generated outputs under `.gradle/`, `build/`, or `app/build/`.
- Do not commit `local.properties`, APKs, IDE state, or runtime logs such as `firebase-debug.log`.
- Do not introduce a new framework, navigation model, persistence layer, or dependency-injection system without documenting the reason in `memory-bank/systemPatterns.md` and `memory-bank/activeContext.md`.
- When changing a feature contract, update its tests, the default Japanese resources, `README.md` if user-facing, and the memory bank if future agents need the new fact.

## Commit and push policy

- After every completed logical change, create a Git commit and push it to the configured remote before ending the task.
- The repository owner grants standing authorization to push completed, in-scope commits to the configured remote without requesting approval after each edit.
- Stage and commit only files that belong to the change. Never include unrelated or pre-existing user changes merely to obtain a clean working tree.
- When one session contains distinct features or independently meaningful changes, split them into separate focused commits and push each commit after its relevant checks pass.
- Use concise commit messages that describe the outcome. Inspect the staged diff before committing.
- If a commit or push cannot be completed, do not claim the task is fully delivered: preserve the local work and report the exact blocker and unpushed commit state.

## Definition of done

A task is complete when the requested behavior is implemented, relevant tests/checks pass, platform-only behavior has an explicit manual verification note, existing user changes remain intact, durable documentation is updated where the project view changed, and all task commits have been pushed successfully.
