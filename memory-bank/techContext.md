# Technical Context

## Platform and toolchain

| Item | Current value |
| --- | --- |
| Project | Single Gradle module, `:app` |
| Language | Kotlin 2.0.0 |
| UI | Jetpack Compose + Material 3 |
| Android Gradle Plugin | 8.5.0 |
| Gradle wrapper | 8.7 |
| Java/Kotlin target | JDK/JVM 21 |
| Namespace/application ID | `com.sorobanzen.app` |
| Minimum SDK | 26 |
| Target/compile SDK | 35 / 35 |
| App version | `1.0` (`versionCode` 1) |

Dependency versions are centralized in `gradle/libs.versions.toml`. Key libraries are Compose BOM `2024.06.00`, Lifecycle `2.8.2`, Activity Compose `1.9.0`, Navigation Compose `2.7.7`, Room `2.6.1`, and JUnit `4.13.2`. Room code generation uses KSP.

Navigation Compose is currently declared but the app uses local Compose state and orientation rather than a navigation graph.

## Important paths

- Entry point: `app/src/main/java/com/sorobanzen/app/MainActivity.kt`
- Orchestrator: `app/src/main/java/com/sorobanzen/app/viewmodel/ZenViewModel.kt`
- Domain: `app/src/main/java/com/sorobanzen/app/domain/`
- Data: `app/src/main/java/com/sorobanzen/app/data/`
- UI: `app/src/main/java/com/sorobanzen/app/ui/`
- Local tests: `app/src/test/java/com/sorobanzen/app/domain/`
- Manifest: `app/src/main/AndroidManifest.xml`
- Dependency catalog: `gradle/libs.versions.toml`

## Commands

Run from the repository root:

```bash
./gradlew test
./gradlew assembleDebug
./gradlew lint
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Android Studio can run the `app` configuration on an API 26+ emulator/device.

## Platform integrations

- `TextToSpeech` with `Locale.JAPANESE`, owned by `MainActivity`.
- Accelerometer access through `SensorManager`; no dangerous runtime permission is required.
- Haptics through Compose `LocalHapticFeedback`.
- Bead clicks through Android view sound effects.
- Image sharing through `FileProvider` authority `${applicationId}.fileprovider` and cached PNG files.
- Room SQLite persistence and Android SharedPreferences.
- Android backup is enabled, but the included backup/data-extraction XML files still contain default placeholder rules.

## Verification baseline (2026-07-16)

`./gradlew test` succeeds for debug and release unit-test variants: 51 tasks, with all current JVM tests passing.

Non-fatal warnings observed:

- AGP 8.5.0 reports that it was tested through compile SDK 34 while this project compiles against SDK 35.
- `Theme.kt` writes deprecated `statusBarColor` and `navigationBarColor` properties.

No CI configuration, static-analysis customization, UI tests, or instrumentation tests are currently present.

## Local-only files

`local.properties`, `.gradle/`, IDE metadata, build outputs, and APKs are ignored. `firebase-debug.log` is currently present as an untracked runtime log and should not be treated as project source or committed.
