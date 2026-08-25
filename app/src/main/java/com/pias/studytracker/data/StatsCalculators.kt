package com.pias.studytracker.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

data class StreakInfo(val current: Int, val longest: Int)

data class WeeklySummary(
    val thisWeekTotal: Float,
    val lastWeekTotal: Float,
    val bestDay: DayOfWeek?,
    val bestDayHours: Float
)

/** A day "counts" toward a streak if hours > 0. Pure functions, easy to reason about/test. */
object StatsCalculators {

    fun streaks(hoursByDate: Map<LocalDate, Float>): StreakInfo {
        val studiedDates = hoursByDate.filterValues { it > 0f }.keys
        if (studiedDates.isEmpty()) return StreakInfo(0, 0)

        // Longest: scan every date that has an entry, count run length ending at each date
        // where the previous day also has an entry.
        var longest = 0
        var runLength = 0
        var prevDate: LocalDate? = null
        for (date in studiedDates.sorted()) {
            runLength = if (prevDate != null && date == prevDate.plusDays(1)) runLength + 1 else 1
            longest = maxOf(longest, runLength)
            prevDate = date
        }

        // Current: walk backward from today (or yesterday, if today isn't logged yet)
        // for as long as consecutive days have entries.
        var cursor = LocalDate.now()
        if (cursor !in studiedDates) cursor = cursor.minusDays(1)
        var current = 0
        while (cursor in studiedDates) {
            current++
            cursor = cursor.minusDays(1)
        }

        return StreakInfo(current, longest)
    }

    fun weeklySummary(hoursByDate: Map<LocalDate, Float>): WeeklySummary {
        val today = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfThisWeek = today.with(weekFields.dayOfWeek(), 1L)
        val startOfLastWeek = startOfThisWeek.minusWeeks(1)

        val thisWeekTotal = hoursByDate.entries
            .filter { !it.key.isBefore(startOfThisWeek) && !it.key.isAfter(today) }
            .sumOf { it.value.toDouble() }.toFloat()

        val lastWeekTotal = hoursByDate.entries
            .filter { !it.key.isBefore(startOfLastWeek) && it.key.isBefore(startOfThisWeek) }
            .sumOf { it.value.toDouble() }.toFloat()

        val bestEntry = hoursByDate.entries
            .filter { !it.key.isBefore(startOfThisWeek) && !it.key.isAfter(today) }
            .maxByOrNull { it.value }

        return WeeklySummary(
            thisWeekTotal = thisWeekTotal,
            lastWeekTotal = lastWeekTotal,
            bestDay = bestEntry?.key?.dayOfWeek,
            bestDayHours = bestEntry?.value ?: 0f
        )
    }

    /** Last [days] days including today, oldest first — for the trend bar chart. */
    fun trendSeries(hoursByDate: Map<LocalDate, Float>, days: Int): List<Pair<LocalDate, Float>> {
        val today = LocalDate.now()
        return (days - 1 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            date to (hoursByDate[date] ?: 0f)
        }
    }

    /** Which multiple-of-[step] milestone (if any) was newly crossed going from prevTotal to newTotal. */
    fun crossedMilestone(prevTotal: Float, newTotal: Float, step: Float = 50f): Float? {
        val prevLevel = (prevTotal / step).toInt()
        val newLevel = (newTotal / step).toInt()
        return if (newLevel > prevLevel) newLevel * step else null
    }
}
