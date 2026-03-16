# 📍 Location Tracker Player

> A fully offline Android application that continuously tracks GPS position in the background, stores location history in a local Realm database, and replays routes as animated multi-colour polylines on an interactive map.

---

## 📱 Screenshots

| Login | Location History | Route Playback |
|-------|-----------------|----------------|
| Dark navy UI with teal accents | Stat chips + location cards | Animated polylines with progress bar |

---

## ✨ Features

- **🔐 Local Authentication** — Register and login with email/password stored in Realm. No server required.
- **📡 Background GPS Tracking** — WorkManager fires every 15 minutes to capture a fresh GPS fix and save it to the database.
- **🔔 Persistent Notification** — A foreground service shows a notification while tracking is active, with a Stop button.
- **🔄 Boot Auto-Restart** — A `BroadcastReceiver` listens for `BOOT_COMPLETED` and automatically restarts tracking after device reboot.
- **🛡️ Battery Optimization Exempt** — Requests Doze-mode exemption so the OS doesn't defer background work.
- **📲 OEM Auto-Start Support** — Opens manufacturer-specific autostart screens for Xiaomi, OnePlus, Oppo, Vivo, Huawei, and Samsung.
- **🗺️ Interactive Map** — OpenStreetMap via OSMDroid with multi-touch zoom and pan.
- **▶️ Route Playback** — Animate the stored route point-by-point with each segment drawn in a different colour. Play, Pause, Resume, Replay.
- **📊 Logcat Diagnostics** — Full structured logging in every background component for easy debugging.

---

## 🏗️ Architecture

```
UI Layer
  ├── LoginScreen / SignupScreen
  ├── LocationListScreen
  └── MapScreen (OSMDroid + route playback)
        │
Navigation Layer (Navigation3)
  ├── AppNavigator (mutable backStack)
  └── AppNavigatorHost (NavDisplay + slide animations)
        │
ViewModel Layer
  └── LocationViewModel (StateFlow<List<LocationHistory>>)
        │
Repository Layer
  └── LocationRepository (realm.query<LocationHistory>)
        │
Database Layer
  └── Realm (krdb 3.2.7) — User, LocationHistory, RealmManager singleton
        │
Background Layer
  ├── LocationScheduler — starts/stops WorkManager + ForegroundService
  ├── LocationWorker (CoroutineWorker) — fetches fresh GPS fix, writes to Realm
  ├── LocationForegroundService (START_STICKY) — persistent notification
  └── BootReceiver — restarts tracking on device reboot
```

---

## 🛠️ Tech Stack

| Component | Library | Version |
|-----------|---------|---------|
| Language | Kotlin | 2.1.20 |
| Build System | Android Gradle Plugin | 8.9.1 |
| UI | Jetpack Compose | BOM 2024.09.00 |
| Design | Material3 | via BOM |
| Navigation | Navigation3 | 1.0.1 |
| Database | Realm Kotlin (krdb fork) | 3.2.7 |
| Background Jobs | WorkManager | 2.9.1 |
| Location | Play Services Location | 21.3.0 |
| Maps | OSMDroid (OpenStreetMap) | 6.1.20 |
| Async | kotlinx-coroutines | 1.7.3 |
| Serialization | kotlinx-serialization | 1.7.3 |
| JVM Target | Java 17 | — |
| Min SDK | Android 9 (Pie) | API 28 |
| Target SDK | Android 16 | API 36 |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Meerkat or later
- JDK 17
- Android device or emulator (API 28+)

### Clone & Build

```bash
git clone https://github.com/yourusername/LocationTrackerPlayer.git
cd LocationTrackerPlayer
```

Open in Android Studio → **File → Open** → select the project folder.

Let Gradle sync complete, then run on a device or emulator.

---

## 📁 Project Structure

```
app/src/main/java/com/alpha/locationtrackerplayer/
├── MainActivity.kt                    # Entry point, permission chain
├── background/
│   ├── LocationWorker.kt              # WorkManager job — GPS fetch + Realm write
│   ├── LocationScheduler.kt           # Start/stop WorkManager + ForegroundService
│   ├── LocationForegroundService.kt   # Persistent notification, START_STICKY
│   ├── BootReceiver.kt                # Auto-restart on device reboot
│   └── PermissionManager.kt          # Runtime permission helpers + OEM autostart
├── data/
│   ├── database/RealmManager.kt       # Realm singleton
│   ├── model/
│   │   ├── User.kt                    # @PrimaryKey ObjectId, email, password, isLoggedIn
│   │   └── LocationHistory.kt        # userId, latitude, longitude, timestamp
│   └── repository/repository.kt      # LocationRepository — reactive Flow query
├── navigation/
│   ├── AppNavigator.kt                # Mutable backStack, navigate/pop/clearAndNavigate
│   ├── AppNavigatorHost.kt            # NavDisplay with slide animations
│   └── Screens.kt                     # Sealed interface — type-safe screen definitions
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt             # Dark-themed login with Show/Hide password
│   │   └── SignUpScreen.kt            # Signup with password strength indicator
│   ├── Location/
│   │   └── LocationListScreen.kt      # Stats header + location cards list
│   ├── map/
│   │   └── MapScreen.kt               # OSMDroid map + animated route playback
│   └── theme/
│       ├── Color.kt                   # AppColors design system (navy + teal)
│       ├── Theme.kt
│       └── Type.kt
└── viewModels/
    └── LocationViewModel.kt           # StateFlow<List<LocationHistory>>
```

---

## 🔑 Permissions

| Permission | Purpose |
|-----------|---------|
| `ACCESS_FINE_LOCATION` | GPS coordinates from FusedLocationProvider |
| `ACCESS_BACKGROUND_LOCATION` | Location access while app is in background (API 29+) |
| `POST_NOTIFICATIONS` | Show foreground service notification (API 33+) |
| `FOREGROUND_SERVICE` | Declare foreground service usage |
| `FOREGROUND_SERVICE_LOCATION` | Foreground service type = location (API 34+) |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Exempt from Doze mode |
| `RECEIVE_BOOT_COMPLETED` | Auto-restart tracking after reboot |
| `WAKE_LOCK` | Keep CPU awake during WorkManager execution |

Permissions are requested in the correct sequential order at app launch:
1. Fine location → 2. Background location → 3. Notifications

---

## 🗺️ Route Playback

When you open the map screen, all recorded location points are loaded sorted by timestamp. Tap **▶ Play Route** to animate the journey:

- Each polyline segment between consecutive points is drawn in a different colour from an 8-colour palette
- The map camera follows the current point with smooth `animateTo()` animation
- A progress bar shows current position in the route
- Controls: **Play → Pause → Resume → Replay** and **Show All** to see the complete route instantly

---


## ⚠️ Known Limitations

- **15-minute minimum interval** — WorkManager enforces a minimum 15-minute periodic interval on Android. For testing, trigger the worker manually from Android Studio's **App Inspection → Background Task Inspector**.
- **GPS accuracy** — `getCurrentLocation()` with `PRIORITY_HIGH_ACCURACY` is used to force a fresh fix. If GPS is disabled or the device is indoors, the worker returns `Result.success()` and waits for the next interval rather than retrying unnecessarily.
- **Realm deprecation** — The official Realm Kotlin SDK was deprecated in September 2025. This project uses the **[krdb community fork](https://github.com/xilinjia/krdb)** (`io.github.xilinjia.krdb:3.2.7`) which provides full Kotlin 2.x compatibility with an identical public API.

---

## 📦 Dependency Notes

Getting all dependencies to build together required resolving several compatibility conflicts. The key decisions:

- **AGP 8.9.1** — required by Navigation3 1.0.1, core-ktx 1.18.0, and activity-compose 1.13.0
- **Kotlin 2.1.20** — pulled transitively by AGP 8.9.1's internal stdlib
- **krdb 3.2.7** — official Realm SDK crashes on Kotlin 2.1.x due to internal FIR/IR compiler API changes; krdb patches these
- **JVM 17** — required because krdb is compiled targeting JVM 17
- **Compose BOM 2024.09.00** — compatible with Kotlin 2.x; the 2026 BOM versions would require AGP 8.13+ which pulls Kotlin 2.3.x (incompatible with krdb)

---

## 📄 License

```
MIT License

Copyright (c) 2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---
