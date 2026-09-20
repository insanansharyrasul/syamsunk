<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="128" height="128" alt="SyamsunK icon" />
</p>

<h1 align="center">SyamsunK</h1>

<p align="center">
  An ad-free, offline prayer times app for Android.
</p>

<p align="center">
  <a href="#about">About</a> •
  <a href="#features">Features</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#privacy-and-permissions">Privacy & Permissions</a> •
  <a href="#troubleshooting">Troubleshooting</a> •
  <a href="#development">Development</a> •
  <a href="#license">License</a>
</p>

---

## About

SyamsunK is an Android app that calculates daily Islamic prayer times (Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha). It operates locally on your device without advertisements, user accounts, or background data collection.

The name comes from Arabic *ash-shams* (الشمس, meaning the Sun), referring to how prayer times follow the sun's position across the sky. The *K* marks Kotlin, created as a rewrite of an earlier Flutter version.

## Features

- Computes Fajr, Sunrise (Syuruq), Dhuhr, Asr, Maghrib, and Isha based on geographical coordinates.
- Displays a live countdown to the upcoming prayer.
- Schedules prayer notifications through Android AlarmManager to trigger reliably during system sleep (Doze mode).
- Includes a home screen widget built with Jetpack Glance to check prayer schedules without opening the app.
- Supports regional calculation conventions:
  - Muslim World League (MWL)
  - Umm al-Qura University, Makkah
  - Egyptian General Authority of Survey
  - University of Islamic Sciences, Karachi
  - MUIS (Singapore)
  - ISNA (North America)
  - Dubai
  - Qatar
  - Kuwait
  - Moonsighting Committee Worldwide
- Supports Standard (Shafi'i, Maliki, Hanbali) and Hanafi Asr calculation methods.
- Supports system dark theme.
- Works offline once initial location coordinates are saved.

## Installation

### Google Play

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.insan.syamsunk)

### GitHub Releases

Download standalone APK files directly from the [Releases](https://github.com/insanansharyrasul/syamsunk/releases) page and install them manually.

## Usage

1. **Location setup**: Open the app and grant location permission. SyamsunK retrieves your coordinates once to establish your local prayer schedule.
2. **Configuration**: Open Settings to set your regional calculation authority and preferred Asr madhab.
3. **Home screen widget**: Long-press on your home launcher, open Widgets, select SyamsunK, and place the widget on your screen.

## Privacy and Permissions

SyamsunK does not include trackers, analytics frameworks, or advertising libraries. Location coordinates and user settings are stored locally on the device using Jetpack DataStore.

| Permission | Purpose |
| :--- | :--- |
| `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` | Obtains coordinates once to calculate local prayer times. |
| `POST_NOTIFICATIONS` | Delivers prayer time reminders on Android 13 (API 33) and above. |
| `SCHEDULE_EXACT_ALARM` | Triggers prayer notifications at the exact scheduled minute. |
| `RECEIVE_BOOT_COMPLETED` | Restores scheduled alarms when the device reboots. |

## Troubleshooting

### Notification Delays

Certain Android distributions (such as MIUI, HyperOS, EMUI, HarmonyOS, One UI, and OxygenOS) aggressively restrict background tasks and alarms. If reminders fail to trigger on time:

1. Go to **Settings > Apps > SyamsunK**.
2. Confirm that **Notifications** are permitted.
3. Set **Battery Usage** or **Background Restrictions** to **Unrestricted** (or disable battery optimization for SyamsunK).

### Schedule Discrepancies

If calculated times differ from your local mosque, open **Settings** and ensure that the selected calculation method and Asr madhab match the standards used by your local Islamic authority.

## Development

### Technical Overview

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Widget**: Jetpack Glance
- **Calculations**: [Adhan Java](https://github.com/batoulapps/adhan-java)
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow
- **Storage**: Jetpack DataStore Preferences
- **Scheduling**: Android AlarmManager

### Building Locally

Prerequisites: Android Studio Ladybug or later, Android SDK with API 37, and a device or emulator running Android 8.0 (API 26) or higher.

```bash
git clone https://github.com/insanansharyrasul/syamsunk.git
cd syamsunk
./gradlew assembleDebug
```

Contributions and bug reports can be submitted through the [issue tracker](https://github.com/insanansharyrasul/syamsunk/issues) and pull requests.

## License

This project is licensed under the [MIT License](LICENSE).
