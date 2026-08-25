# Study Tracker

Native Android app (Kotlin + Jetpack Compose + Room) — log daily study hours,
see them on a calendar, track a lifetime average, and get a daily rank that
levels up every 0.5h studied that day.

## Round 2 changes (this update)
- Future dates are locked on the calendar (greyed out, untappable) and the Save button rejects them too
- "Built by Md. Samiul Islam Pias, CSE, Netrokona University" footer in Settings
- Daily rank-up popup fires the instant today's hours cross into a new tier
- Lifetime milestone popup every 50 total hours (50, 100, 150, ...)
- Daily reminder notification at a time you set in Settings, skipped automatically if today's already logged. Requires the notification permission prompt on Android 13+ (asked once, on first open after this update).
- Weekly summary card: this week vs. last week, best day this week
- Trend chart: 7-day / 30-day toggle bar graph
- Monthly total shown next to the month name on the calendar
- Current + longest streak card (a day counts if hours > 0)

**Note on reminders surviving reboot:** the app registers a boot receiver to
re-arm your daily reminder after the phone restarts, so you shouldn't need to
re-set it. The reminder itself is "inexact" (Android may fire it a few
minutes off schedule to save battery) — deliberate trade-off to avoid asking
for the more sensitive exact-alarm permission for a non-critical nudge.

## Round 1 changes
- Dark HUD theme with slow-drifting glowing teal/green blobs in the background
- Rank tiers are now user-editable in Settings (add/edit/delete). They're
  always evaluated sorted by hours ascending — to "reorder" a tier, just
  change its hour value. "Reset to default" restores the original ladder.
- Calendar heatmap now scales green intensity from 0h (no fill) to 10h+
  (brightest)
- Animated splash quote on every open
- First-launch name prompt; name shows in the top bar, editable later in
  Settings
- Backup: Export/Import as a JSON file (Settings) — the one thing that
  protects your data if you ever uninstall the app
- Delete a past entry directly from the entry card when a date already has
  logged hours

**Note on your existing data:** the database schema changed (added a table
for rank tiers), so this update ships a migration rather than wiping your
`study_tracker.db` — your previously logged hours should carry over. Still,
this is a good moment to try Export once the update installs, just in case.

## How data is stored
All entries live in a local Room (SQLite) database on the device —
`study_tracker.db`. Nothing leaves the phone; no internet permission is
requested.

## How the two stats differ
- **Today's Rank** — recalculated from *today's* hours only. Every 0.5h
  studied levels you up, tiers named Script Kiddie → Recon Rookie → ... →
  Grandmaster Hacker. Resets fresh each day. Edit `RankSystem.kt` to rename
  tiers or change the hour thresholds.
- **Ultimate Average** — total hours ÷ days elapsed since your very first
  logged entry. Lifetime stat, unrelated to the daily rank.

## Building the APK — no Android Studio required

1. Push this folder to a new GitHub repo (or `git init` here and push).
2. GitHub Actions (`.github/workflows/build-apk.yml`) builds automatically
   on every push to `main`. Watch it under the repo's **Actions** tab.
3. When it finishes, open the workflow run → **Artifacts** →
   download `study-tracker-debug-apk`. It's a zip containing `app-debug.apk`.
4. Transfer the APK to your phone (Google Drive, USB, Telegram-to-self,
   whatever's easiest) and tap to install. You'll need to allow
   "install unknown apps" for whichever app you use to open it — Android
   will prompt you the first time.

No local build step, no Android Studio, no laptop load — GitHub's servers
do the compiling.

### If you want to edit the code first
You don't need Android Studio for that either — the Kotlin files are plain
text. GitHub's own web editor (press `.` on the repo page) or any text
editor works. Android Studio only becomes genuinely useful once you want
a visual layout designer or on-device debugging — not required for this app.

## Project structure
```
app/src/main/java/com/pias/studytracker/
  data/
    StudyEntry.kt       Room entity (one row per date)
    StudyDao.kt         queries
    StudyDatabase.kt    Room database singleton
    StudyRepository.kt  ultimate-average calculation lives here
    RankSystem.kt        daily rank tiers — edit thresholds/names here
  ui/
    StudyViewModel.kt    app state
    CalendarView.kt       month grid, color-coded by hours
    Theme.kt              colors
  MainActivity.kt          screen layout
```
