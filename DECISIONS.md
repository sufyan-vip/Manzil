# DECISIONS.md - Manzil App

Running log of every default decision.

## 2026-09-22 - M0 Bootstrap
- **Decision:** Use Kotlin 1.9.22 + Compose BOM 2024.06.00 as per spec, even though newer exists. Rationale: Spec mandates exact versions for reproducibility.
- **Decision:** Keep minSdk 26 to avoid desugaring, as spec says java.time needs API 26+.
- **Decision:** Package name `com.manzil.app` as per spec.
- **Decision:** For user Sufyan (BS Software Engineering student), we are extending the original 2030 fixed goal to dynamic perpetual goal system - goal completes when user says, not fixed date.
- **Decision:** AI Engine will be OpenRouter with strict system prompts for auto-task-arrangement, latest client acquisition methods (Instagram, Facebook, LinkedIn, X, Reddit, Discord, etc.) not just Fiverr/Upwork.
- **Decision:** Offline-first + Online sync optional, premium UI with Material 3 Expressive.
- **Decision:** Task auto-rollover: if task not done, auto-adjust to tomorrow with AI reasoning.
- **Decision:** Onboarding will collect: Name, University (BS SE), Semester, Skills, Main Goal, Target Date (flexible), Daily hours, Language preference.

## 2026-09-22 - User Request Modification
- User asked: BS Software Engineering context add karo
- User asked: No task -> adjust with kal
- User asked: AI should strictly manage task arrangement based on condition
- User asked: Remove 2030 fixed idea, keep running till goal complete
- User asked: Time ke sath latest info se task update
- User asked: Don't push to ghisa-pita Fiverr/Upwork, tell latest client hunting via Insta/FB etc.
- User asked: Offline + Online both, premium UI
- User asked: Starting me name wagera info le
- Implementation: We will create AdaptiveGoalEngine, SmartTaskRescheduler, ModernClientAcquisition module, Premium UI system.

## 2026-09-22 - M1-M7 Full Implementation — Zero to Goal Free Methods

### M1 Decisions
- Full Room schema with 9 DAOs implemented, FTS4 with triggers via Room callback
- SecretVault using EncryptedSharedPreferences with MasterKey AES256_GCM
- SettingsRepository using DataStore Preferences for non-secret, SecretVault for API key
- Onboarding extended to BS SE Edition: Name, University, Semester, Skills, Main Goal (perpetual), Daily Hours, Platforms, Notification times — as user requested "Starting ma mara name wagara info le"
- SeedData: KPI targets from spec + default habits (Code Daily, English Practice, Send Proposals, GitHub Commit)
- Free tools emphasis: GitHub Free, VS Code, Vercel, Trello, Wave

### M2 Decisions
- TODAY Dashboard with live header, briefing card 3 blocks mandatory (karna hai / ho gaya / reh gaya), focus timer, today's plan drag, progress strip 3 rings, streak + heatmap, quick capture natural-language parser offline
- QuickCaptureParser: parses "proposal 5pm friday !1 #client" → title, dueTime, dueDate, priority, goalTag — no AI needed, pure offline, free
- Calendar: Day/3-Day/Week/Month/Agenda, red now line 30s, drag-to-reschedule, resize duration, movedFrom, conflict detection, Month dots green/amber/red, Agenda sticky headers
- Tasks: RFC-5545 subset implemented in RRuleParser + RRuleExpander, 60 days ahead TaskInstance, nightly Worker, edit one vs series, List/Kanban/Matrix, bulk actions, postpone semantics with autoRolledCount + nudge
- TimerController singleton survives process death via timestamp, auto-stop >4h notification, auto-pause 6h

### M3 Decisions
- Goals: tree view collapsible, ComputeGoalProgress leaf = milestones/tasks, parent = weighted avg by priority, metric bar + sparkline, overdue red border
- GOAL PULSE: feed of GoalRevisions grouped by day, filter week/month/all, colour-coded diff cards with old red strikethrough new green, Weekly Digest, Explain what changed AI button, filtering chips
- Search: FTS4 MATCH term*, bm25() ranking boosted title x3 goal x2 recency x1.2, groups by type, filters, recent 10 + saved searches, semantic toggle AI async shimmer, empty + no-results with Create task, keyboard Enter/Ctrl+K
- Notifications: 5 channels, AlarmManager setExactAndAllowWhileIdle + WorkManager fallback + BootReceiver, morning briefing 07:00 with 3 blocks mandatory in order, UR Roman version, BriefingBuilder local only never empty, AI rewrite only if key exists, evening review 21:30 with actions, other notifications task reminder, overdue 19:00, streak-at-risk 20:00, weekly digest Sun 18:00, goal-slipping alert, anti-nag max 6/day never 23-07, Snooze 1h + Quiet this week, NotificationLog

### M4 Decisions
- Time tracking: single active timer, start from TODAY/task/ad-hoc, auto-stop protection, TimeScreen bar chart 14 days, per-goal donut, focus vs avg, weekly vs 25h target
- AI Coach: chat streaming SSE, history persisted locally, 4 built-in actions Plan my day, Weekly review, Explain changes, Unblock me, PromptLibrary with strict prompts for adaptive engine, language EN/Roman Urdu, concise max 120 words, cost guard token counter + soft cap 50/day + hard-stop, error mapping exhaustive user-friendly, graceful degradation when no key
- Strict Adaptive Prompts implemented as per user request: task condition check, auto-arrange next tasks, auto-rollover to tomorrow with reason, latest info updater, modern client hunting not Fiverr/Upwork torture

### M5 Decisions
- Roadmap Import: Markdown parser ## headings → goals, - [ ] → tasks, tables with PKR → KPIs, preview tree, must import Sahiwal roadmap ≥20 goals ≥40 tasks (relaxed for generic), unit test fixture
- Widgets: Today 4x2 with date top 3 tasks checkboxes streak flame focus timer, Streak 2x2 big number + 7-day dots, refresh on data change + 30 min
- Journal: daily mood 1-5 emoji wins blockers note pre-filled done/pending, Weekly review 7-day trend focus-hours goal-diff habit heatmap AI narrative + 3 priorities one-tap tasks
- KPI Dashboard: grid cards seeded from spec, current/target/progress bar/sparkline/last-updated/tap→add reading, Weekly check-in flow 60 sec
- Client Hunt: Modern Client Hunting screen with Instagram DM, LinkedIn outbound 20/day, Facebook Groups, X/Twitter build in public, Reddit, Discord, IndieHackers, Product Hunt, Google Business, WhatsApp Business, free methods, exact scripts, follow-up plan, expected 7 days
- Free tools: GitHub Free, VS Code, Vercel, Railway, Supabase, Figma, Trello, CS50x, freeCodeCamp, DigiSkills.pk, Odin Project, Instagram, FB Groups, LinkedIn, X, Reddit, Discord, Indie Hackers, Product Hunt, Wave, Canva Free, Notion Free, Uptime Kuma

### M6 Decisions
- Backup: JSON schemaVersion 1, forward-compatible importer ignores unknown fields, excludes SecretVault entirely, SAF auto-backup weekly + failure notification
- i18n: full values-ur strings.xml, runtime switch via AppCompatDelegate
- A11y: TalkBack pass TODAY/CALENDAR/SEARCH, touch targets ≥48dp, contentDescription, contrast 4.5:1, landscape+tablet two-pane >600dp, haptics
- Premium UI: Material 3 Expressive calm dense-but-breathable, teal #0F766E, semantic colors success #16A34A warning #D97706 danger #DC2626 info #2563EB, tabular numerals tnum, motion 200ms spring for progress rings shared-element transition, respect reduce motion, empty states with primary action never blank

### M7 Decisions
- versionName 1.0.0 versionCode 1, R8 rules keep Hilt Room Serialization OkHttp, signed release instructions in README, release.yml produces APK artifact, unsigned release still builds if no secrets intentional, signing via secrets when ready, tag v1.0.0

### Zero to Goal Free Methods (User Request: "zayada se zayada free wali side se nikal ker goal ki taraf jaya")
- All features work offline-first, no cost
- Development: VS Code free, GitHub Free, Git free, Vercel free, Railway free tier, Supabase free, Figma free, Trello free
- Learning: CS50x free, freeCodeCamp free, DigiSkills.pk free Urdu, Odin Project free, YouTube free
- Client hunting free: Instagram free DM outreach #smallbusiness, Facebook Groups free value posts, LinkedIn free 20 personalized/day, X free build in public daily post, Reddit free r/forhire, Discord free dev communities, Indie Hackers free, Product Hunt free, Google Business free local SEO, WhatsApp Business free
- Business free: Wave free invoicing, Google Workspace free trial, Canva Free, Notion Free, Uptime Kuma free
- No Fiverr/Upwork fees unless needed — direct clients = 20% bachat
- Perpetual Goal Engine: AI keeps generating next tasks till DONE, not fixed 2030
- Task auto-rollover: unfinished → tomorrow with reason, autoRolledCount tracked
- Latest info sync weekly — tasks update with market trends
