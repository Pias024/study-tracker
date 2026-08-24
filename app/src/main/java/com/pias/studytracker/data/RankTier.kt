package com.pias.studytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single user-editable rank tier: "at [hours] hours, you're called [name]".
 * The list is always displayed/evaluated sorted by hours ascending — so
 * "reordering" tiers means editing their hour thresholds, there's no separate
 * manual ordering field. Kept intentionally simple for a threshold ladder.
 */
@Entity(tableName = "rank_tiers")
data class RankTier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hours: Float,
    val name: String
)
