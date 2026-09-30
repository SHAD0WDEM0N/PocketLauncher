# PocketLauncher

> **Android-first, controller-first retro gaming operating system.**  
> Designed to feel like a dedicated handheld console, not a generic Android app.

---

## 🎮 The Vision

Most Android retro gaming frontends act as external application dispatchers—kicking you out into separate emulator apps, losing UI consistency, and cluttering the experience.

**PocketLauncher** takes a console-first approach:

```
PocketLauncher (Jetpack Compose UI)
       │
       ▼
Native libretro Host Runtime (C++ / JNI)
       │
       ▼
Direct libretro Cores (mGBA)
       │
       ▼
Games (GB / GBC / GBA)
```

PocketLauncher handles the entire experience under one unified roof:
- **Zero app switching**: Runs game emulation directly inside the host runtime.
- **Unified state**: Save states, playtime tracking, game library management, and controller mapping remain integrated.
- **Distraction-free**: MinUI / NextUI simplicity paired with Analogue OS-inspired presentation.

---

## 🎯 Target Hardware

Engineered and optimized for dedicated Android retro handhelds:
- **Retroid Pocket G2**: Reference Android handheld hardware.
- **Mangmi Pocket Air Y**: Low-overhead target ensuring maximum performance and battery longevity.
- **Universal Android Handhelds & Controllers**: Retroid, Ayn, Anbernic, Logitech G Cloud, Razer Kishi, Backbone One, and Bluetooth gamepads.

---

## 🕹️ First Systems & Core

The initial release targets the iconic Nintendo handheld trilogy using a single, unified high-performance core:
- **Game Boy (GB)**
- **Game Boy Color (GBC)**
- **Game Boy Advance (GBA)**
- **Engine Core**: `mGBA` libretro core

Future phases expand to SNES, Genesis, and PS1 once the core pipeline is perfected.

---

## ✨ Features

- **Console Operating System Feel**: Pure dark tones (`#0D0D0D`), warm amber accents (`#FFB300`), crisp status bar (clock, battery, Wi-Fi), and contextual bottom button legend (`A` Select, `B` Back, `X` Search, `Y` System).
- **Controller-First Navigation**: 100% operable via physical D-pad, face buttons (A/B/X/Y), bumpers/triggers (L1/R1/L2/R2), and Start/Select. Zero touch required.
- **Hardware Input Diagnostic**: Built-in visual hardware controller test screen displaying real-time button actuation, stick deflection, and keycodes.
- **Native JNI Engine**: C++20 / C++17 foundation (`libpocketlauncher.so`) built with CMake and Android NDK for minimal input latency and high frame pacing consistency.

---

## 🚀 Releases

### **PocketLauncher 0.2 (First New Model Release)**
- **Phase 0: Foundation Architecture**
  - Modern Jetpack Compose UI architecture with customizable Console Theme tokens.
  - Native JNI bridge (`PocketEngine` / `native-lib.cpp`) with native runtime version reporting.
  - `PocketInput` unified controller event routing and button mapping.
  - Integrated Hardware Input Diagnostic Screen (`Settings -> Input Test`).
  - Native build support for `arm64-v8a`, `armeabi-v7a`, and `x86_64`.

Download the standalone APK directly from the [Releases](https://github.com/SHAD0WDEM0N/PocketLauncher/releases) page.

---

## 🛠️ Building from Source

### Prerequisites
- **Android Studio Ladybug (2024.2.1)** or newer
- **JDK 17** or **JDK 21**
- **Android SDK API 36** (minimum SDK 26 / Android 8.0)
- **Android NDK** (r27 / r28)
- **CMake 3.22.1+**

### Build Commands
```bash
# Clone the repository
git clone https://github.com/SHAD0WDEM0N/PocketLauncher.git
cd PocketLauncher

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

The compiled APK will be located in:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🗺️ Roadmap

- [x] **Phase 0**: Foundation, JNI Bridge, Console Theme, Input Test & Mapping
- [ ] **Phase 1**: Hardware Input Abstraction, custom remapping, stick deadzones
- [ ] **Phase 2**: Libretro C Host integration & audio/video frame buffer pipe
- [ ] **Phase 3**: mGBA core compilation & first in-launcher GBA boot
- [ ] **Phase 4**: Game Library Scanner, ROM metadata, and cover art caching
- [ ] **Phase 5**: Save state manager & playtime logging
- [ ] **Phase 6**: Quick Resume & suspend state

---

## 📄 License

GPL-3.0 License. See `LICENSE` for details.
All libretro cores remain the intellectual property of their respective authors under their respective open-source licenses.
