# MAD-RELEASE — Basic Calculator (Base Project)

Base Android Native application built for the university release-engineering assignment **MAD-RELEASE** (Course: *INT1449 — Mobile Application Development*, Group 14).

This repository provides a clean, stable, and test-covered foundation containing the core calculator logic and Jetpack Compose UI. It serves as the common starting point for three independent release-engineering feature branches.

---

## Tech Stack

- **Language:** Kotlin 2.2.10
- **UI Toolkit:** Jetpack Compose (Compose BOM 2025.08.01)
- **Design System:** Material 3 (Dynamic Color, Light & Dark themes)
- **Architecture:** MVI/StateFlow pattern (`Screen` → `ViewModel` → `Engine` → `State`)
- **Precision Arithmetic:** Java `BigDecimal`
- **Build System:** Gradle 9.5.0 (Kotlin DSL) + Android Gradle Plugin 9.3.2
- **Target SDK:** 37 | **Min SDK:** 23
- **Test Framework:** JUnit 4
- **Package / Application ID:** `com.madrelease.calculator`

---

## Project Structure

```text
Caculatorapp/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/madrelease/calculator/
│       │   │   ├── MainActivity.kt
│       │   │   ├── app/
│       │   │   │   └── CalculatorApp.kt
│       │   │   └── calculator/
│       │   │       ├── CalculatorAction.kt
│       │   │       ├── CalculatorButton.kt
│       │   │       ├── CalculatorEngine.kt
│       │   │       ├── CalculatorScreen.kt
│       │   │       ├── CalculatorState.kt
│       │   │       └── CalculatorViewModel.kt
│       │   └── res/
│       │       ├── drawable/
│       │       └── values/
│       └── test/java/com/madrelease/calculator/calculator/
│           └── CalculatorEngineTest.kt
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── README.md
```

---

## Calculator Behavior & Specification

- **Immediate-Execution Semantics:** Calculations evaluate sequentially from left to right without standard operator precedence (e.g., `2 + 3 × 4 = 20`).
- **Precision:** Uses `BigDecimal` for all calculations, avoiding IEEE 754 binary floating-point rounding errors (e.g., `0.1 + 0.2 = 0.3`).
- **Rounding:** Non-terminating division (such as `1 ÷ 3`) is safely rounded to 16 decimal places (`RoundingMode.HALF_UP`).
- **Keypad & Actions:**
  - Numbers `0`–`9` (limited to 16 input digits, leading zeroes normalized).
  - Operations `+`, `−`, `×`, `÷` (consecutive operator entries replace pending operator).
  - Decimal point `.` (automatically prefixes `0.` when starting a new operand).
  - Percent `%` (divides the active operand by 100).
  - Sign toggle `+/−` (inverts operand sign; handles pending `-0`).
  - Backspace `⌫` (erases last digit; preserves pending operator; resets negative single digit to 0).
  - Clear `AC` (resets operand, pending operator, and expression; clears error).
  - Equals `=` (evaluates expression; safe when repeated or called without second operand).
- **Error Handling:**
  - Division by zero (`x ÷ 0` or `x ÷ -0`) displays `Error` without crashing.
  - Numbers exceeding magnitude $10^{100}$ trigger `Error`.
  - The calculator locks inputs during error state until `AC` is pressed to recover.

---

## Setup & Run Instructions

### Prerequisites
- JDK 17 (recommended: Eclipse Adoptium / Temurin 17)
- Android SDK 37 installed with platform tools and build-tools
- Ensure `local.properties` specifies your Android SDK directory:
  ```properties
  sdk.dir=/path/to/your/Android/Sdk
  ```

### Build & Test Commands

Run the following commands using the committed Gradle wrapper:

```bash
# Run unit tests
./gradlew test

# Run Android Lint static analysis
./gradlew lint

# Assemble Debug APK (outputs to app/build/outputs/apk/debug/app-debug.apk)
./gradlew assembleDebug

# Assemble Unsigned Release APK (outputs to app/build/outputs/apk/release/)
./gradlew assembleRelease

# Generate Unsigned Release Android App Bundle (outputs to app/build/outputs/bundle/release/)
./gradlew bundleRelease
```

---

## Branching Purpose & Roadmap

This base project stays intentionally neutral with no release-engineering scaffolding (no Firebase, Crashlytics, signing keystores, key.properties, or CI/CD pipelines).

Each team member branches independently from `develop` to implement their topic:

| Branch | Owner | Focus Area | Key Deliverables |
| :--- | :--- | :--- | :--- |
| `feature/debug-release` | Đặng Văn Chiến | Packaging & Build Types | `Debug` vs `Release` builds, R8 code shrinking & obfuscation, `mapping.txt`, APK vs AAB |
| `feature/versioning-signing` | Chu Tuyết Nhi | Versioning & Security | `versionCode`, `versionName`, Keystore creation, Release signing configs via Gradle |
| `feature/crashlytics-monitoring` | Vũ Thị Thu Duyên | Monitoring & Hotfix | Firebase Crashlytics SDK, crash breadcrumbs, crash-free metrics, hotfix strategy |

---

## Base Release Metadata

- **versionCode:** `1`
- **versionName:** `"1.0.0"`
- **Build Types:** `debug`, `release` (standard, unsigned, `isMinifyEnabled = false` for base)
