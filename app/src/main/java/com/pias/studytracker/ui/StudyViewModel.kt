package com.pias.studytracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pias.studytracker.data.RankInfo
import com.pias.studytracker.data.RankSystem
import com.pias.studytracker.data.RankTier
import com.pias.studytracker.data.StatsCalculators
import com.pias.studytracker.data.StreakInfo
import com.pias.studytracker.data.StudyDatabase
import com.pias.studytracker.data.StudyRepository
import com.pias.studytracker.data.UserPreferences
import com.pias.studytracker.data.WeeklySummary
import com.pias.studytracker.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(
        StudyDatabase.getInstance(application).studyDao(),
        StudyDatabase.getInstance(application).rankTierDao()
    )
    private val userPreferences = UserPreferences(application)

    init {
        viewModelScope.launch { repository.ensureRankTiersSeeded() }
    }

    // ---------- Core data ----------

    val hoursByDate: StateFlow<Map<LocalDate, Float>> =
        repository.getAllEntries()
            .map { entries -> entries.associate { LocalDate.parse(it.date) to it.hours } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val ultimateAverage: StateFlow<Float> =
        repository.getUltimateAverage()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val selectedDate = MutableStateFlow(LocalDate.now())
    val visibleMonth = MutableStateFlow(YearMonth.now())

    // ---------- Rank tiers (user-editable) ----------

    val rankTiers: StateFlow<List<RankTier>> =
        repository.getRankTiers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun tierPairs(tiers: List<RankTier>): List<Pair<Float, String>> =
        tiers.map { it.hours to it.name }

    val selectedDayRank: StateFlow<RankInfo> =
        combine(selectedDate, hoursByDate, rankTiers) { date, map, tiers ->
            RankSystem.rankFor(map[date] ?: 0f, tierPairs(tiers).ifEmpty { RankSystem.DEFAULT_TIERS })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RankSystem.rankFor(0f))

    val averageRank: StateFlow<RankInfo> =
        combine(ultimateAverage, rankTiers) { avg, tiers ->
            RankSystem.rankFor(avg, tierPairs(tiers).ifEmpty { RankSystem.DEFAULT_TIERS })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RankSystem.rankFor(0f))

    fun upsertRankTier(tier: RankTier) {
        viewModelScope.launch { repository.upsertRankTier(tier) }
    }

    fun deleteRankTier(tier: RankTier) {
        viewModelScope.launch { repository.deleteRankTier(tier) }
    }

    fun resetRankTiersToDefault() {
        viewModelScope.launch { repository.resetRankTiersToDefault() }
    }

    // ---------- Name (first-launch prompt) ----------

    val userName: StateFlow<String?> =
        userPreferences.userName
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setUserName(name: String) {
        viewModelScope.launch { userPreferences.setUserName(name) }
    }

    // ---------- Streaks / weekly summary / trend ----------

    val streaks: StateFlow<StreakInfo> =
        hoursByDate.map { StatsCalculators.streaks(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakInfo(0, 0))

    val weeklySummary: StateFlow<WeeklySummary> =
        hoursByDate.map { StatsCalculators.weeklySummary(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklySummary(0f, 0f, null, 0f))

    val trendDays = MutableStateFlow(7)

    val trendSeries: StateFlow<List<Pair<LocalDate, Float>>> =
        combine(hoursByDate, trendDays) { map, days -> StatsCalculators.trendSeries(map, days) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTrendDays(days: Int) {
        trendDays.value = days
    }

    // ---------- Calendar navigation ----------

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun changeMonth(delta: Long) {
        visibleMonth.value = visibleMonth.value.plusMonths(delta)
    }

    // ---------- Entry save/delete, with celebration triggers ----------

    private val _celebration = MutableStateFlow<CelebrationEvent?>(null)
    val celebration: StateFlow<CelebrationEvent?> = _celebration

    fun consumeCelebration() {
        _celebration.value = null
    }

    fun saveHours(date: LocalDate, hours: Float) {
        viewModelScope.launch {
            val tiers = tierPairs(rankTiers.value).ifEmpty { RankSystem.DEFAULT_TIERS }
            val prevTotal = hoursByDate.value.values.sum()
            val prevForDate = hoursByDate.value[date] ?: 0f
            val newTotal = prevTotal - prevForDate + hours

            repository.saveHours(date, hours)

            // Milestone takes priority if both would fire in the same save.
            val milestone = StatsCalculators.crossedMilestone(prevTotal, newTotal)
            if (milestone != null) {
                _celebration.value = CelebrationEvent.Milestone(milestone)
                return@launch
            }

            if (date == LocalDate.now()) {
                val prevRank = RankSystem.rankFor(prevForDate, tiers)
                val newRank = RankSystem.rankFor(hours, tiers)
                if (newRank.tierName != prevRank.tierName && hours > 0f) {
                    _celebration.value = CelebrationEvent.DailyRankUp(newRank.tierName)
                }
            }
        }
    }

    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch { repository.deleteDate(date) }
    }

    // ---------- Backup: export / import ----------

    suspend fun exportBackupJson(): String = repository.exportBackupJson(userName.value)

    suspend fun importBackupJson(json: String) {
        val restoredName = repository.importBackupJson(json)
        if (restoredName != null) userPreferences.setUserName(restoredName)
    }

    // ---------- Reminder ----------

    val reminderTime: StateFlow<Pair<Int, Int>?> =
        userPreferences.reminderTime
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setReminder(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferences.setReminderTime(hour, minute)
            ReminderScheduler.schedule(getApplication(), hour, minute)
        }
    }

    fun clearReminder() {
        viewModelScope.launch {
            userPreferences.clearReminderTime()
            ReminderScheduler.cancel(getApplication())
        }
    }
}
