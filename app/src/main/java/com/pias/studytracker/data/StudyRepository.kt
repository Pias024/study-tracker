package com.pias.studytracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StudyRepository(private val dao: StudyDao) {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun saveHours(date: LocalDate, hours: Float) {
        dao.upsert(StudyEntry(date.format(formatter), hours))
    }

    suspend fun deleteDate(date: LocalDate) {
        dao.delete(date.format(formatter))
    }

    suspend fun getHoursFor(date: LocalDate): Float =
        dao.getForDate(date.format(formatter))?.hours ?: 0f

    fun getAllEntries(): Flow<List<StudyEntry>> = dao.getAll()

    /** Daily rank: recalculated purely from the hours logged on [date]. */
    suspend fun getDailyRank(date: LocalDate): RankInfo =
        RankSystem.rankFor(getHoursFor(date))

    /**
     * "Ultimate average": total hours logged, divided by the number of
     * calendar days elapsed since the very first entry (inclusive of today).
     * This is independent of the daily rank system.
     */
    fun getUltimateAverage(): Flow<Float> =
        combine(dao.getTotalHours(), dao.getFirstDate()) { total, firstDateStr ->
            if (firstDateStr == null) {
                0f
            } else {
                val firstDate = LocalDate.parse(firstDateStr, formatter)
                val daysElapsed = (LocalDate.now().toEpochDay() - firstDate.toEpochDay() + 1)
                    .coerceAtLeast(1)
                total / daysElapsed
            }
        }
}
