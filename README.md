# Clio Audiobook Player (Beta)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)

A minimalistic, lightning-fast, and distraction-free local audiobook player for Android.

> **Status: Beta** — This app is currently in beta. Feedback and issue reports are welcome.

---

## What's New & Changelog (vs. Original)

Clio is a specialized fork built upon the open-source foundation of [Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek, featuring significant UI modernizations, new player capabilities, and comprehensive internal performance optimizations:

### New Player & UI Features
- **Playback Screen Lock:** Dedicated lock button to disable touchscreen interactions while playing, preventing accidental skips or pauses in your pocket.
- **Integrated 5-Band Graphic Equalizer:** Real-time audio equalizer with presets (Spoken Word, Bass Boost, Treble Boost, Vocal Clarity) and custom frequency sliders.
- **Refined Navigation & Controls:** Sleek collapse arrow buttons instead of the generic close "X" icon.
- **Modernized UI & Adaptive Icon:** Refreshed Material You layout with a custom warm orange-red gradient adaptive icon.
- **"NEW" Badge Indicator:** Clear visual badge on freshly added and unplayed audiobooks in the library.
- **Percentage Progress Indicator:** Exact listening progress percentage displayed across book cards, chapter rows, and player views.
- **Auto-Open Last Audiobook on Startup:** Option to automatically launch straight into the last played audiobook when opening the app.
- **Independent Skip & Rewind Durations:** Skip-forward and rewind times can now be configured separately (e.g., 30s forward, 10s backward).
- **Customizable Playback Backgrounds:** Choose between cover art blur, dynamic theme colors, and solid dark backgrounds.

### Performance & Engine Improvements
- **Instant Cold Start:** Near-instant application startup powered by Android Baseline Profiles and lean composable initialization.
- **Optimized Audio Rendering Pipeline:** Custom audio-only renderers factory with reduced playback latency and minimal CPU overhead.
- **Faster Library Scanning & Database:** Streamlined Room database queries and multi-threaded chapter/metadata parsing.
- **Zero Telemetry & Bloat:** Completely free of background analytics, ads, and external donation prompts.
- **100% German & English Localization:** Full translation coverage for all settings, dialogs, and controls.

---

## Download Release

The pre-compiled APK is available on the [Releases](https://github.com/mryx007/clio-audiobook-player/releases) page:

- **`app-free-release.apk`** — Standalone, optimized release build.

---

## Credits & Acknowledgements

Clio Audiobook Player is a fork developed from the open-source project [Voice](https://github.com/PaulWoitaschek/Voice) created by **Paul Woitaschek**. 
All original work remains under the **GNU General Public License v3.0 (GPLv3)**.

---

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
