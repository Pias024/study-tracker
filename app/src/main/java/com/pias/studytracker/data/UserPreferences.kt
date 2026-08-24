package com.pias.studytracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/** Stores the user's display name locally. Null until they complete the first-launch prompt. */
class UserPreferences(private val context: Context) {

    private object Keys {
        val NAME = stringPreferencesKey("user_name")
    }

    val userName: Flow<String?> = context.dataStore.data.map { it[Keys.NAME] }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.NAME] = name }
    }
}
