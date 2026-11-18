# Journal — Android Life Simulation

![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Latest APK](https://img.shields.io/badge/APK-v1.1.5%20Included-brightgreen)

**Journal** is a serene, deep life-simulation game for Android built with Kotlin and Jetpack Compose. Designed around a calm **4-tab layout** (**Journal**, **Activities**, **Tree**, **Profile**) with focused submenu pages and bottom sheets, it lets you guide characters year-by-year from first breath to legacy across 14 culturally grounded nations without visual clutter.

> **Disclaimer — Google Login & Firebase:**  
> You can sign in to **Journal** using your **Google Account** to back up saves and compete on global leaderboards. Account authentication, cloud saves, and leaderboard syncing use **Google Firebase** (**Firebase Authentication** and **Cloud Firestore**). If you prefer not to sign in, you can choose **Play as Guest** anytime to keep all saves 100% locally on your device.

---

## Download & Install APK (Android)

A freshly compiled, ready-to-install `v1.1.5` APK is included right inside this repository:

- **Direct APK file:** [`releases/journal-v1.1.5.apk`](releases/journal-v1.1.5.apk)
- **Release Notes:** See [`CHANGELOG.md`](CHANGELOG.md) for the full `v1.1.5`, `v1.1.0`, and `v1.0.0` update log.
- **GitHub Releases:** Visit the **Releases** tab on GitHub for versioned `.apk` downloads and SHA-256 checksums.

### Sideload Instructions on Android
1. Download `journal-v1.1.5.apk` (from [`releases/journal-v1.1.5.apk`](releases/journal-v1.1.5.apk) or the **Releases** page) onto your Android phone or tablet (**Android 7.0 / API 24 or higher**).
2. Tap the downloaded `.apk` file in your browser downloads or **Files** app.
3. If Android prompts *"For your security, your phone isn't allowed to install unknown apps from this source"*, tap **Settings** and toggle **Allow from this source**.
4. Tap **Install**, wait a moment, and open **Journal**.
5. Choose **Sign in with Google** (powered by **Firebase** for cloud saves & global leaderboards) or **Play as Guest** (instant offline play stored locally on your device).

> **SHA-256 Checksum (`releases/journal-v1.1.5.apk`):**  
> `763a7f50e73a49671ae346c199d0364a26c46369392d3c4a01c167e02254b220`

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
| **Office Dynamics & Global News** | Career screen tracker for colleague, manager, and teacher bonds with a visual **Life Miserable** status indicator, plus an annual **Global News Feed** updating with regional wars, science breakthroughs, and cultural shifts. |
| **Health, Mental Wellness & Mood Trends** | Track Health, Energy, Fitness, Disease Risk, Happiness, Stress, and Anxiety, alongside a **10-Year Mood Trends** chart highlighting career stress load and random encounter impacts. |
| **Romance, Multi-Child & Relationship Settings** | One-time orientation choice, dating apps/blind dates/friend-to-partner flows with rejection odds, anti-cheating partner checks, neglect decay, multi-child families, and a **Relationship Settings** priority dashboard (*Standard*, *Interact*, *Gift $150*). |
| **Social Stream & Daily Random Events** | Post **Life**, **Career**, and **Relationship** updates to earn popularity-scaled likes and NPC comments, plus trait-tailored **Daily Random Events** (finding cash, lost items, old acquaintances). |
| **Non-Negative Cash & Adult Loans** | Cash on hand never goes below `$0`. Minors remain debt-free, while adults (`18+`) can borrow up to `$150,000` (4% annual interest) for tuition, merch, or emergencies and repay flexibly. |
| **Aging Milestones & Family Tree** | Dynamic character avatar updates, relationship evolution across life stages, an interactive multi-generation **Family Tree** lineage tracker, and **Daily Missions** (unlocked at **Age 13+**) with XP leveling and daily trio bonuses. |
| **Calm 4-Tab & Submenu UI** | Story-first **Journal** feed with quick bottom sheets for Health, Global News, and Daily Missions, plus an **Activities Hub** routing cleanly into **Education & Careers**, **Skills & Adult Loans**, **Creator Studio**, **Social Stream**, **Underworld**, **Relationships**, and **Relationship Settings**. |
| **Accounts & Firebase Cloud Sync** | **Google Sign-In** (via Jetpack Credential Manager) powered by **Firebase Authentication** & **Cloud Firestore** for saves and leaderboards, plus 100% offline **Guest Mode** (Jetpack DataStore). |

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


### Firebase Notes
- **Guest Mode** works completely offline without any backend setup.
- Cloud saves, public leaderboards, and Google Sign-In use Firebase Auth + Cloud Firestore configured via `app/google-services.json`, schema blueprint (`firebase-blueprint.json`), and security rules (`firestore.rules`).
- Cross-platform Apple Sign-In can also be enabled for iOS companions in **Firebase Console → Authentication → Sign-in method**.
