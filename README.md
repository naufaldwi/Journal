# Journal — Android Life Simulation

![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Latest APK](https://img.shields.io/badge/APK-v1.0.0%20Included-brightgreen)

**Journal** is a serene, deep life-simulation game for Android built with Kotlin and Jetpack Compose. Guide characters year-by-year from first breath to legacy across 14 culturally grounded nations—balancing physical & mental wellness, skill mastery, conservatories & universities, fame-driven or classic careers, live streaming, multi-generation family trees, and regional underworld paths.

---

## Download & Install APK (Android)

A freshly compiled, ready-to-install APK is included right inside this repository:

- **Direct APK file:** [`releases/journal-v1.0.0.apk`](releases/journal-v1.0.0.apk)
- **GitHub Releases:** Visit the **Releases** tab on GitHub for versioned `.apk` downloads and SHA-256 checksums.

### Sideload Instructions on Android
1. Download `journal-v1.0.0.apk` (from [`releases/journal-v1.0.0.apk`](releases/journal-v1.0.0.apk) or the **Releases** page) onto your Android phone or tablet (**Android 7.0 / API 24 or higher**).
2. Tap the downloaded `.apk` file in your browser downloads or **Files** app.
3. If Android prompts *"For your security, your phone isn't allowed to install unknown apps from this source"*, tap **Settings** and toggle **Allow from this source**.
4. Tap **Install**, wait a moment, and open **Journal**.
5. Choose **Sign in with Google** (cloud saves & global leaderboard) or **Play as Guest** (instant offline play stored locally on your device).

> **SHA-256 Checksum (`releases/journal-v1.0.0.apk`):**  
> `2930466b27d7f2338285f6b8cafece4bb0d82739fdad8e4efc80dca8e8e989df`

---

## Key Features Showcase

| System | Highlights |
| --- | --- |
| **14 Countries & Deep Representation** | Richly localized cities, naming pools, current socioeconomic events, and crime groups across **Indonesia** (Jakarta, Yogyakarta, Bandung, Surabaya, Denpasar, Medan, Makassar), **Japan, Hong Kong, Italy, United States, United Kingdom, South Korea, Portugal, Denmark, India, Brazil, Australia, Canada, and Nigeria**. |
| **Skill Bars & Training Activities** | 8 core skills (*Deduction & Law*, *Engineering & Logic*, *Painting & Visual Art*, *Music, Guitar & Vocals*, *Acting & Stagecraft*, *Athletics & Racecraft*, *Culinary & Craft*, *Persuasion & Rhetoric*) trained via 8 dedicated activities and required for career gates & promotions. |
| **Automatic School Skill Growth** | Childhood school (ages 6–17) builds foundational skills, while **Law School**, **Art & Design Conservatory**, **Music Conservatory**, **University**, and **Craft & Motorsport Institute** automatically raise matching skills & core stats on enrollment, annually, and at graduation. |
| **Broad Fame & Classic Careers** | Become a **Lawyer / Supreme Court Justice**, **Painter**, **Stage & Screen Actor**, **Runway Model**, **Solo Singer**, **Band Guitarist or Drummer**, **Grand Prix Race Driver**, **Software Engineer**, **Physician**, or **Chef**—with realistic annual **Fame decay** unless you keep touring, releasing work, or streaming. |
| **Culturally Accurate Underworld** | Try high-risk shadow careers localized by region: **Preman** (Indonesia), **Yakuza** (Japan), **Triad** (Hong Kong), **Mafia** (Italy), **Gangster** (US), and more—with heat/notoriety and prison sentences. |
| **Creator Studio & Live Streaming** | Pick your niche, publish trend-aligned posts, manage community replies, sign brand sponsorships, launch merch lines, upgrade studio gear with Sparkles (`✦`), and host **Live Streams** with real-time viewer & currency payouts. |
| **Health, Mental Wellness & Era Events** | Track Health, Energy, Fitness, Disease Risk, Happiness, Stress, and Anxiety shaped by diet, exercise, sleep, preventive checkups, therapy, plus periodic random life twists and historical era shifts. |
| **Non-Negative Cash & Adult Loans** | Cash on hand never goes below `$0`. Minors remain debt-free, while adults (`18+`) can borrow up to `$150,000` (4% annual interest) for tuition, merch, or emergencies and repay flexibly. |
| **Aging Milestones & Family Tree** | Dynamic character avatar updates, relationship evolution across life stages, an interactive multi-generation **Family Tree** lineage tracker, and **Daily Missions** (unlocked at **Age 13+**) with XP leveling and daily trio bonuses. |
| **Accounts & Offline Guest Mode** | **Google Sign-In** (via Jetpack Credential Manager + Firebase Auth & Cloud Firestore) or instant offline **Guest Mode** (Jetpack DataStore), with optional cross-platform Apple Sign-In support configurable in Firebase Authentication. |

---

## Publishing a New GitHub Release

This repository includes an automated workflow at [`.github/workflows/android-release.yml`](.github/workflows/android-release.yml).

### Option A — Automatic Release via Git Tag (Recommended)
Push the project to GitHub and push any tag starting with `v`:

```bash
git add .
git commit -m "Add release APK, README, and release workflow"
git push origin main

git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions will automatically run `:app:testDebugUnitTest`, compile the APK, generate a `.sha256` file, and publish a **GitHub Release** with the installable `.apk` attached.

### Option B — Manual Upload via GitHub UI
1. Go to your repository on GitHub → **Releases** → **Draft a new release**.
2. Create a tag (e.g., `v1.0.0`) and title.
3. Drag and drop `releases/journal-v1.0.0.apk` into the **Attach binaries** box and click **Publish release**.

*(Optional production signing: if you want GitHub Actions to also produce a release-key-signed APK, add `RELEASE_KEYSTORE_BASE64`, `STORE_PASSWORD`, and `KEY_PASSWORD` to **Settings → Secrets and variables → Actions**.)*

---

## Developer Setup & Local Build

### Tech Stack
- **Language & UI:** Kotlin `2.2.10`, Jetpack Compose (BOM `2024.09.00`), Material Design 3, Navigation Compose
- **Architecture:** MVVM with `ViewModel`, Kotlin Coroutines & `StateFlow`, deterministic `LifeEngine`
- **Persistence & Auth:** Firebase Authentication, Cloud Firestore, Jetpack Credential Manager (Google Sign-In), Jetpack DataStore (local Guest storage)
- **Build & Test:** Android Gradle Plugin `9.1.1`, Gradle `9.3.1`, Compile/Target SDK `36`, Min SDK `24`, JUnit 4 + Robolectric

### Prerequisites
- **JDK 17** (or higher compatible with AGP 9.1.1)
- **Android SDK** (`compileSdk 36`, `ANDROID_SDK_ROOT` configured)
- **Gradle `9.3.1`** installed on `PATH`

### Build & Test Commands

1. **Restore debug keystore (if building a fresh clone without one):**
   ```bash
   if [ ! -f debug.keystore ] && [ -f debug.keystore.base64 ]; then
     base64 -d debug.keystore.base64 > debug.keystore
   fi
   ```

2. **Optional environment secrets:**
   Copy `.env.example` to `.env` if adding any custom API keys (no Gemini API key is required for core gameplay; local `.env` and `debug.keystore` remain gitignored).

3. **Run unit tests:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

4. **Compile the installable APK:**
   ```bash
   gradle :app:assembleDebug
   ```
   The output APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Firebase Notes
- **Guest Mode** works completely offline without any backend setup.
- Cloud saves, public leaderboards, and Google Sign-In use Firebase Auth + Cloud Firestore configured via `app/google-services.json`, schema blueprint (`firebase-blueprint.json`), and security rules (`firestore.rules`).
- Cross-platform Apple Sign-In can also be enabled for iOS companions in **Firebase Console → Authentication → Sign-in method**.
