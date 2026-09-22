# Manzil — Your Destination, Daily Tracked

> **BS Software Engineering Edition** — Built for a BSSE student who wants to turn his multi-year life goal into a live daily system.

Manzil is an offline-first Android app that turns your long-term goal into daily actions. It auto-adjusts tasks, learns from your progress, and uses AI to keep you on track until your goal is DONE — not just till 2030.

### What's New in This Edition (User Requested)

1.  **BS Software Engineering Context:** Onboarding now asks for Name, University, Semester, Current Skills, Main Goal (flexible, not fixed 2030), Daily Deep Work Hours.
2.  **Smart Task Auto-Rollover:** If no task is done today, AI automatically adjusts it with tomorrow + explains why.
3.  **Strict AI Task Manager:** AI monitors task condition (TODO, BLOCKED, OVERDUE) and re-arranges next tasks to push you toward goal. Prompt is locked for this purpose.
4.  **Perpetual Goal Engine:** No fixed 2030 deadline. Goal runs until YOU mark it DONE. Time-based adaptive updates.
5.  **Latest Info Updater:** AI periodically fetches latest tech trends / client acquisition methods and updates tasks.
6.  **Modern Client Hunting (No Fiverr/Upwork torture):** Teaches Instagram DM outreach, Facebook Groups, LinkedIn outbound, X/Twitter, Reddit, Discord, IndieHackers, ProductHunt, etc.
7.  **Offline + Online:** 100% offline usable, online for AI and latest info sync.
8.  **Premium UI:** Material 3 Expressive, dark/light, animations, haptics, tabular numerals.

### Core Features (From Original Spec)

- **TODAY Dashboard:** Live clock, briefing, focus timer, today's plan, progress rings, streak, quick capture, leftovers
- **Calendar:** Day/3-Day/Week/Month/Agenda, red now line, drag-to-reschedule, conflict detection
- **Goals + GOAL PULSE:** Goal tree, progress, metric tracking, colour-coded diff feed (what changed)
- **Search:** FTS4 instant search <50ms, 150ms debounce, ranked
- **Tasks & Recurrence:** RFC-5545 subset, 60-day expansion
- **Time Tracking:** Single timer, auto-stop protection
- **AI Coach via OpenRouter:** Chat + Plan my day + Weekly review + Explain changes + Unblock me
- **Notifications:** Morning briefing (karna hai / ho gaya / reh gaya), evening review, reminders, streak alerts
- **Settings:** Encrypted API key, notifications, theme, language EN/UR, backup/restore
- **Widgets:** Glance Today + Streak
- **Journal & KPI:** Mood, wins, blockers, business dashboard

### Tech Stack

Kotlin 1.9.22, Compose BOM 2024.06.00, Gradle 8.6, AGP 8.3.2, Hilt 2.51 + KSP, Room 2.6.1 + FTS4, DataStore + Security Crypto, WorkManager + AlarmManager, Navigation Compose 2.7.7, OkHttp 4.12.0 + Serialization, Vico charts, minSdk 26, targetSdk 34.

### Project Structure

See `APP_BUILD_PROMPT.md` Section 6 for exact structure.

### Build

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

### GitHub Actions

- `build-apk.yml` — Assemble & Unit Test on push to main/arena/**
- `ci.yml` — Lint + Tests
- `release.yml` — Signed release on tag v*

### Privacy

All data stays in local Room. Only network call is to `https://openrouter.ai/api/v1/*` with your own key. No analytics, no ads.

### Roadmap Import

Paste your Sahiwal roadmap Markdown and it becomes 20+ goals and 40+ tasks.

### License

Private — for personal use.
