# そろばん禅

そろばん禅 is a premium, beautifully crafted, wabi-sabi inspired Android application that serves as a modern minimalist calculator in portrait mode and a fully interactive, realistic Japanese abacus in landscape mode. The product interface is intentionally Japanese-only across every device locale. The application includes seamless orientation transitions, standard calculations, consumption tax rates, traditional Japanese unit conversions, and an educational mental math practice mode.

---

## 🌸 Core Concept & Design System
The design of **Soroban Zen** is inspired by *wabi-sabi* (traditional Japanese minimalist aesthetic focusing on natural asymmetry, simplicity, and warmth):
- **Color Palette**: Warm washi (`#F4F0E7`), sumi ink (`#25231F`), moss (`#586A55`), aizome indigo (`#3E5363`), and a restrained sakura accent (`#A9676D`), with a dedicated charcoal-paper dark theme.
- **Typography**: System serif for expressive headings and system sans-serif for controls, body copy, and precise numeric displays, preserving reliable Japanese glyph coverage without bundled font weight.
- **Crafted Surfaces**: A deterministic Compose-drawn washi texture, vector ensō mark, softly raised cards, and a layered wooden soroban frame provide depth without raster UI assets.
- **Micro-interactions**: Tactile feedback, bead click sounds, spring-based bead motion, deliberate destructive-action confirmation, and short fade transitions reinforce state without visual noise.
- **Responsive Layout**: System safe areas, 48dp-class touch targets, capped tablet keypad width, scrollable tool sheets, and responsive landscape controls keep the interface composed across phones and tablets.

---

## 🧮 Detailed Features

### 1. Interactive Soroban Mode (Landscape)
- **Traditional Layout**: Modern standard 1-5 abacus (1 heaven bead + 4 earth beads per rod).
- **Responsive Rods**: Configurable from 7 to 17 rods with traditional alignment indicators/dots (dots on every 4th rod representing thousands, millions, etc.).
- **Satisfying Interaction**: Custom canvas rendering of bi-conical beads (*soroban-dama*) with drag-and-slide gestures, haptic pops, and spring-snapping physics.
- **Real-Time Reading**: Displays the numerical value alongside its Japanese Kanji reading (e.g., `十二万三千四百五十六`).
- **Text-to-Speech (TTS)**: Built-in voice synthesizer that speaks out numbers in native Japanese pronunciation.
- **Shake to Reset**: Integrates Android's accelerometer; physically shaking the device triggers a satisfying clear-all animation and haptic vibration.
- **Abacus Sharing**: Automatically generates an off-screen bitmap/image card of the abacus's current state (showing beads, numbers, readings, and a signature watermark) and opens the Android sharing sheet.

### 2. Normal Calculator Mode (Portrait)
- Quiet-luxury display hierarchy with a centered ensō wordmark and warm, rounded tactile keys.
- Full support for basic operations (Addition, Subtraction, Multiplication, Division) and precedence math parsing.
- Access to localized, scrollable bottom sheets for Japanese tools.
- Room database calculation history with scrollable records, instant reload, and clear-history confirmation.

### 3. Traditional Japanese Tools
- **Consumption Tax Calculator**: Handles standard (10%) and reduced (8% for food/essentials) tax rates with detailed tax breakdowns.
- **Traditional Unit Converter**: Converts metric units to/from length (shaku, sun, ken), area (tsubo, jo), volume (sho, go), and weight (kan, momme) with high accuracy.
- **Practice / Training Mode**: An educational mental math arithmetic test with a 60-second countdown timer and score tracking.

---

## 📂 Project Structure
```text
calculator/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sorobanzen/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── HistoryDao.kt          (Room Database DAO queries)
│   │   │   │   │   ├── HistoryDatabase.kt     (Room Database singleton builder)
│   │   │   │   │   └── HistoryEntity.kt       (Database Table schema)
│   │   │   │   ├── domain/
│   │   │   │   │   ├── MathEvaluator.kt       (Mathematical expression parser)
│   │   │   │   │   ├── SorobanEngine.kt       (Japanese Kanji number reading)
│   │   │   │   │   ├── TaxCalculator.kt       (Consumption tax standardizer)
│   │   │   │   │   └── UnitConverter.kt       (Traditional Japanese measurements)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── CalculatorGrid.kt  (Keypad UI)
│   │   │   │   │   │   ├── ShakeDetector.kt   (Accelerometer shake listener)
│   │   │   │   │   │   ├── ShareUtility.kt    (Off-screen bitmap sharing engine)
│   │   │   │   │   │   ├── SorobanCanvas.kt   (Custom abacus canvas & gestures)
│   │   │   │   │   │   └── ZenComponents.kt   (Shared visual system components)
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── CalculatorScreen.kt (Portrait layout & sheets controller)
│   │   │   │   │   │   ├── PracticeScreen.kt  (Mental math module)
│   │   │   │   │   │   ├── SettingsScreen.kt  (Configuration panel)
│   │   │   │   │   │   ├── TaxScreen.kt       (Tax breakout card)
│   │   │   │   │   │   └── UnitConverterScreen.kt (Traditional converter)
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt           (Light & Dark Wabi-sabi palette)
│   │   │   │   │       ├── Theme.kt           (Material 3 theme configuration)
│   │   │   │   │       └── Type.kt            (Typography styles)
│   │   │   │   └── MainActivity.kt            (Entry point, Orientation & TTS handler)
│   │   │   ├── res/
│   │   │   │   ├── values/strings.xml         (Japanese-only resources)
│   │   │   │   ├── values/themes.xml          (Window style attributes)
│   │   │   │   └── xml/file_paths.xml         (Sharing directory definitions)
│   │   │   └── AndroidManifest.xml            (App settings declaration)
│   │   └── build.gradle.kts                   (App module build gradle)
│   └── proguard-rules.pro                     (Release optimizer configs)
├── gradle/
│   ├── wrapper/
│   │   └── gradle-wrapper.properties          (Gradle wrapper distribution details)
│   └── libs.versions.toml                     (Dependency versions catalog)
├── build.gradle.kts                           (Project build rules)
├── settings.gradle.kts                        (Module loading rules)
└── gradle.properties                          (Daemon and configuration properties)
```

---

## 🛠️ Build and Run Instructions

### Prerequisites
1. **JDK 21** installed.
2. **Android Studio Koala (2024.1+)** or newer (highly recommended for Jetpack Compose and Kotlin 2.0 compiler previews).
3. Android device or emulator running **API 26 (Android 8.0)** or higher.

### Opening in Android Studio
1. Launch Android Studio.
2. Select **File > Open** and choose the root folder of this project (`calculator/`).
3. Android Studio will automatically recognize the Gradle files, build configuration version catalogs, download the appropriate Gradle Wrapper distribution (`Gradle 8.7`), and synchronize the project dependencies.
4. Click the **Run** button to compile and install on your target device/emulator.

### Building via Terminal
From the root folder `calculator/`:
```bash
# On Linux/macOS
./gradlew assembleDebug

# On Windows (PowerShell)
.\gradlew.bat assembleDebug
```
The compiled debug APK will be generated under `app/build/outputs/apk/debug/app-debug.apk`.
