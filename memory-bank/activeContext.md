# Active Context

Last updated: 2026-07-16

## Current product state

The branch is `main`. The current implementation includes:

- `AppPreferences`, `CalculatorEngine`, and `PracticeSession` as focused persistence/domain components.
- JVM coverage for calculator, parser, practice, soroban Kanji readings, tax, and unit conversion.
- Persistent rod-count, sound, haptic, and Japanese TTS preferences.
- Safer calculator operator/history behavior, practice submission locking, tax validation, share failure handling, and sensor/share lifecycle fixes.
- Tatami planner domain/UI removed; the approximate `畳` area conversion remains.
- A cohesive premium UI across calculator, tool sheets, practice, settings, history, and landscape soroban: refined light/dark palettes, serif/sans type hierarchy, procedural washi texture and ensō mark, shared card/pill/metric components, responsive safe-area handling, and a layered wooden Canvas soroban.
- A Japanese-only product interface. Default Android resources, accessibility descriptions, dates, history labels, errors, Kanji readings, and generated share cards are Japanese on every device locale. English resources and Romaji output have been removed.

`firebase-debug.log` is a local runtime artifact and must remain untracked.

## Last verification

- `./gradlew test assembleDebug lint` — **passed** on 2026-07-16.
- Source audit found no user-visible English or Romaji literals.
- Emulator visual verification completed on 2026-07-16 using an API 36 Pixel 7 AVD with the device locale set to `en-US`. The calculator still rendered Japanese-only labels, including the new `全消` and `一字` keys, without clipping.
- Earlier emulator checks on API 35/36 covered portrait and landscape, light and dark modes, calculator, tax sheet, settings, responsive tablet keypad, safe drawing insets, and soroban rendering.
- Physical-device-only behavior (accelerometer shake, audible bead/TTS output, haptic feel, and Android share chooser/file delivery) still requires a real-device pass.

## Immediate cautions

- Do not restore the deleted Tatami planner unless the user explicitly requests it.
- Keep calculator semantics in `CalculatorEngine` and practice submission rules in `PracticeSession`.
- Keep the interface Japanese-only; mathematical and international measurement symbols are the only intended Latin-symbol exceptions.
- Build currently emits the AGP/SDK compatibility warning and deprecated system-bar API warnings documented in `techContext.md`.
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
