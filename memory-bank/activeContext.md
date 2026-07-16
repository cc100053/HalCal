# Active Context

Last updated: 2026-07-16

## Current repository state

The branch is `main`. The working tree contains broad, intentional-looking uncommitted changes that predate the memory bank. Future agents must inspect `git status --short` and preserve them.

The current working tree, not commit `de5da25`, includes these important product changes:

- New `AppPreferences`, `CalculatorEngine`, and `PracticeSession` classes.
- New JVM coverage for calculator, parser, practice, soroban readings, tax, and unit conversion.
- Persistent settings wired through the ViewModel/activity factory.
- Safer calculator operator/history behavior, practice submission locking, tax validation, share failure handling, and sensor/share lifecycle fixes.
- Tatami planner domain/UI files removed; remaining tatami output is only the approximate area conversion.
- Manifest/FileProvider, resource, screen, and README edits in progress.

This summary records observed state only; it does not assume that all uncommitted work should be committed together.

## Last verification

- `./gradlew test` — **passed** on 2026-07-16.
- Debug and release Kotlin compilation succeeded as part of the test task.
- Device-only interactions have not been verified in this documentation task.

## Immediate cautions

- Do not restore the deleted Tatami planner unless the user explicitly requests it.
- Do not discard or rewrite the existing uncommitted changes.
- Keep calculator semantics in `CalculatorEngine` and practice submission rules in `PracticeSession`; these classes were introduced to make behavior testable.
- Build currently emits the AGP/SDK compatibility warning and deprecated system-bar API warnings documented in `techContext.md`.
- A test run may need permission to access the user's Gradle cache outside the repository sandbox.

## Likely next engineering opportunities

These are observations, not an approved roadmap:

- Move remaining hard-coded UI strings/content descriptions into English and Japanese resources.
- Add ViewModel tests for history, preferences, timers, and feature orchestration.
- Add Compose/instrumentation coverage for orientation, sheets, and settings.
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
