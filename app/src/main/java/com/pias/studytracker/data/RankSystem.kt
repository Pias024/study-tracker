package com.pias.studytracker.data

import kotlin.math.floor

data class RankInfo(
    val level: Int,
    val tierName: String,
    val hoursIntoTier: Float,
    val tierSpanHours: Float,
    val progressToNextLevel: Float, // 0f..1f within the current 0.5h block
    val hoursToNextLevel: Float
)

/**
 * Rank ladder logic. Tiers are now USER-EDITABLE (stored in Room via RankTier/RankTierDao) —
 * this object no longer hardcodes them, it just evaluates whatever list it's given.
 * DEFAULT_TIERS below is only the seed data used on first install and the
 * "Reset to default" action in Settings.
 */
object RankSystem {

    const val HOURS_PER_LEVEL = 0.5f

    // Bug-bounty themed defaults. 8h is a fixed milestone: studying 8h/day = Elite Hunter.
    val DEFAULT_TIERS: List<Pair<Float, String>> = listOf(
        0f to "Script Kiddie",
        1f to "Recon Rookie",
        2f to "Bug Hunter",
        3f to "Vulnerability Analyst",
        4f to "Exploit Developer",
        5f to "Red Teamer",
        6f to "Security Researcher",
        8f to "Elite Hunter",
        9f to "Bounty Legend",
        10f to "Grandmaster Hacker"
    )

    fun rankFor(totalHours: Float, tiers: List<Pair<Float, String>> = DEFAULT_TIERS): RankInfo {
        val sortedTiers = tiers.sortedBy { it.first }
        if (sortedTiers.isEmpty()) {
            return RankInfo(0, "Unranked", 0f, HOURS_PER_LEVEL, 0f, HOURS_PER_LEVEL)
        }

        val safeHours = totalHours.coerceAtLeast(0f)
        val level = floor(safeHours / HOURS_PER_LEVEL).toInt()

        var tierIndex = 0
        for (i in sortedTiers.indices) {
            if (safeHours >= sortedTiers[i].first) tierIndex = i else break
        }
        val tierStart = sortedTiers[tierIndex].first
        val tierName = sortedTiers[tierIndex].second
        val tierEnd = if (tierIndex + 1 < sortedTiers.size) sortedTiers[tierIndex + 1].first else tierStart + 2f

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
