package com.pias.studytracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pias.studytracker.data.RankInfo
import com.pias.studytracker.data.RankSystem
import com.pias.studytracker.data.RankTier
import com.pias.studytracker.data.StudyDatabase
import com.pias.studytracker.data.StudyRepository
import com.pias.studytracker.data.UserPreferences
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

    // ---------- Calendar navigation / entry editing ----------

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun changeMonth(delta: Long) {
        visibleMonth.value = visibleMonth.value.plusMonths(delta)
    }

    fun saveHours(date: LocalDate, hours: Float) {
        viewModelScope.launch { repository.saveHours(date, hours) }
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
}
