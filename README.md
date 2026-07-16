# Soroban Zen (そろばん禅)

Soroban Zen is a premium, beautifully crafted, wabi-sabi inspired Android application that serves as a modern minimalist calculator in portrait mode and a fully interactive, realistic Japanese abacus (Soroban) in landscape mode. The application includes seamless orientation transitions, standard calculations, consumption tax rates, traditional Japanese unit conversions, an interactive tatami mat planner, and an educational mental math practice mode.

---

## 🌸 Core Concept & Design System
The design of **Soroban Zen** is inspired by *wabi-sabi* (traditional Japanese minimalist aesthetic focusing on natural asymmetry, simplicity, and warmth):
- **Color Palette**: Off-white paper background (`#FAF9F6`), matte charcoal ink text (`#1E1E1E`), traditional moss green (`#5E6F54`), pale sakura pink (`#DCAEAF`), and slate indigo blue (`#324A5E`).
- **Typography**: Clean, readable sans-serif text aligned with Japanese design principles.
- **Micro-interactions**: Satisfying haptic feedback (bead snap clicks) and spring-based physics for beads sliding and orientation transitions.
- **Orientation Transitions**: When rotating the screen, a custom `AnimatedContent` combines scaling, sliding, and fading to simulate an organic unfolding transition instead of a jarring layout swap.

---

## 🧮 Detailed Features

### 1. Interactive Soroban Mode (Landscape)
- **Traditional Layout**: Modern standard 1-5 abacus (1 heaven bead + 4 earth beads per rod).
- **Responsive Rods**: Configurable from 7 to 17 rods with traditional alignment indicators/dots (dots on every 4th rod representing thousands, millions, etc.).
- **Satisfying Interaction**: Custom canvas rendering of bi-conical beads (*soroban-dama*) with drag-and-slide gestures, haptic pops, and spring-snapping physics.
- **Real-Time Translation**: Displays the numerical value alongside its Japanese Kanji reading (e.g., `十二万三千四百五十六`) and phonetic Romaji reading (e.g., `jū ni man san sen...`).
- **Text-to-Speech (TTS)**: Built-in voice synthesizer that speaks out numbers in native Japanese pronunciation.
- **Shake to Reset**: Integrates Android's accelerometer; physically shaking the device triggers a satisfying clear-all animation and haptic vibration.
- **Abacus Sharing**: Automatically generates an off-screen bitmap/image card of the abacus's current state (showing beads, numbers, readings, and a signature watermark) and opens the Android sharing sheet.

### 2. Normal Calculator Mode (Portrait)
- Clean, non-crowded button grid with warm, rounded buttons.
- Full support for basic operations (Addition, Subtraction, Multiplication, Division) and precedence math parsing.
- Access to specialized bottom sheets for Japanese tools.
- Room database calculation history with scrollable records and instant reload.

### 3. Traditional Japanese Tools
- **Consumption Tax Calculator**: Handles standard (10%) and reduced (8% for food/essentials) tax rates with detailed tax breakdowns.
- **Traditional Unit Converter**: Converts metric units to/from length (shaku, sun, ken), area (tsubo, jo), volume (sho, go), and weight (kan, momme) with high accuracy.
- **Tatami Room Planner**: Calculates required Tatami mats based on customizable room width/length (in meters) and regional mat sizing (Kyoto/Kyouma, Nagoya/Ainoma, Tokyo/Edoma). Draws an interactive auspicious arrangement of mats (Syugi-biki pattern) with custom Canvas rendering.
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
│   │   │   │   │   ├── SorobanEngine.kt       (Kanji & Romaji number translation)
│   │   │   │   │   ├── TaxCalculator.kt       (Consumption tax standardizer)
│   │   │   │   │   ├── TatamiPlanner.kt       (Tatami sizing & layout math)
│   │   │   │   │   └── UnitConverter.kt       (Traditional Japanese measurements)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── CalculatorGrid.kt  (Keypad UI)
│   │   │   │   │   │   ├── ShakeDetector.kt   (Accelerometer shake listener)
│   │   │   │   │   │   ├── ShareUtility.kt    (Off-screen bitmap sharing engine)
│   │   │   │   │   │   └── SorobanCanvas.kt   (Custom abacus canvas & gestures)
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── CalculatorScreen.kt (Portrait layout & sheets controller)
│   │   │   │   │   │   ├── PracticeScreen.kt  (Mental math module)
│   │   │   │   │   │   ├── SettingsScreen.kt  (Configuration panel)
│   │   │   │   │   │   ├── TatamiPlannerScreen.kt (Tatami blueprint grid canvas)
│   │   │   │   │   │   ├── TaxScreen.kt       (Tax breakout card)
│   │   │   │   │   │   └── UnitConverterScreen.kt (Traditional converter)
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt           (Light & Dark Wabi-sabi palette)
│   │   │   │   │       ├── Theme.kt           (Material 3 theme configuration)
│   │   │   │   │       └── Type.kt            (Typography styles)
│   │   │   │   └── MainActivity.kt            (Entry point, Orientation & TTS handler)
│   │   │   ├── res/
│   │   │   │   ├── values/strings.xml         (English resources)
│   │   │   │   ├── values-ja/strings.xml      (Default Japanese resources)
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
