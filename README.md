# PocketLauncher

> **Android-first, controller-first retro gaming frontend and embedded libretro runtime.**  
> Designed to feel like a dedicated handheld console rather than a collection of separate Android emulator apps.

PocketLauncher is currently in active development. The present build supports the complete Nintendo Game Boy family through an in-app native libretro host, with library management, scraping, saves, save states, play history, touch support and a growing custom theme system.

---

## 🎮 Vision

Most Android retro frontends are launchers for other emulator applications. PocketLauncher is being built around a different idea: keep the library, interface and emulation experience under one roof wherever practical.

```text
PocketLauncher
│
├─ Jetpack Compose controller-first frontend
│  ├─ Home / system library
│  ├─ Favourites / Recently Played
│  ├─ Artwork & metadata
│  ├─ Themes
│  └─ Emulator / system settings
│
├─ PocketEngine (Kotlin / JNI)
│
├─ Native libretro host (C++ / Android NDK)
│
└─ libretro core
   └─ mGBA → GB / GBC / GBA
```

The long-term architecture is intended to support both **integrated libretro cores** and, where useful, **external Android emulators**, while keeping the PocketLauncher interface consistent.

---

## ✅ Current Status

### Supported systems

| System | Status | Current provider |
| --- | --- | --- |
| Game Boy | ✅ Working | mGBA libretro |
| Game Boy Color | ✅ Working | mGBA libretro |
| Game Boy Advance | ✅ Working | mGBA libretro |

The same mGBA installation currently services all three systems.

### Emulator runtime

- Native **C++ / JNI libretro host**
- Runtime loading of the downloaded mGBA core
- GB / GBC / GBA ROM launching inside PocketLauncher
- XRGB8888, RGB565 and 0RGB1555 frame handling
- Integrated stereo audio output
- Controller input forwarded directly to libretro
- Frame pacing based on the core-reported video FPS
- Automatic periodic save-RAM flushing
- ZIP ROM staging for supported systems

### Saves and in-game experience

- Battery / SRAM saves per system and game
- Multiple save-state slots
- Save-state screenshots / thumbnails
- In-game PocketLauncher menu
- Resume, save state, load state, restart and quit
- Per-system display preferences
- Sharp / smooth filtering
- Fit / integer / stretch scaling
- LCD grid, pixel grid and scanline effects
- Optional automatic GB / GBC / GBA system borders
- Configurable in-game menu hotkey
- Optional touch controls
- Optional on-screen menu button

### Library and metadata

- Separate ROM folder per system using Android's Storage Access Framework
- System enable / disable controls
- ROM scanning and cleaned display names
- Favourites
- Recently Played
- Last-played tracking
- Total playtime tracking
- TheGamesDB metadata and artwork scraping
- Local scrape cache
- Manual rescrape / missing-artwork / missing-metadata actions

### Frontend

- Controller-first navigation
- Touch navigation support
- Responsive layout intended for handheld aspect ratios
- Hardware input diagnostic / Input Test
- Front End Settings / Emulator Settings / Android System Settings separation
- Transparent system and game tiles so themes remain visible behind artwork

### Themes

PocketLauncher currently includes:

- Dark and Light modes
- Accent presets:
  - Amber
  - Blue
  - Green
  - Purple
  - Red
  - Pink
- Solid background
- Animated **PSP-inspired waves**
- Animated **PS2-inspired drifting orbs**
- Animated **Xbox-inspired energy glow**
- Animated **Pocket Hex** metallic / shifting hexagon theme
- Persistent theme preferences

Theme choices apply immediately and survive app restarts.

---

## 🎯 Development & Test Hardware

### Primary physical test device

- **AYANEO Pocket S** — current main physical development / testing handheld

### Additional target handhelds

- **Retroid Pocket G2** — 16:9 Android target expected to use the same main frontend layout behaviour as the Pocket S
- **Mangmi Air Y** — lower-overhead Android target
- **AYANEO Pocket Micro** — smaller-display / alternate-aspect-ratio target

### Touch testing

Touch behaviour is also tested using **Android Studio virtual devices**, so PocketLauncher remains usable without a physical controller even though the interface is designed controller-first.

---

## 🕹️ Controls

The frontend is designed to be completely usable with a handheld controller.

Typical navigation:

- **D-pad** — move selection
- **A** — select / confirm
- **B** — back
- **X / Y / shoulder buttons** — contextual actions where shown
- **Start / Select / stick buttons** — available to the runtime and configurable menu-hotkey combinations

Touch users receive equivalent tap / Back routes on supported screens.

---

## ⚙️ Emulator Settings

The current Emulator Settings area contains:

- **Manage Systems**
  - Enable or disable GB, GBC and GBA independently
- **Core Downloads**
  - Download, verify/load and remove the mGBA libretro core
  - Supports device ABI detection
- **External Emulators**
  - Planned

The current downloader is intentionally mGBA-specific. Replacing that with a reusable multi-core catalogue / provider architecture is one of the next major milestones.

---

## 🎨 Artwork & Scraping

PocketLauncher currently uses **TheGamesDB** for game metadata and artwork.

The API key is entered by the user and stored locally.

Available scrape actions include:

- Scrape new / missing games
- Repair missing artwork
- Repair missing metadata
- Rescrape configured handheld libraries

Current scraper integration targets GB, GBC and GBA.

---

## 🧱 Project Architecture

Main areas of the codebase:

```text
app/src/main/java/com/example/pocketlauncher/
├─ engine/
│  ├─ PocketEngine.kt
│  ├─ CoreDownloadManager.kt
│  ├─ EngineAudioPlayer.kt
│  ├─ BatterySaveManager.kt
│  ├─ SaveStateManager.kt
│  └─ RomRuntimeStager.kt
├─ input/
├─ library/
├─ scraper/
├─ theme/
└─ ui/
   ├─ common/
   ├─ emulation/
   ├─ home/
   ├─ input/
   ├─ platform/
   └─ settings/

app/src/main/cpp/
├─ core_loader.cpp
├─ core_loader.h
├─ libretro.h
├─ pocket_engine.cpp
└─ pocket_engine.h
```

The native host dynamically loads compatible libretro core libraries and provides the bridge between Compose / Kotlin and libretro video, audio, input, save RAM and state serialization.

---

## 🛠️ Building

### Requirements

- Android Studio 2024.2.1 or newer
- JDK 17
- Android SDK API 36
- Minimum Android SDK 26
- Android NDK 27.0.12077973
- CMake 3.22.1+

PocketLauncher currently builds:

- `arm64-v8a`
- `armeabi-v7a`
- `x86_64`

### Local debug build

```bash
git clone https://github.com/SHAD0WDEM0N/PocketLauncher.git
cd PocketLauncher
./gradlew assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 GitHub Actions Test Builds

The current active development / device-test line is **`develop`**.

A push to `develop` runs the existing **Android CI** workflow:

1. Set up JDK / Android SDK / NDK / CMake
2. Run unit tests
3. Build the debug APK
4. Upload the build as the **`pocketlauncher-debug`** artifact

The downloadable APK inside the artifact is:

```text
app-debug.apk
```

Development builds use the GitHub Actions run number for the `0.2.0-dev.*` version code/name.

---

## 🗺️ Roadmap

### Completed foundation

- [x] Jetpack Compose controller-first frontend
- [x] Native JNI / C++ engine
- [x] Native libretro host
- [x] Integrated video, audio and controller input
- [x] mGBA core download / loading
- [x] Game Boy support
- [x] Game Boy Color support
- [x] Game Boy Advance support
- [x] Per-system ROM folders
- [x] Enable / disable systems
- [x] ZIP ROM staging
- [x] Battery saves
- [x] Save states
- [x] Save-state thumbnails
- [x] In-game PocketLauncher menu
- [x] Per-system display options
- [x] Playtime and last-played tracking
- [x] Recently Played
- [x] Favourites
- [x] TheGamesDB metadata / artwork scraping
- [x] Touch gameplay controls
- [x] Touch frontend support
- [x] Hardware Input Test
- [x] Persistent frontend themes
- [x] Animated theme backgrounds

### Next: library presentation

- [ ] Dedicated Game Details screen
- [ ] Cartridge-style presentation for handheld games
- [ ] Richer media / metadata presentation
- [ ] Launch-count statistics
- [ ] Expanded play statistics / library stats

### Next: theme editor

- [ ] Custom JPG / PNG background selection
- [ ] Full colour wheel / continuous accent selector
- [ ] Optional PlayStation / Nintendo / Xbox-style button glyph packs
- [ ] Animation intensity / motion options if required

### Next: reusable emulator architecture

- [ ] Generic core catalogue
- [ ] Per-system default core
- [ ] Multiple compatible cores per system
- [ ] Per-game core / emulator override
- [ ] External Android emulator provider
- [ ] Remove mGBA-specific assumptions from the generic launch path

### Future systems

Once the reusable core pipeline is ready:

- [ ] NES
- [ ] SNES
- [ ] Mega Drive / Genesis
- [ ] PlayStation
- [ ] Additional systems after the core architecture is proven

### Longer-term runtime work

- [ ] Custom controller remapping
- [ ] Analogue stick deadzones
- [ ] Quick Resume / suspend state
- [ ] Additional libretro environment callbacks as new cores require them
- [ ] Performance / battery tuning across lower-power Android handhelds

---

## 🔭 Immediate Project Priorities

The current agreed development order is:

1. **Game Details and cartridge-style library presentation**
2. **Statistics improvements**
3. **Reusable Core / Emulator Manager**
4. **Additional console families**

This keeps new systems from becoming one-off hard-coded integrations.

---

## 📄 License

PocketLauncher is currently described as a **GPL-3.0** project.

Downloaded libretro cores remain subject to their respective upstream licenses and distribution requirements.
