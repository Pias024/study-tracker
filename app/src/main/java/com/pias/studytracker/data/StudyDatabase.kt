package com.pias.studytracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [StudyEntry::class, RankTier::class], version = 2, exportSchema = false)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao
    abstract fun rankTierDao(): RankTierDao

    companion object {
        // Adds the rank_tiers table without touching existing study_entries data.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS rank_tiers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        hours REAL NOT NULL,
                        name TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile private var INSTANCE: StudyDatabase? = null

        fun getInstance(context: Context): StudyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_tracker.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
