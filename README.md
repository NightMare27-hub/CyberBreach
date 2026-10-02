# 🛡️ Cyber Breach: Security Escape Room

> *"Learn real cybersecurity by stopping a breach, not by reading about one."*

**Cyber Breach** is an educational Android escape-room game designed for beginners, CS/IT students, and security enthusiasts. You play as a junior Security Operations Center (SOC) analyst managing live incident responses across fictional organizations (hospitals, schools, online stores).

---

## 🎮 Core Game Loop
1. **Briefing**: Read a 30-second concept card explaining security vulnerabilities (open ports, brute-force attacks, unpatched services).
2. **Investigate**: Monitor live simulated network alert logs streaming into a terminal interface.
3. **Analyze**: Use Packet Analyzers and Log Filters to inspect suspicious rows (IPs, ports, payloads).
4. **Act**: Select a defense tool (Firewall, Patch Manager) and apply it to the target threat.
5. **Resolve or Fail**: Contain the breach before the Breach Meter reaches 100% or time expires.
6. **Debrief & Export**: Review performance stats, earn stars/credits, and export an official **Incident Response Report (PDF)**.

---

## ✨ Key Features
- 🛡️ **Interactive Terminal**: Monospace network alert terminal with live auto-scrolling log feeds.
- 🧰 **Toolbelt System**: Deploy Packet Analyzer, Firewall, Log Filter, and Patch Manager.
- 📊 **Dual Modes**: 
  - **Learn Mode**: Risk-free practice, no timer, free hints, no breach penalties.
  - **Challenge Mode**: Timed, breach meter active, mistake penalties, stars & credit rewards.
- 📜 **Incident Response Reports**: Export one-page A4 PDF reports to your `Documents` folder and share them via the system share sheet.
- ⚡ **Offline & Online Resilience**: Downloads new level packs over HTTPS REST/JSON, caches them locally, and falls back to bundled offline assets.
- 💾 **Persistent Progress & Upgrades**: SQLite database tracks stars, best times, attempts, unlocked levels, and credit-purchased tool upgrades.
- 🔔 **Daily Reminders**: Inexact daily challenge notifications scheduled via `AlarmManager`.

---

## 🛠️ Tech Stack & Architecture
- **Language**: Java 11 (Android SDK 24+)
- **UI Architecture**: Material 3, ViewBinding, ViewPager2, TabLayout, TableLayout, WebView, VideoView
- **Audio & Haptics**: `SoundPool` for short effects, `MediaPlayer` for volume-scaling alarm, `Vibrator`
- **Storage**: SQLite (`SQLiteOpenHelper`), `SharedPreferences`, Private External Cache
- **Networking**: `HttpURLConnection`, `ConnectivityManager`, `ExecutorService` + `Handler`
- **Export & Permissions**: `PdfDocument`, `Canvas`, `MediaStore`, `FileProvider`, Scoped Storage (API 29+), `ActivityResultContracts`
- **Testing**: JUnit 4 unit tests (`GameEngineTest`, `LevelParserTest`)

---

## 🔒 Security & Privacy Statement
Everything in **Cyber Breach** is simulated.
- No real network scanning, exploits, or hacking tools are contained in the app.
- All IP addresses use reserved RFC 5737 documentation ranges (`192.0.2.x`, `198.51.100.x`, `203.0.113.x`).
- Zero personal data is collected or transmitted.
