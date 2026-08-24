package com.pias.studytracker.data

import kotlin.math.floor

data class RankInfo(
    val level: Int,          // how many 0.5h blocks completed TODAY (1 per block)
    val tierName: String,
    val hoursIntoTier: Float,
    val tierSpanHours: Float,
    val progressToNextLevel: Float, // 0f..1f within the current 0.5h block
    val hoursToNextLevel: Float
)

/**
 * Bug-bounty themed rank tiers — this is a DAILY rank, recalculated fresh from
 * that day's hours only (not cumulative across the app's lifetime). Every 0.5h
 * studied that day is one "level"; tiers group levels into named ranks.
 * The separate lifetime "ultimate average" stat lives in StudyRepository, not here.
 * Tune the TIERS list any time — nothing else depends on the values.
 */
object RankSystem {

    private const val HOURS_PER_LEVEL = 0.5f

    // Scaled for a single day (realistic range ~0-12h), not a lifetime total.
    private val TIERS = listOf(
        0f to "Script Kiddie",
        1f to "Recon Rookie",
        2f to "Bug Hunter",
        3f to "Vulnerability Analyst",
        4f to "Exploit Developer",
        5f to "Red Teamer",
        6f to "Security Researcher",
        7f to "Elite Hunter",
        8.5f to "Bounty Legend",
        10f to "Grandmaster Hacker"
    )

    fun rankFor(totalHours: Float): RankInfo {
        val safeHours = totalHours.coerceAtLeast(0f)
        val level = floor(safeHours / HOURS_PER_LEVEL).toInt()

        var tierIndex = 0
        for (i in TIERS.indices) {
            if (safeHours >= TIERS[i].first) tierIndex = i else break
        }
        val tierStart = TIERS[tierIndex].first
        val tierName = TIERS[tierIndex].second
        val tierEnd = if (tierIndex + 1 < TIERS.size) TIERS[tierIndex + 1].first else tierStart + 2f

        val hoursIntoTier = safeHours - tierStart
        val hoursIntoCurrentBlock = safeHours % HOURS_PER_LEVEL
        val progress = hoursIntoCurrentBlock / HOURS_PER_LEVEL
        val hoursToNext = HOURS_PER_LEVEL - hoursIntoCurrentBlock

        return RankInfo(
            level = level,
            tierName = tierName,
            hoursIntoTier = hoursIntoTier,
            tierSpanHours = tierEnd - tierStart,
            progressToNextLevel = progress,
            hoursToNextLevel = if (hoursToNext >= HOURS_PER_LEVEL) 0f else hoursToNext
        )
    }
}
