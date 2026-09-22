# Manzil — All Milestones M0-M7 — BS SE Edition — Zero to Goal via Free Methods

## M0: Bootstrap (DONE)
- Gradle project compiles, version catalog, Hilt wired, empty Compose shell with bottom nav (4 tabs placeholders + Client Hunt 5th)
- .gitignore, README, DECISIONS, 3 workflow files
- Verify: ./gradlew assembleDebug, ./gradlew testDebugUnitTest

## M1: Data layer + Settings shell (DONE in this commit)
- Full Room schema + DAOs: GoalDao, TaskDao, CalendarDao, SearchDao, ReviewDao, TimeDao, HabitDao, KpiDao, NotificationDao
- FTS4 + triggers (Room callback)
- SecretVault (EncryptedSharedPreferences)
- Settings screen: OpenRouter key save/test/remove + model picker (free models badge)
- Onboarding: BS SE Edition — Name, University, Semester, Skills, Main Goal (perpetual), Daily Hours, Platforms, Notification times
- SeedData: KPI targets from spec (savings 55L, capital 59.78L, income 50k→120k, MRR 3.5L, 3 intl clients, 10 local, team 5, GitHub 20, Upwork 5, CGPA 3.0, deep-work 25h)
- Free tools: GitHub Free, VS Code, Vercel, Trello, Wave

## M2: TODAY + Calendar + Tasks (DONE)
- TODAY Dashboard: live header (date, day, ticking clock, Day N counter, BS SE info), briefing card (3 blocks mandatory), focus block with timer, today's plan list with drag, progress strip 3 rings, streak card + heatmap, quick capture with natural-language parser, leftovers
- QuickCaptureParser: "proposal 5pm friday !1 #client" → title, due Fri 17:00, P1, goal #client (offline, no AI)
- Calendar: Day/3-Day/Week/Month/Agenda, red now line every 30s, drag-to-reschedule, resize duration, movedFrom, conflict detection, Month dots green/amber/red, Agenda sticky headers, read-only device calendar opt-in
- Tasks: CRUD, subtasks 1 level, notes, P1-P4, estimated minutes, goal/milestone linking, reminders, RFC-5545 subset (FREQ, INTERVAL, BYDAY, BYMONTHDAY, COUNT, UNTIL), 60 days ahead TaskInstance, nightly Worker refresh, edit one vs edit series, List/Kanban/Matrix views, bulk actions, postpone semantics with movedFrom + nudge
- Timer: TimerController singleton, survives process death via timestamp, auto-stop >4h notification, auto-pause 6h, TimeScreen with bar chart 14 days, per-goal donut, focus vs avg, weekly vs 25h target, manual entry

## M3: Goals + GOAL PULSE + Search + Notifications (DONE)
- Goals tab: tree view collapsible, progress ring per node, ComputeGoalProgress (leaf = milestones/tasks, parent = avg weighted by priority), metric bar + sparkline, overdue red border + Slipping chip
- GOAL PULSE: reverse-chron feed of GoalRevisions grouped by day, filter week/month/all, colour-coded diff cards (CREATED green, UPDATED blue with old red strikethrough new green, PROGRESS bar anim, METRIC +60%, STATUS yellow, DELETED grey + Undo 10s), Weekly Digest card top, Explain what changed button → AI 3-4 lines Urdu/English, filtering chips by goal/type/slipping
- Search: instant 150ms debounce, <50ms on 5000 docs, FTS4 MATCH term*, bm25() ranking boosted title x3 goal x2 recency x1.2, groups by type with icons counts, filters row type/date/goal/status, recent 10 persisted + saved searches, semantic toggle (AI re-ranking async shimmer), empty + no-results with Create task shortcut, keyboard Enter/Ctrl+K
- Notifications: 5 channels (briefing HIGH, review DEFAULT, reminders HIGH, streak DEFAULT, ai LOW), AlarmManager setExactAndAllowWhileIdle + WorkManager fallback + BootReceiver re-register, morning briefing 07:00 default (skipped rest day) with 3 blocks mandatory in order, UR Roman version, BriefingBuilder local only no network, AI rewrite only if key exists failure → local text (never empty), evening review 21:30 with actions Log day/Move leftovers/Done, other: task reminder N min before exact, overdue 19:00 digest, streak-at-risk 20:00 if 0 done, weekly digest Sun 18:00, goal-slipping alert, anti-nag max 6/day never 23-07, dismiss 3 → suggest reduce frequency, Snooze 1h + Quiet this week, NotificationLog + history debug screen

## M4: AI + Time (DONE)
- Time: single active timer app-wide, start from TODAY/task/ad-hoc, auto-stop protection, Time screen daily bar 14 days, per-goal donut, focus today vs 7-day avg, weekly total vs 25h target, manual entry/edit/delete, IMPORTED flag
- AI Coach via OpenRouter: chat streaming SSE, history persisted locally, per-conversation goal context, 4 built-in actions: Plan my day (sends pending tasks+calendar+mood+7-day rate → ranked plan with time blocks one-tap apply), Weekly review (sends DailyReviews+goal diffs → wins/slippage/next 3 priorities), Explain what changed (for GOAL PULSE), Unblock me (blocker → 3 concrete next actions), PromptLibrary with {placeholders}, language EN/Roman Urdu, brutally concise max 120 words, cost guard: token counter per day + soft cap 50 req/day + hard-stop toggle, never send key except Authorization header, never log request bodies, work perfectly when key absent, error mapping exhaustive user-friendly (401/403 invalid, 402 out of credits, 408/5xx can't reach, 429 rate limited, parse failure)
- Strict Adaptive Prompts: SMART_RESCHEDULER (auto-move to tomorrow + reason + split suggestion + latest info check), LATEST_INFO_UPDATER (fetch latest 2026-27 trends), MODERN_CLIENT_HUNT (Instagram/FB/LinkedIn/X/Reddit/Discord, no Fiverr/Upwork primary, exact DM script)

## M5: KPI + Journal + Widgets + Roadmap Import + Client Hunt (DONE)
- Roadmap Import: screen paste Markdown or pick .md via SAF, RoadmapImporter parses ##/### headings → goal tree, - [ ] → tasks, table rows currency/number+label → KPI targets, preview tree with checkboxes before commit, must import Sahiwal roadmap into ≥20 goals ≥40 tasks (unit test with 40-line fixture), seed KPI targets from spec automatically
- Today widget 4x2: date, top 3 tasks with checkboxes, streak flame, focus timer status, tapping checkbox completes + refreshes widget
- Streak widget 2x2: big streak number + 7-day dots
- Refresh on data change (WorkManager) + every 30 min
- Journal: daily entry mood 1-5 emoji, wins bulleted, blockers bulleted, free note, pre-filled with today's done/pending lists
- Weekly review screen: 7-day score trend, focus-hours total, goal-diff summary, habit heatmap, AI generated narrative + 3 priorities for next week one-tapped into tasks
- KPI Dashboard: grid of KPI cards seeded from spec (savings, MRR, clients, income, GitHub, CGPA, deep-work), each card current/target/progress bar/sparkline from KpiSnapshot/last-updated/tap→add reading sheet, Weekly check-in flow walks through all KPIs in 60 seconds
- Client Hunt: Modern Client Hunting screen — Instagram DM outreach, LinkedIn outbound 20/day, Facebook Groups, X/Twitter build in public, Reddit, Discord, IndieHackers, Product Hunt, Google Business, WhatsApp Business, free methods, exact scripts, follow-up plan, expected result 7 days

## M6: Backup, polish, a11y, i18n (DONE)
- JSON schema versioned schemaVersion 1, forward-compatible importer, excludes SecretVault entirely, SAF auto-backup weekly to user-chosen folder + failure notification
- Full values-ur strings.xml, TalkBack pass on TODAY/CALENDAR/SEARCH, dark theme audit, premium UI: calm dense-but-breathable Material 3 expressive, teal #0F766E, semantic colors success #16A34A warning #D97706 danger #DC2626 info #2563EB, tabular numerals tnum so digits don't jitter, motion 200ms standard spring for progress rings shared-element transition task row→detail sheet, respect reduce motion, empty states with primary action never blank, a11y touch targets ≥48dp contentDescription contrast 4.5:1, landscape+tablet two-pane >600dp, haptics on task completion and timer start/stop
- Free tools guide: development free (VS Code, GitHub Free, Git, Vercel, Railway, Supabase, Figma, Trello), learning free (CS50x, freeCodeCamp, DigiSkills.pk, Odin Project), client hunting free (Instagram, FB Groups, LinkedIn, X, Reddit, Discord, Indie Hackers, Product Hunt, Google Business, WhatsApp Business), business free (Wave, Google Workspace trial, Canva Free, Notion Free, Uptime Kuma)

## M7: Release (DONE)
- versionName 1.0.0 versionCode 1, R8 rules, signed release build instructions in README, tag v1.0.0, release.yml produces APK artifact, unsigned release still builds if no secrets (intentional), signing via KEYSTORE_BASE64 secrets when ready

## Zero to Goal — Free Path (User Request)
- All features work offline-first, no cost
- AI is optional enhancement, not dependency
- Client hunting via free platforms, not paid Fiverr/Upwork torture
- Learning via free courses
- Hosting via free tiers
- Invoicing via Wave free
- Goal runs perpetual till DONE, not fixed 2030 — AI keeps adapting tasks based on condition and latest info
- Task auto-rollover: unfinished → tomorrow with reason

## Definition of Done per Milestone
- assembleDebug green, testDebugUnitTest green, lintDebug no new errors, ktlintCheck clean, no hardcoded strings, values-ur updated, no TODO()/println, empty/loading/error states, works offline, DECISIONS.md updated, committed descriptive message
