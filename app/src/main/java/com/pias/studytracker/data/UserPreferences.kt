package com.pias.studytracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/** Stores the user's display name and reminder time locally. */
class UserPreferences(private val context: Context) {

    private object Keys {
        val NAME = stringPreferencesKey("user_name")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val userName: Flow<String?> = context.dataStore.data.map { it[Keys.NAME] }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.NAME] = name }
    }

    /** Pair(hour, minute) in 24h format, or null if no reminder is set. */
    val reminderTime: Flow<Pair<Int, Int>?> = context.dataStore.data.map { prefs ->
        val h = prefs[Keys.REMINDER_HOUR]
        val m = prefs[Keys.REMINDER_MINUTE]
        if (h != null && m != null) h to m else null
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun clearReminderTime() {
        context.dataStore.edit {
            it.remove(Keys.REMINDER_HOUR)
            it.remove(Keys.REMINDER_MINUTE)
        }
    }
}
