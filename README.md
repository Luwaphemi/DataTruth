DataTruth

A Kotlin Multiplatform (KMP) application that monitors mobile data usage and compares it with network provider reports to detect discrepancies.

[Android Build](https://github.com/Luwaphemi/DataTruth/actions/workflows/build.yml/badge.svg)](https://github.com/Luwaphemi/DataTruth/actions)
[Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.9.3-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)

Short Demo Video

Watch the app in action: [DataTruth Demo](https://youtube.com/shorts/SbWJLAIsTAA)


Overview

DataTruth empowers mobile users to take control of their data consumption by providing transparent, real-time monitoring and comparison tools. The app measures actual device data usage and compares it against provider-reported usage, alerting users to any discrepancies that may indicate billing errors or unauthorized data charges.

Key Features

- Real-time Data Monitoring - Track your mobile and WiFi data usage directly from your device
- Provider Report Comparison - Compare device measurements with network provider reports
- Discrepancy Detection - Automatic alerts when device usage doesn't match provider reports
- Usage Analytics - Visual charts and statistics showing data consumption patterns
- Data Plan Configuration - Set up your data plan limits and billing cycle
- Cross-Platform - Works on both Android and iOS

Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin Multiplatform | Shared business logic across platforms |
| Compose Multiplatform | Shared UI components |
| SQLDelight | Cross-platform local database |
| Kotlin Coroutines | Asynchronous programming |
| Kotlin Serialization | Data serialization |
| GitHub Actions | CI/CD for automated builds |


Project Structure

DataTruth/
├── composeApp/
│   └── src/
│       ├── commonMain/          # Shared code for all platforms
│       │   └── kotlin/
│       │       └── com/example/datatruth/
│       │           ├── data/           # Repository & database
│       │           ├── models/         # Data models
│       │           ├── platform/       # Platform interfaces
│       │           └── ui/             # Shared UI components
│       ├── androidMain/         # Android-specific code
│       │   └── kotlin/
│       │       └── com/example/datatruth/
│       │           ├── platform/       # Android data monitor
│       │           └── data/           # Android DB driver
│       └── iosMain/             # iOS-specific code
│           └── kotlin/
│               └── com/example/datatruth/
│                   ├── platform/       # iOS data monitor
│                   └── data/           # iOS DB driver
├── iosApp/                      # iOS app entry point
├── .github/workflows/           # CI/CD configuration
├── ESSAY.md                     # Project essay
├── LICENSE                      # MIT License
└── README.md                    # This file

Installation & Setup

Prerequisites

- Android Studio Ladybug (2024.2.1) or later
- JDK 17 or later
- Android SDK with API level 24+
- Xcode 15+ (for iOS builds, macOS only)
- Git

Clone the Repo

bash
git clone https://github.com/Luwaphemi/DataTruth.git
cd DataTruth


Runing on Android

Option 1: Using Android Studio (Recommende)

1. Open Android Studio
2. Selet File → Open and navigate to the `DataTruth` folder
3. Wait for Gradle sync to complete
4. Connect an Android device or start an emulator
5. Select the composeApp run configuration
6. Click the Run button (▶️) or press `Shift+F10`

Option 2: Using Command Line

On Windows:
bash
.\gradlew.bat :composeApp:assembleDebug


On macOS/Linux:
bash
./gradlew :composeApp:assembleDebug


The APK will be genrated at:

composeApp/build/outputs/apk/debug/composeApp-debug.apk


Install on your device:
bash
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk


Android Permissions Required

The app requires the following permissions (automatically requested):
- `READ_PHONE_STATE` - To identify the carrier
- `PACKAGE_USAGE_STATS` - To read detailed data usage statistics

Running on iOS

Option 1: Using Xcode (macOS only)

1. Open the `iosApp/iosApp.xcodeproj` file in Xcode
2. Select a simulator or connected iOS device
3. Click the **Run** button or press `Cmd+R`

Option 2: Using Android Studio with KMP Plugin

1. Open the project in Android Studio
2. Select the iosApp run configuration
3. Choose an iOS simulator
4. Click Run

Option 3: Using GitHub Actions (No Mac Required)

The iOS framework is automatically built via GitHub Actions. Check the Actions tab for build status.


How to Use the App

Step 1: Launch & Welcome Screen
- Open the app
- View the welcome screen with the DataTruth logo
- Tap "Get Started" to enter the dashboard

Step 2: Set Up Your Data Plan
- On the dashboard, tap "Setup Plan"
- Enter your provider name (e.g., MTN, Airtel, Glo)
- Enter your data limit in GB
- Enter your billing cycle start day (1-31)
- Tap "Save"

Step 3: Capture Device Usage
- Tap "📱 Capture Usage" to record current device data usage
- View the breakdown of Mobile, WiFi, and Total usage

Step 4: Fetch Provider Data
- Tap "📡 Fetch Provider" to get provider-reported usage
- (Note: Currently uses mock data for demonstration)

Step 5: View Discrepancies
- The app automatically compares device vs provider data
- If a discrepancy is detected (>5% difference), you'll see:
  - ⚠️ Warning banner on the main stats card
  - 🚨 Detailed discrepancy cards with severity levels

Step 6: Monitor Usage
- View your usage summary with progress bar
- Check daily average consumption
- See days remaining in billing cycle
- Review usage history

Key Features Demonstration

1. Data Usage Monitoring
The app reads actual device data usage using Android's `TrafficStats` and `NetworkStatsManager` APIs:

kotlin
// Real device measurement
val mobileRx = TrafficStats.getMobileRxBytes()
val mobileTx = TrafficStats.getMobileTxBytes()
val mobileDataBytes = mobileRx + mobileTx


2. Discrepancy Detection
Automatic comparison with configurable thresholds:

kotlin
val differencePercentage = ((deviceUsage - providerUsage) / providerUsage) * 100
if (abs(differencePercentage) > 5.0) {
    // Alert user of discrepancy
}

3. Severity Levels
- LOW (< 5%) - Minor difference, likely normal
- MEDIUM (5-15%) - Notable difference, worth monitoring
- HIGH (15-30%) - Significant discrepancy, investigate
- CRITICAL (> 30%) - Major discrepancy, contact provider

Architecture

The app follows a clean architecture pattern with KMP:

┌─────────────────────────────────────────────────┐
│                    UI Layer                      │
│         (Compose Multiplatform - Shared)         │
├─────────────────────────────────────────────────┤
│                ViewModel Layer                   │
│            (Shared Business Logic)               │
├─────────────────────────────────────────────────┤
│               Repository Layer                   │
│          (Data Access - SQLDelight)              │
├─────────────────────────────────────────────────┤
│              Platform Layer                      │
│    ┌──────────────┐    ┌──────────────┐         │
│    │   Android    │    │     iOS      │         │
│    │ DataMonitor  │    │ DataMonitor  │         │
│    └──────────────┘    └──────────────┘         │
└─────────────────────────────────────────────────┘

Testing

Run unit tests:

bash
./gradlew test

Run Android instrumented tests:

```bash
./gradlew connectedAndroidTest


License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Author

Oluwafemi Akindele Sanya
- Post graduate student, Department of Computing, Afe Babalola University
- Email: sanyaluwafemi@abuad.edu.ng


Acknowledgments

- JetBrains for Kotlin Multiplatform and Compose Multiplatform
- The KMP Contest organizers
- Open source community

Learn More

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)


Made with ❤️ using Kotlin Multiplatform