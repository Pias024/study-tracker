package com.pias.studytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per calendar date. [date] is stored as "yyyy-MM-dd" so it sorts
 * and groups correctly. [hours] is the total hours studied that day.
 * Saving the same date again overwrites the previous value (see DAO upsert).
 */
@Entity(tableName = "study_entries")
data class StudyEntry(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val hours: Float
)
