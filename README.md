# Manzil — Your Destination, Daily Tracked

An offline-first Android app that turns one long-term goal into today's work — and keeps
running until the goal is **DONE**, not until a fixed year.

> Built for the zero-to-goal path: skills → portfolio → first paying clients → a software house.
> Every feature works without internet. The only network call in the whole app is OpenRouter,
> with your own optional key.

---

## What you get (1.1.0)

| Screen | What actually works |
|---|---|
| **Onboarding** | 4 steps (you → goal → skills/time → reminders). Finishing it creates a real starter plan: root goal, 6 sub-goals, 8 milestones, 6 tasks for today, KPI targets and 4 habits. Your own words replace the placeholder goal title. |
| **Today** | Live clock + "Day N of your plan", the same briefing the morning notification sends (Do today / Done yesterday / Still pending / Goal move / Streak), focus timer with Start-Pause-Resume-Stop that writes a real `TimeEntry` and rolls minutes into the task and its goal, today's plan with checkboxes, reorder, postpone, 3 progress rings, 12-week heatmap, streak, calendar strip, keystone habits, yesterday's leftovers (move / drop) and quick capture with live parsing preview. |
| **Tasks** | Five filters (Today / Upcoming / Overdue / Done / All) with live counts, full editor (title, notes, date, time, P1-P4, estimate, goal, repeat), recurring tasks with real RFC-5545-style expansion for the next 60 days, postpone, delete, offline. |
| **Calendar** | Month grid with load dots (green ≤3 / amber ≤6 / red 7+), week time-grid with a live "now" row, agenda for the next 14 days, tap a slot to create an event, tap a block to edit, overlap detection with a warning stripe, and one tap to turn a task into a real calendar block. |
| **Goals** | Goal tree with progress rings, sub-goals, milestones you can tick off, metric goals with a live bar, slipping detection ("slipping 12d"), pause / resume / mark done, and full CRUD. |
| **Goal Pulse** | Reverse-chronological feed of every change: created / updated / progress / metric / status, with old value struck through in red and the new value in green. Filters for this week / month / all, plus "Explain changes" (local summary, AI-polished when a key exists). |
| **KPI dashboard** | 12 targets seeded from your plan (savings 55,00,000 · capital 59,78,000 · income 50k→120k · MRR 3,50,000 · clients · team · GitHub · Upwork · CGPA · deep work), each with progress bar, sparkline and an "add reading" sheet. |
| **Time tracking** | 14-day bar chart, weekly target vs actual, per-goal rollup, full ledger, manual entries, delete, restart a timer from any entry. |
| **Journal** | Mood 1-5, wins, blockers, notes, auto summary of the day, history, and a weekly review that computes real numbers (done/planned, focus minutes, habit %, goal changes) and turns the three priorities into tasks with one tap. |
| **AI Coach** | Four actions — Plan my day, Weekly review, Explain changes, Unblock me — plus free chat. Works **without** a key (local plan from the adaptive engine). With a key it uses OpenRouter, maps every error to a human sentence, and degrades gracefully. |
| **Client hunt** | Today's free client action with an exact script, follow-up and expected result, plus a playbook for Instagram, Facebook groups, LinkedIn, X, WhatsApp and walk-ins — all one tap away from becoming a real task. |
| **Free tools** | The 12-week zero-to-goal roadmap and every free tool (dev, learning, client hunting, business). |
| **Roadmap import** | Paste markdown → `##` becomes goals, `###` sub-goals, `- [ ]` tasks, table rows with numbers become KPI targets. Preview before importing. |
| **Settings** | OpenRouter key (Keystore-encrypted, masked, test connection, remove), model browser with free models first, theme (system/light/dark) + dynamic colour, language (English / Roman Urdu output, plus a shortcut to the phone's language setting), notification toggles and times, exact-alarm status, test notification, profile, deep-work targets, JSON/CSV export, JSON import, starter roadmap, erase everything. |
| **Notifications** | Morning briefing and evening review through `AlarmManager.setExactAndAllowWhileIdle` with an automatic inexact fallback, a WorkManager safety net, boot re-registration, 5 channels, and hard anti-nag rules: max 6 a day, never 23:00-07:00, no duplicates. Every notification is stored in a log. |
| **Widget** | A home-screen widget with today's pending tasks, done count and streak. |
| **Offline** | Everything above, always. No account, no analytics, no ads, no crash reporting. |

### Deliberately not included (and why)

- **Hindi/Urdu string packs are partial.** The shell, tabs, notifications and settings are fully
  translated (English + اردو); deeper screen copy is English. The `values-ur` file is ready to extend.
- **AI streaming (SSE).** The chat is request/response with a "thinking" indicator instead of
  token-by-token streaming — fewer moving parts, same answers.
- **Device-calendar mirroring.** Apps that write to your calendar need broad permissions; Manzil
  keeps its own calendar and never touches the device one.
- **Marketplace integrations (Fiverr/Upwork).** Intentional: the playbook is direct outreach.

---

## Architecture

```
app/src/main/java/com/manzil/app/
├── core/            adaptive engine (offline planner), formatting, design system, theme,
│                    recurrence expander, markdown importer, keystore vault, JSON diff
├── data/
│   ├── local/       Room: 14 entities, 8 DAOs, converters, seed data
│   ├── prefs/       DataStore: settings + focus-timer state (timestamps, survives process death)
│   ├── remote/      OpenRouter (OkHttp + kotlinx.serialization, async, error mapped)
│   ├── repository/  ManzilRepository (plan/tasks/goals/pulse), InsightsRepository (analytics),
│   │                SearchRepository (offline ranked search)
│   └── backup/      JSON export/import + CSV (secrets never included)
├── domain/          models + use cases
├── feature/         one package per screen, each with its own ViewModel and UI state
├── notification/    channels, notifier with anti-nag rules, alarm scheduler, briefing composer,
│                    receivers, WorkManager safety net
└── widget/          RemoteViews widget + snapshot store
```

Stack: Kotlin 1.9.22 · Compose BOM 2024.06.00 · Material 3 · Hilt 2.51 + KSP · Room 2.6.1 ·
DataStore 1.0.0 · WorkManager 2.9.0 · Navigation Compose 2.7.7 · OkHttp 4.12.0 ·
kotlinx.serialization 1.6.3 · minSdk 26 · targetSdk 34 · Gradle 8.6 · AGP 8.3.2.

Design system: one accent (teal `#0F766E`), semantic success/warning/danger/info, tabular
numerals for every counter and timer, taskbar-free cards with 18dp radii, empty states with one
primary action on every list, 48dp touch targets, `contentDescription` on every icon.

---

## Build

```bash
./gradlew assembleDebug          # debug APK  → app/build/outputs/apk/debug/
./gradlew assembleRelease        # release APK → app/build/outputs/apk/release/
./gradlew testDebugUnitTest      # 40+ unit tests, all pure JVM
./gradlew lintDebug
```

The release build is signed with the Android debug keystore when `~/.android/debug.keystore`
exists, and the build script creates it automatically if it is missing — so `assembleRelease`
always produces an **installable** APK. For a Play Store upload, drop in your own keystore
(see `app/build.gradle.kts`, `signingConfigs`).

### Download the ready APK

- **Releases** → `v1.1.0` assets (`manzil-1.1.0.apk`) — install this one.
- **Actions → Build Manzil APK → latest run → Artifacts** → `manzil-debug-apk`.

---

## Privacy

All data lives in a local Room database. The API key is encrypted with an AES-256-GCM key that
never leaves the Android Keystore and is excluded from backups and exports. Nothing is
analytics-tracked, and the only outbound request the app can make is to
`https://openrouter.ai/api/v1/*` — and only when you press an AI button with your own key.
