# 🎓 Cyber Breach: Project Study & Viva Preparation Guide

Welcome to the **Cyber Breach Master Study Guide**! This document is designed to help you thoroughly understand every line of code, architectural choice, data flow, and design pattern in your project so you can confidently explain it to your professor, viva examiner, or presentation audience.

---

## 📚 Table of Contents
1. **Module 1**: High-Level Overview, User Flow & Architecture
2. **Module 2**: Pure-Java Modular Engine Suite (`com.example.cyberbreach.engine`)
3. **Module 3**: Storage & Database Layer (SQLite, SharedPreferences, Scoped Storage)
4. **Module 4**: Networking, REST JSON & Resilient 3-Tier Fallback System
5. **Module 5**: Report Export Engine, Notifications & Unit Testing
6. **Module 6**: Top 15 Viva Questions & Model Answers

---

## Module 1: High-Level Overview, User Flow & Architecture

### 1.1 Project Concept
- **App Name:** Cyber Breach: Security Escape Room
- **Tagline:** *"Learn real cybersecurity by stopping a breach, not by reading about one."*
- **Role:** The player plays as a junior **Security Operations Center (SOC) Analyst** managing live incident responses.
- **Safety Guarantee:** Everything is 100% simulated. IP addresses use reserved RFC 5737 documentation ranges (`192.0.2.x`, `198.51.100.x`, `203.0.113.x`). No real scanning or exploit code is used.

### 1.2 Screen Flow & Navigation Architecture
Your app uses a modern **5-Tab Command Hub** (`MainActivity` + `BottomNavigationView` + `ViewPager2`):

```text
┌─────────────────┐
│  LoginActivity  │  (First launch: Set Analyst Handle)
└────────┬────────┘
         │
┌────────┴────────┐
│TutorialActivity │  (Interactive Identify -> Mitigate SOC Onboarding)
└────────┬────────┘
         │
┌────────┴────────┐
│  MainActivity   │  (5-Tab Command Hub)
└┬───────┬───────┬┴───────┬────────┐
 │       │       │        │        │
 ▼       ▼       ▼        ▼        ▼
Home   Missions Learn   Armory   Profile
```

### 1.3 Core Incident Response Flow
1. **Briefing Screen (`BriefingActivity`)**: Pre-room card rendering concept HTML in a `WebView`, optional explainer video in a `VideoView`, and an implicit intent link to OWASP documentation.
2. **Incident Terminal (`RoomActivity`)**: Monospace log feed streaming simulated traffic.
3. **Two-Stage SOC Response**:
   - **Stage 1 (Identify)**: Use `PACKET_ANALYZER` to flag the malicious entry (row highlights in **SOC Warning Yellow**).
   - **Stage 2 (Mitigate)**: Select a defense tool (**Firewall**, **Patch Manager**, **Isolation Framework**, **Email Gateway**, **Crypto Manager**) to block or patch the threat.
4. **Result Screen (`ResultActivity`)**: Displays stars earned, time, mistakes, debrief text, and an **"Incident Report"** export button.
5. **PDF Report Exporter (`ReportActivity`)**: Draws an A4 report on a `Canvas`, exports a PDF file to `Documents/CyberBreach/`, and shares it via the system Share sheet.

---

## Module 2: Pure-Java Modular Engine Suite

Why did we build a dedicated `engine` package?
> **Key Viva Concept:** Decoupling business logic from Android UI classes (`Activity` / `Fragment`) adheres to the **Single Responsibility Principle (SRP)**. It makes the game rules 100% unit-testable on the JVM without needing an emulator!

### 2.1 `GameEngine` (Active Room Engine)
- **Role**: Tracks active room state, countdown timer ticks, breach percentage, and tool action outcomes.
- **Two-Stage Rule**: Enforces that a threat MUST be flagged via `Tool.PACKET_ANALYZER` before mitigation tools can act (prevents reckless guessing!).

### 2.2 `LevelEngine` (Progression & Mastery Engine)
- **Role**: Manages track prerequisites, difficulty scaling (*EASY*, *MEDIUM*, *HARD*, *EXPERT*), concept tag filtering, and 4-tier level mastery:
  - `LOCKED`: Prerequisites not met.
  - `UNLOCKED`: Available to play.
  - `COMPLETED`: Solved at least once.
  - `MASTERED`: Solved with 3 stars, 0 hints, and 0 mistakes in Challenge mode.
- **`StarBreakdown` Evaluator**:
  - Star 1: Contain the breach.
  - Star 2: Precision (< 2 mistakes).
  - Star 3: Mastery (0 hints used under target time).

### 2.3 `ToolEngine` (Defense Toolbelt Engine)
- **Role**: Manages capabilities and tier scaling (Tiers 1 to 5) for all 7 defense tools:
  - **Firewall**: Reduces mistake breach penalty ($15\% \rightarrow 12\% \rightarrow 9\% \rightarrow 6\% \rightarrow 3\%$).
  - **Log Filter**: Reduces hint time cost ($10\text{s} \rightarrow 8\text{s} \rightarrow 6\text{s} \rightarrow 4\text{s} \rightarrow 2\text{s}$).
  - **Packet Analyzer**: Adds `[HIGH RISK]` anomaly keywords at Tier 2+.
  - **Upgrade Pricing Curve**: $\text{Cost} = 30 \times \text{tier}$ credits.

### 2.4 `StatsEngine` (XP, Ranks, Streaks & Achievements)
- **Role**: Calculates Experience Points ($\text{XP} = \text{stars} \times 50 + \text{attempts} \times 10 + \text{clears} \times 100$), streak credit multipliers (+10% per day up to +50%), and 6-tier SOC ranks:
  1. *Trainee Analyst* (0-2 stars)
  2. *Junior Incident Responder* (3-5 stars)
  3. *SOC Analyst* (6-9 stars)
  4. *Security Engineer* (10-14 stars)
  5. *Senior Incident Lead* (15-19 stars)
  6. *Chief Information Security Officer / CISO* (20+ stars)
- **Achievement Badges**: Evaluates 5 trophies (⚡ *Speed Demon*, 🎯 *Eagle Eye*, 🛡️ *Iron Wall*, 🎓 *Scholar*, 💼 *Master Analyst*).

---

## Module 3: Storage & Database Layer

### 3.1 SQLite Database (`DbHelper` v3)
- **Pattern**: Thread-safe Singleton pattern using `synchronized DbHelper.get(Context)`.
- **5 Tables**:
  - `progress`: `level_id`, `unlocked`, `best_time_sec`, `stars`.
  - `attempts`: `id`, `level_id`, `mode`, `time_sec`, `hints_used`, `success`, `played_at`.
  - `tool_upgrades`: `tool_id`, `tier`.
  - `player`: `id = 1`, `credits`, `username`, `tutorial_completed`.
  - `achievements`: `achievement_id`, `unlocked_at`.
- **Atomic Transactions**: Tool upgrades use `db.beginTransaction()` and `db.setTransactionSuccessful()` to ensure credits are never deducted if the upgrade fails.

### 3.2 SharedPreferences (`PrefsManager`)
- Stores lightweight key-value pairs: sound mute, haptics, daily reminder switch, font scale, and last play date for daily streak calculations.
- Uses `.apply()` (asynchronous background write) rather than `.commit()` (blocking main thread write).

---

## Module 4: Asynchronous REST Networking & 3-Tier Fallback

### 4.1 HTTPS REST Client (`HttpClient`)
- Uses `HttpURLConnection` GET over HTTPS with 8000ms connect/read timeouts.
- Must run on background worker threads (`ExecutorService`) to prevent `NetworkOnMainThreadException`.

### 4.2 Network Validation (`NetworkUtil`)
- Queries `ConnectivityManager` and checks `NET_CAPABILITY_INTERNET` and `NET_CAPABILITY_VALIDATED` to ensure active internet before attempting requests.

### 4.3 Resilient 3-Tier Fallback Architecture (`LevelRepository` & `LessonRepository`)

$$\text{GitHub Pages Remote HTTPS} \longrightarrow \text{Local Private File Cache} \longrightarrow \text{Bundled Assets } (\mathtt{levels.json} / \mathtt{lessons.json})$$

- **GitHub Pages Endpoints**:
  - Levels: `https://nightmare27-hub.github.io/CyberBreach/levels.json`
  - Lessons: `https://nightmare27-hub.github.io/CyberBreach/lessons.json`
- **Process Death Protection (`CyberBreachApp`)**: The `Application` class pre-loads local level data into `LevelStore` at startup, ensuring that if Android kills and restores the process, the app never crashes from an empty level list!

---

## Module 5: PDF Export, Permissions & Testing

### 5.1 PDF Report Generator (`ReportRenderer` & `ReportExporter`)
- **`ReportRenderer`**: Pure drawing class that draws an A4 report (595 x 842 points) onto a `Canvas`. The same method draws the on-screen `Bitmap` preview in `ReportActivity` and the `PdfDocument` page.
- **`ReportExporter`**:
  - **Android 10+ (API 29+)**: Uses Scoped Storage (`MediaStore.Files`) with `IS_PENDING` flags—**requiring zero storage permissions**.
  - **Android 9 & Below (API 24-28)**: Saves to public `Documents/CyberBreach/` using legacy `WRITE_EXTERNAL_STORAGE` permission, exposed via `FileProvider`.

### 5.2 Runtime Permissions
- Uses `ActivityResultContracts.RequestPermission()` registered as Activity fields.
- Checks `shouldShowRequestPermissionRationale()` to present a `MaterialAlertDialogBuilder` explanation before requesting permission.

### 5.3 Automated Unit Testing
- **30 Unit Tests (100% Pass Rate)**:
  - `GameEngineTest` (10 tests): Rules, penalties, two-stage flagging, hints, modes.
  - `LevelParserTest` (4 tests): JSON pack parsing and error handling.
  - `LevelEngineTest` (5 tests): Performance evaluations and 4-tier mastery rules.
  - `ToolEngineTest` (5 tests): Tier capability scaling and upgrade cost math.
  - `StatsEngineTest` (4 tests): XP math, ranks, streak multipliers, achievements.
  - `LessonParserTest` (2 tests): Dynamic lesson JSON parsing.

---

## Module 6: Top 15 Viva Questions & Model Answers

### Q1: What is the main objective of Cyber Breach?
> **Answer:** Cyber Breach is an educational escape-room game that teaches cybersecurity fundamentals (open ports, brute-force attacks, unpatched web vulnerabilities, incident documentation) by having players act as SOC analysts analyzing live simulated network traffic and deploying defensive tools.

### Q2: Why did you separate business logic into a pure-Java `engine` package?
> **Answer:** We followed the Single Responsibility Principle and Clean Architecture principles. Decoupling game rules (`GameEngine`, `LevelEngine`, `ToolEngine`, `StatsEngine`) from Android UI classes (`Activity` / `Fragment`) allows us to run fast, automated JVM unit tests (`JUnit 4`) without needing an emulator or Android framework context.

### Q3: How do you handle network calls without freezing the UI?
> **Answer:** Network calls using `HttpURLConnection` are executed asynchronously on a background thread using a single-thread `ExecutorService`. Once complete, results are posted back to the UI thread using a `Handler` attached to `Looper.getMainLooper()`.

### Q4: What happens if the phone is offline or in Airplane Mode?
> **Answer:** We implemented a 3-tier fallback repository pattern. The app first attempts to fetch fresh data from GitHub Pages HTTPS. If offline or failing, it falls back to the private local file cache (`levels_cache.json`). If no cache exists, it falls back to bundled asset files (`assets/levels.json` / `assets/lessons/`).

### Q5: How do you prevent `NetworkOnMainThreadException`?
> **Answer:** By delegating all network connections to `LevelRepository` and `LessonRepository`, which wrap `HttpClient.get()` inside `ExecutorService.execute()`.

### Q6: How does the PDF report export work on Android 10+ vs older versions?
> **Answer:** On Android 10+ (API 29+), we use Scoped Storage via `MediaStore.Files` in `Documents/CyberBreach`, which requires zero runtime storage permissions. On Android 9 and below (API 24–28), we use legacy public storage with runtime `WRITE_EXTERNAL_STORAGE` permission, exposing the URI via `FileProvider`.

### Q7: Why do you use `FileProvider` instead of `file://` URIs?
> **Answer:** Since Android 7.0 (API 24), sharing `file://` URIs outside the app throws a `FileUriExposedException`. `FileProvider` creates secure `content://` URIs with temporary read access (`FLAG_GRANT_READ_URI_PERMISSION`).

### Q8: What database are you using, and how do you handle concurrency?
> **Answer:** We use SQLite via a thread-safe singleton `SQLiteOpenHelper` class (`DbHelper`). Transactions like tool upgrades use `db.beginTransaction()` and `db.setTransactionSuccessful()` so credits and tool tier updates either complete together or roll back.

### Q9: Why did you use `SharedPreferences` alongside SQLite?
> **Answer:** SQLite is used for structured relational data (attempts, unlocks, tool tiers, achievements). `SharedPreferences` (`PrefsManager`) is used for lightweight key-value settings (mute, haptics, reminders, streak dates) using `.apply()` for non-blocking background writes.

### Q10: How does the breach meter and audio alarm system work?
> **Answer:** In Challenge mode, mistakes and time add breach points to `GameEngine`. In `RoomActivity`, `SoundPool` plays short sfx (`click`, `alert`, `success`), and a looping `MediaPlayer` alarm dynamically scales its volume from 50% breach up to 100% breach.

### Q11: How do you prevent process death crashes when Android kills the app in the background?
> **Answer:** In `CyberBreachApp` (our `Application` class subclass), we pre-load local levels into `LevelStore` during `onCreate()`. This guarantees that if Android restores the app directly into `RoomActivity` or `ResultActivity`, `LevelStore` is already populated.

### Q12: How does the two-stage SOC response mechanic work?
> **Answer:** To prevent reckless guessing, `GameEngine` enforces an **Identify $\rightarrow$ Mitigate** workflow. Analysts must first use `Tool.PACKET_ANALYZER` on a log entry to flag the threat (`Outcome.FLAGGED`), which highlights the row in yellow. Only then can mitigation tools (`FIREWALL`, `PATCH_MANAGER`, etc.) contain the breach.

### Q13: What game modes are supported?
> **Answer:** **Learn Mode** (risk-free, 0% breach meter, no timer, free hints, no stars/credits) and **Challenge Mode** (timed, active breach meter, mistake penalties, star rating evaluation, credit rewards).

### Q14: How are notifications scheduled?
> **Answer:** Using `AlarmManager.setInexactRepeating` at 6 PM to trigger `ReminderReceiver` (a `BroadcastReceiver`). On Android 13+ (API 33+), runtime `POST_NOTIFICATIONS` permission is requested before enabling.

### Q15: How did you test your application?
> **Answer:** We built a 30-case automated unit test suite using JUnit 4 (`GameEngineTest`, `LevelParserTest`, `LevelEngineTest`, `ToolEngineTest`, `StatsEngineTest`, `LessonParserTest`) covering game rules, parsing, tier scaling, XP math, and achievements with a 100% pass rate.
