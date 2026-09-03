# そろばん禅

そろばん禅 (`com.sorobanzen.app`) is a Japanese-only Android app that combines a quiet portrait calculator with an interactive Japanese soroban. The app uses a single portrait window: when the phone is held sideways, the soroban is measured and drawn turned inside that window.

Current build: `versionName 1.0`, `versionCode 1`, minimum API 26, compile/target API 35.

## Product behavior

### Mode switching

- Upright shows the calculator; sideways shows the soroban.
- The activity window stays pinned to portrait. `OrientationEventListener` changes the content mode, and `MainActivity.TurnedFrame` rotates and lays out sideways content with swapped width/height constraints.
- The calculator's `そろばん` button and soroban's `電卓` button change the same mode state directly. A button selection remains until the phone is physically turned again.
- Mode changes cross-fade inside the app. Soroban mode hides the system bars; swipe from an edge to reveal them temporarily.

### Calculator and history

- Supports decimal `+`, `−`, `×`, and `÷` calculations with normal multiplication/division precedence.
- The parser also understands parentheses and unary signs, although the keypad does not expose parentheses.
- `C`, `AC`, sign toggle, one decimal point per operand, repeat-equals behavior, and readable input limits are supported.
- The display formats `×`/`÷`, spaces binary operators, groups integer digits, and shows Japanese Kanji readings for whole-number results.
- Successful calculations and tax actions are stored newest-first in Room history. Tapping a row loads its result; clearing history requires confirmation. Soroban changes are not saved to history.

### Soroban

- Fixed seven-rod 1:5 layout: one heaven bead worth five and four earth beads worth one each.
- Each rod is one decimal digit (`0..9`), ordered from most-significant on the left to least-significant on the right.
- Tap or drag beads on the procedural Compose Canvas. Alignment dots, ebony grain, brass rods/inlay, bone reckoning field, and black-lacquer bi-conical beads are drawn at runtime.
- The rail provides `電卓`, `そろばんを払う`, `使い方`, and `設定`. Clear and shake-to-reset expose a short `元に戻す` action.
- The value is shown with comma grouping and a Japanese Kanji reading. Romaji, text-to-speech, sharing, and adjustable rod counts are not part of the current app.

### 使い方 guide

The sideways rail opens an inline guide overlay, so it remains readable inside the turned frame. It is one stepped lesson with a read-only five-rod preview, optional notes and formulas, step dots, and `戻る`/`次へ`/`閉じる` controls.

The six chapters are:

1. `読み方`
2. `数を置く`
3. `足す引く`
4. `5をつかう`
5. `10をつかう`
6. `かけ算・わり算`

`次へ` and `戻る` continue across chapter boundaries. The final step changes `次へ` to `もう一度`; selecting a chapter starts it at its first step.

### Japanese tools

- **Consumption tax:** add or remove Japan's 10% standard rate or 8% reduced rate. Tax arithmetic uses whole-yen round-down rules and rejects negative or non-finite input.
- **Traditional units:** length (`尺`, `寸`, `間`), area (`坪`, approximate `畳`), volume (`升`, `合`), and weight (`貫`, `匁`). Any listed unit can be selected as the input unit.

### Settings and visual system

- Settings persist only bead sound effects and haptic feedback.
- Light/dark appearance follows the system setting; there is no in-app dark-mode switch.
- The interface uses Japanese strings from the default `values/strings.xml` for every device locale. Mathematical and international unit symbols are the only intentional Latin-symbol exceptions.
- The UI uses procedural washi texture, an ensō mark, serif/sans typography, restrained wabi-sabi colors, safe-area handling, and responsive tablet/compact-height layouts.

## Project structure

```text
.
├── app/src/main/java/com/sorobanzen/app/
│   ├── MainActivity.kt
│   ├── data/                 # Room history and SharedPreferences
│   ├── domain/               # Calculator, soroban, tax, and units
│   ├── ui/
│   │   ├── components/       # Canvas, calculator grid, sensors, shared UI
│   │   ├── screens/          # Calculator, soroban, tools, settings, guide
│   │   └── theme/            # Light/dark Material 3 theme
│   └── viewmodel/            # Activity-scoped state and orchestration
├── app/src/main/res/         # Japanese strings, themes, icons, backup rules
├── app/src/test/             # Local JVM tests for domain and hit-testing logic
├── gradle/                   # Version catalog and Gradle wrapper
└── memory-bank/              # Durable project context
```

There is no backend, account system, cloud sync, analytics, or navigation graph. The former Tatami planner, soroban sharing, and text-to-speech readout have been removed; the unit converter's approximate `畳` result remains.

## Build and run

Prerequisites: JDK 21 and Android SDK tooling for API 35. Open the repository root in Android Studio or run:

```bash
./gradlew test
./gradlew assembleDebug
./gradlew lint
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. To install and launch it on a connected emulator/device:

```bash
./gradlew installDebug
adb shell am start -n com.sorobanzen.app/.MainActivity
```

Use the virtual accelerometer for emulator mode checks; the rotate control changes the window, while this app keeps the window portrait:

```bash
adb emu sensor set acceleration 0:9.8:0    # upright → calculator
adb emu sensor set acceleration -9.8:0:0   # sideways → soroban
adb emu sensor set acceleration 9.8:0:0    # other sideways direction
```

Screenshots are captured in the portrait window frame and need turning to read sideways soroban mode. Physical-device checks are still useful for accelerometer shake, sound, haptic feel, and the hand-held mode transition.
