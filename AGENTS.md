# AGENTS.md

This file is the operating guide for AI agents working in the Soroban Zen repository. It applies to the entire repository.

## Start here

1. Read `memory-bank/README.md`, then the memory-bank files it links.
2. Run `git status --short` before editing. This repository may contain intentional, uncommitted user work; preserve it and never revert or overwrite unrelated changes.
3. Treat the current working tree as the source of truth. `README.md` is useful product documentation, but code and tests win if they disagree.
4. Keep `memory-bank/activeContext.md` and `memory-bank/progress.md` current when a change affects architecture, scope, workflows, risks, or feature status.

## Project at a glance

Soroban Zen is a single-module Android application written in Kotlin and Jetpack Compose. Portrait orientation presents a calculator and tool sheets; landscape orientation presents an interactive Japanese soroban. The package and application ID are `com.sorobanzen.app`.

The main source layers are:

- `app/src/main/java/com/sorobanzen/app/domain/`: pure calculation and session logic. Keep this Android-free where practical.
- `app/src/main/java/com/sorobanzen/app/data/`: Room history storage and SharedPreferences-backed settings.
- `app/src/main/java/com/sorobanzen/app/viewmodel/`: state, orchestration, coroutines, and domain/data coordination.
- `app/src/main/java/com/sorobanzen/app/ui/`: Compose screens, canvas components, sharing, sensors, and theme.
- `app/src/main/res/`: English and Japanese strings, themes, icons, backup rules, and FileProvider configuration.
- `app/src/test/`: local JVM tests for domain behavior.

See `memory-bank/systemPatterns.md` for the runtime and data-flow map.

## Architecture rules

- Put deterministic business rules in `domain`, not inside composables.
- Screens render collected `StateFlow` values and send user actions to `ZenViewModel`; avoid duplicating mutable feature state in UI code.
- Expose ViewModel state as read-only `StateFlow`/`SharedFlow`. Launch database work and timers in `viewModelScope`.
- Preserve the orientation contract: portrait is calculator mode, landscape is soroban mode, and settings temporarily takes over either orientation.
- Treat each soroban rod as a decimal digit in `0..9`. Rod arrays are most-significant digit first. The supported rod count is `7..17`, default `13`.
- Keep calculator display symbols (`×`, `÷`) separate from parser symbols (`*`, `/`). `CalculatorEngine` owns keypad semantics; `MathEvaluator` owns expression parsing and precedence.
- Use `BigDecimal` for tax arithmetic and retain the current yen rounding rule (`RoundingMode.DOWN`). Validate input as finite and non-negative before calling `TaxCalculator`.
- Keep persistence access behind `HistoryDao` and `AppPreferences`. A Room version change requires an explicit migration decision; never silently add destructive fallback behavior.
- Sharing must remain compatible with the manifest `FileProvider` and `res/xml/file_paths.xml`. Perform bitmap/file work off the main thread and grant URI read permission.
- Sensor and TTS lifecycles must remain paired: register/unregister listeners and initialize/shutdown `TextToSpeech` with their Android owners.

## UI and product conventions

- Preserve the wabi-sabi visual language defined in `ui/theme`: warm paper/charcoal surfaces with moss, indigo, and sakura accents.
- Support both system light and dark themes.
- Put user-facing text in both `res/values/strings.xml` and `res/values-ja/strings.xml`. Existing hard-coded bilingual strings are technical debt, not a pattern to copy.
- Respect the sound, haptic, and TTS preference toggles when adding interactions.
- Canvas behavior, orientation changes, accelerometer reset, Android sharing, and TTS require device/emulator verification; JVM tests cannot validate them.

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
- Interaction or platform changes: also manually verify on a device/emulator in portrait and landscape.

`./gradlew test` is the current passing baseline. Known non-fatal build warnings are recorded in `memory-bank/techContext.md`.

## Change discipline

- Prefer small, coherent edits that follow existing Kotlin and Compose style.
- Do not edit generated outputs under `.gradle/`, `build/`, or `app/build/`.
- Do not commit `local.properties`, APKs, IDE state, or runtime logs such as `firebase-debug.log`.
- Do not introduce a new framework, navigation model, persistence layer, or dependency-injection system without documenting the reason in `memory-bank/systemPatterns.md` and `memory-bank/activeContext.md`.
- When changing a feature contract, update its tests, localized resources, `README.md` if user-facing, and the memory bank if future agents need the new fact.

## Commit and push policy

- After every completed logical change, create a Git commit and push it to the configured remote before ending the task.
- The repository owner grants standing authorization to push completed, in-scope commits to the configured remote without requesting approval after each edit.
- Stage and commit only files that belong to the change. Never include unrelated or pre-existing user changes merely to obtain a clean working tree.
- When one session contains distinct features or independently meaningful changes, split them into separate focused commits and push each commit after its relevant checks pass.
- Use concise commit messages that describe the outcome. Inspect the staged diff before committing.
- If a commit or push cannot be completed, do not claim the task is fully delivered: preserve the local work and report the exact blocker and unpushed commit state.

## Definition of done

A task is complete when the requested behavior is implemented, relevant tests/checks pass, platform-only behavior has an explicit manual verification note, existing user changes remain intact, durable documentation is updated where the project view changed, and all task commits have been pushed successfully.
