package com.pias.studytracker.data

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // Insert or overwrite the entry for a date (e.g. correcting today's hours later).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: StudyEntry)

    @Query("SELECT * FROM study_entries ORDER BY date ASC")
    fun getAll(): Flow<List<StudyEntry>>

    @Query("SELECT * FROM study_entries WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: String): StudyEntry?

    @Query("SELECT COALESCE(SUM(hours), 0) FROM study_entries")
    fun getTotalHours(): Flow<Float>

    @Query("SELECT MIN(date) FROM study_entries")
    fun getFirstDate(): Flow<String?>

    @Query("DELETE FROM study_entries WHERE date = :date")
    suspend fun delete(date: String)
}
