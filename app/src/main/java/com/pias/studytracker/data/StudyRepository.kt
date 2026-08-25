package com.pias.studytracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StudyRepository(
    private val dao: StudyDao,
    private val rankTierDao: RankTierDao
) {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    // ---------- Study entries ----------

    suspend fun saveHours(date: LocalDate, hours: Float) {
        dao.upsert(StudyEntry(date.format(formatter), hours))
    }

    suspend fun deleteDate(date: LocalDate) {
        dao.delete(date.format(formatter))
    }

    suspend fun getHoursFor(date: LocalDate): Float =
        dao.getForDate(date.format(formatter))?.hours ?: 0f

    fun getAllEntries(): Flow<List<StudyEntry>> = dao.getAll()

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

    // ---------- Rank tiers (user-editable) ----------

    fun getRankTiers(): Flow<List<RankTier>> = rankTierDao.getAll()

    /** Seeds the default ladder if the table is empty (fresh install or migrated DB). */
    suspend fun ensureRankTiersSeeded() {
        if (rankTierDao.count() == 0) {
            resetRankTiersToDefault()
        }
    }

    suspend fun upsertRankTier(tier: RankTier) = rankTierDao.upsert(tier)

    suspend fun deleteRankTier(tier: RankTier) = rankTierDao.delete(tier)

    suspend fun resetRankTiersToDefault() {
        rankTierDao.clearAll()
        rankTierDao.insertAll(RankSystem.DEFAULT_TIERS.map { (hours, name) -> RankTier(hours = hours, name = name) })
    }

    // ---------- Backup: export / import ----------

    /** Serializes all entries + rank tiers + the given display name into one JSON string. */
    suspend fun exportBackupJson(userName: String?): String {
        val entries = dao.getAll().first()
        val tiers = rankTierDao.getAll().first()

        val entriesJson = JSONArray()
        entries.forEach {
            entriesJson.put(JSONObject().apply {
                put("date", it.date)
                put("hours", it.hours)
            })
        }

        val tiersJson = JSONArray()
        tiers.forEach {
            tiersJson.put(JSONObject().apply {
                put("hours", it.hours)
                put("name", it.name)
            })
        }

        val root = JSONObject().apply {
            put("userName", userName ?: JSONObject.NULL)
            put("entries", entriesJson)
            put("rankTiers", tiersJson)
        }
        return root.toString(2)
    }

    /**
     * Replaces all local data with what's in [json]. Returns the restored user name
     * (caller is responsible for saving it to UserPreferences — that lives outside Room).
     */
    suspend fun importBackupJson(json: String): String? {
        val root = JSONObject(json)

        val entriesJson = root.optJSONArray("entries") ?: JSONArray()
        val tiersJson = root.optJSONArray("rankTiers") ?: JSONArray()

        // Wipe existing data first so import is a clean replace, not a merge.
        for (existing in dao.getAll().first()) {
            dao.delete(existing.date)
        }
        rankTierDao.clearAll()

        for (i in 0 until entriesJson.length()) {
            val obj = entriesJson.getJSONObject(i)
            dao.upsert(StudyEntry(obj.getString("date"), obj.getDouble("hours").toFloat()))
        }

        val restoredTiers = mutableListOf<RankTier>()
        for (i in 0 until tiersJson.length()) {
            val obj = tiersJson.getJSONObject(i)
            restoredTiers.add(RankTier(hours = obj.getDouble("hours").toFloat(), name = obj.getString("name")))
        }
        if (restoredTiers.isNotEmpty()) {
            rankTierDao.insertAll(restoredTiers)
        } else {
            resetRankTiersToDefault()
        }

        return if (root.isNull("userName")) null else root.optString("userName", null)
    }
}
