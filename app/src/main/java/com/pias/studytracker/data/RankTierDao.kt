package com.pias.studytracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RankTierDao {

    @Query("SELECT * FROM rank_tiers ORDER BY hours ASC")
    fun getAll(): Flow<List<RankTier>>

    @Query("SELECT COUNT(*) FROM rank_tiers")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tier: RankTier)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tiers: List<RankTier>)

    @Delete
    suspend fun delete(tier: RankTier)

    @Query("DELETE FROM rank_tiers")
    suspend fun clearAll()
}
