package com.pias.studytracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pias.studytracker.data.RankInfo
import com.pias.studytracker.data.RankSystem
import com.pias.studytracker.data.StudyDatabase
import com.pias.studytracker.data.StudyRepository
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
        StudyDatabase.getInstance(application).studyDao()
    )

    // date -> hours, for calendar rendering
    val hoursByDate: StateFlow<Map<LocalDate, Float>> =
        repository.getAllEntries()
            .map { entries -> entries.associate { LocalDate.parse(it.date) to it.hours } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Lifetime "ultimate average" hours/day since the first-ever entry.
    val ultimateAverage: StateFlow<Float> =
        repository.getUltimateAverage()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val selectedDate = MutableStateFlow(LocalDate.now())
    val visibleMonth = MutableStateFlow(YearMonth.now())

    // Daily rank for whatever date is currently selected — recomputed whenever hours change.
    val selectedDayRank: StateFlow<RankInfo> =
        combine(selectedDate, hoursByDate) { date, map -> RankSystem.rankFor(map[date] ?: 0f) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RankSystem.rankFor(0f))

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
}
