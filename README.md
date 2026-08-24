# Study Tracker

Native Android app (Kotlin + Jetpack Compose + Room) — log daily study hours,
see them on a calendar, track a lifetime average, and get a daily rank that
levels up every 0.5h studied that day.

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
