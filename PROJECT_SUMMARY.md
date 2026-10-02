# 🛡️ Cyber Breach: Comprehensive Project & Architectural Summary

**Project Name:** Cyber Breach: Security Escape Room  
**Course / Subject:** Android Application Development / Advanced Android Programming  
**Language & Platform:** Java 11 | Android SDK 24+ (Target SDK 37) | Material 3  
**Repository:** [github.com/NightMare27-hub/CyberBreach](https://github.com/NightMare27-hub/CyberBreach)  

---

## 1. Executive Summary & Concept

**Cyber Breach** is an educational, mobile-first escape-room game where players assume the role of a junior **Security Operations Center (SOC) Analyst**. Instead of passively reading security theory, players investigate live simulated network incidents across fictional organizations (hospitals, schools, online stores) and deploy real defensive security tools to contain network breaches in real time.

> **Tagline:** *"Learn real cybersecurity by stopping a breach, not by reading about one."*

### Key Learning Objectives & Audience
- **Target Users:** CS/IT students, beginners curious about cybersecurity, and puzzle fans.
- **Educational Concepts Taught:** Port security (SSH vs. Telnet), brute-force login detection, web vulnerability patching (OWASP), log analysis, and incident response documentation.
- **Simulation Guarantee:** Everything is 100% simulated. All IP addresses use reserved RFC 5737 documentation ranges (`192.0.2.x`, `198.51.100.x`, `203.0.113.x`). No real scanning or exploit code is used.

---

## 2. Core Game Loop & Gameplay Features

```
┌──────────────┐     ┌────────────────┐     ┌──────────────┐     ┌────────────────┐
│   Briefing   │ ──> │ Terminal Room  │ ──> │ Result Screen│ ──> │ PDF Incident   │
│(Concept Card)│     │(Live Log Feed) │     │ (Stars & Stats)    │ Report Export  │
└──────────────┘     └────────────────┘     └──────────────┘     └────────────────┘
```

1. **Briefing Screen**: Displays a 30-second mission briefing card in a `WebView` explaining the security vulnerability, along with an optional `VideoView` explainer and an implicit intent link to OWASP documentation.
2. **Terminal Room**: A dark-themed monospace `TableLayout` streams simulated network alert logs every 1.8 seconds.
3. **Toolbelt Actions**: Players inspect traffic and select from 4 defense tools:
   - 🔍 **Packet Analyzer**: Inspects packet details and flags high-risk payloads.
   - 🛡️ **Firewall**: Blocks malicious source IP addresses or closes unencrypted ports.
   - 🔎 **Log Filter**: Filters the terminal logs by source address and highlights traffic in amber.
   - 🩹 **Patch Manager**: Patches unpatched vulnerable web services.
4. **Breach Meter & Audio/Haptics**:
   - In **Challenge Mode**, making a mistake adds penalty points to the Breach Meter, causes haptic vibration (`Vibrator`), and triggers alert audio (`SoundPool`).
   - As the breach exceeds 50%, a looping `MediaPlayer` alarm dynamically increases in volume up to 100%.
5. **Result & Debrief**: Displays an official incident outcome, a star rating, a performance breakdown table, and expert debrief lessons learned.
6. **Incident Response Report Export**: Generates an A4 PDF Incident Response Report drawn on a `Canvas`, exported via `MediaStore` / `FileProvider` to the device's public `Documents` directory.

---

## 3. Technical Architecture & Component Design

### A. Pure-Java Modular Engine Suite (`com.example.cyberbreach.engine`)
To ensure high testability, clean separation of concerns, and maintainability, all business logic is decoupled from Android UI classes into pure Java engines:

1. **`GameEngine`**: Manages real-time room ticks, countdown timers, breach meter calculations, tool action evaluations, and star/penalty modifiers.
2. **`LevelEngine`**: Handles level track organization (*Networking Basics*, *Common Attacks*, *Defense Systems*), 4-tier mastery evaluations (`LOCKED`, `UNLOCKED`, `COMPLETED`, `MASTERED`), `StarBreakdown` star rating evaluations, and threat concept filtering.
3. **`ToolEngine`**: Defines the 5-tier capability matrix for defense tools, firewall breach penalty reductions (from 15% down to 3%), log filter hint cost discounts (10s down to 2s), upgrade cost curves (`30 * tier`), and action diagnostic results.
4. **`StatsEngine`**: Manages Experience Points (XP) math, the 6-tier SOC Analyst Rank Ladder (*Trainee Analyst* → *Junior Incident Responder* → *SOC Analyst* → *Security Engineer* → *Senior Incident Lead* → *CISO*), daily streak credit multipliers (1.0x to 1.5x), and achievement badge triggers (`SPEED_DEMON`, `EAGLE_EYE`, `IRON_WALL`, `MASTER_ANALYST`).

---

### B. Persistence & Storage Layer
- **SQLite Database (`DbHelper`)**: Singleton `SQLiteOpenHelper` managing 4 tables:
  - `progress`: Level unlock states, best completion times, and star counts.
  - `attempts`: Complete history log of every played attempt.
  - `tool_upgrades`: Current tier levels for defense tools.
  - `player`: Player credit balance (earned from stars, spent on tool upgrades via atomic SQL transactions).
- **SharedPreferences (`PrefsManager`)**: Stores lightweight preferences including audio mute, haptic feedback, daily reminder flags, and consecutive daily streak calculations.
- **Private External Storage**: Saves downloaded level pack caches in `getExternalFilesDir(null)` (automatically removed when uninstalled) with fallback to internal storage.

---

### C. Network Architecture & Asynchronous Fetching
- **HTTPS REST JSON Client (`HttpClient`)**: Performs GET requests over `HttpURLConnection` with connect/read timeouts.
- **Network Validation (`NetworkUtil`)**: Queries `ConnectivityManager` to verify validated active internet access.
- **JSON Parser (`LevelParser`)**: Converts raw JSON packs (`JSONObject` & `JSONArray`) into domain models (`Level`, `LogEntry`).
- **Resilient Multi-Tier Fallback Repository (`LevelRepository`)**: Executes network calls on a background `ExecutorService` and posts results to the main thread via `Handler`. Implemented with a 3-tier fallback strategy:
  $$\text{Remote HTTPS Server} \longrightarrow \text{Local Private File Cache} \longrightarrow \text{Bundled Asset } (\mathtt{levels.json})$$
- **Process Death Protection (`CyberBreachApp`)**: The `Application` class pre-loads local level data into `LevelStore` at startup so process restoration never encounters an empty level list.

---

### D. Export Engine, Scoped Storage & Runtime Permissions
- **Canvas Report Drawing (`ReportRenderer`)**: A single drawing class renders an A4 report onto a `Canvas` (used both for the on-screen `Bitmap` preview and the `PdfDocument` page).
- **Public Storage Exporter (`ReportExporter`)**:
  - **Android 10+ (API 29+)**: Uses `MediaStore.Files` / `MediaStore.Downloads` with `IS_PENDING` scoped storage flags—**requiring zero runtime permissions**.
  - **Android 9 & Below (API 24–28)**: Saves to public `Documents/CyberBreach/` using legacy `WRITE_EXTERNAL_STORAGE` permission requested at runtime via `ActivityResultContracts.RequestPermission` with rationale dialogs, exposed safely via `FileProvider`.
- **Daily Notifications (`ReminderScheduler` & `ReminderReceiver`)**: Uses `AlarmManager.setInexactRepeating` at 6 PM to schedule daily challenge reminders, handling `POST_NOTIFICATIONS` runtime permission checks on Android 13+ (API 33+).

---

## 4. Course Syllabus & Unit Mapping Table

| Unit # | Syllabus Topic | Implemented Feature in Cyber Breach | Source Files / Components |
|---|---|---|---|
| **Unit 1** | Views & Core UI Layouts | Spinner mode/tool selectors, TabLayout + ViewPager2 navigation, WebView concept cards, ImageView badges, monospace TableLayout terminal | `activity_main.xml`, `activity_room.xml`, `fragment_learn.xml`, `HomePagerAdapter.java`, `LevelAdapter.java` |
| **Unit 2** | Event Handling & AV Controls | Row click/long-press handlers, CountDownTimer, Toast feedback, Modal AlertDialogs, SoundPool sfx, MediaPlayer looping alarm, Vibrator haptics | `RoomActivity.java`, `GameEngine.java`, `SoundManager.java` |
| **Unit 3** | Explicit/Implicit Intents & Data Passing | Activity screen flow (Play → Briefing → Room → Result), typed extra keys (`IntentKeys`), implicit browser intent (OWASP), implicit share intent (`ACTION_SEND`) | `BriefingActivity.java`, `ResultActivity.java`, `IntentKeys.java` |
| **Unit 4** | Internal Storage (SQLite & SharedPreferences) | SQLiteOpenHelper database for progress, attempts, credits, and upgrades; SharedPreferences for mute, haptics, and daily streak tracking | `DbHelper.java`, `PrefsManager.java`, `ProfileFragment.java` |
| **Unit 5** | Server Connection, JSON & Async Tasks | HTTPS GET JSON level pack fetching via `HttpURLConnection`, `JSONObject`/`JSONArray` parsing, `ExecutorService` background threads, Handler UI posting, offline fallbacks | `HttpClient.java`, `LevelParser.java`, `LevelRepository.java`, `LevelStore.java`, `CyberBreachApp.java` |
| **Unit 6** | External Storage & Runtime Permissions | Private external cache (`getExternalFilesDir`), public PDF report generation (`PdfDocument` & `Canvas`), Scoped Storage (`MediaStore`), `FileProvider`, `AlarmManager` daily reminders, `WRITE_EXTERNAL_STORAGE` & `POST_NOTIFICATIONS` runtime permissions | `ReportRenderer.java`, `ReportExporter.java`, `ReportActivity.java`, `ReminderScheduler.java`, `ReminderReceiver.java`, `file_paths.xml` |
| **Unit 7** | Testing, Hardening & Publishing | JUnit 4 unit testing suite (28 test cases), Gradle Kotlin DSL release configuration, `keystore.properties` protection in `.gitignore`, signed AAB bundle preparation | `GameEngineTest.java`, `LevelParserTest.java`, `LevelEngineTest.java`, `ToolEngineTest.java`, `StatsEngineTest.java`, `build.gradle.kts` |

---

## 5. Testing & Verification Summary

The project includes an automated JUnit 4 unit test suite covering 28 test cases with a **100% pass rate**:

```text
> Task :app:testDebugUnitTest
28 passed, 0 skipped, 0 failed
```

- **`GameEngineTest` (10 tests)**: Verifies mistake penalties, 3-star solves, breach thresholds, Learn vs. Challenge modes, hint costs, tool upgrade modifiers, and timer ticks.
- **`LevelParserTest` (4 tests)**: Verifies valid JSON pack parsing and rejection of malformed or empty payloads.
- **`LevelEngineTest` (5 tests)**: Verifies performance evaluation math, 4-tier level mastery rules, track prerequisites, and threat concept tag filtering.
- **`ToolEngineTest` (5 tests)**: Verifies firewall penalty scaling, log filter hint cost scaling, upgrade cost curves (`30 * tier`), and packet risk analysis.
- **`StatsEngineTest` (4 tests)**: Verifies XP calculations, 6-tier SOC rank ladder progression, daily streak credit multipliers, and achievement badge triggers.
