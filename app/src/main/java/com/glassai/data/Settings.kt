package com.glassai.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "glassai_settings")

class SettingsRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val keyProviders = stringPreferencesKey("providers")
    private val keyCurrent = stringPreferencesKey("current")

    val providers: Flow<List<Provider>> = context.dataStore.data.map { prefs ->
        prefs[keyProviders]?.let {
            runCatching { json.decodeFromString<List<Provider>>(it) }.getOrNull()
        } ?: Presets.builtin
    }

    val currentName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[keyCurrent] ?: Presets.builtin.first().name
    }

    suspend fun saveProviders(list: List<Provider>) {
        context.dataStore.edit { it[keyProviders] = json.encodeToString(list) }
    }

    suspend fun selectProvider(name: String) {
        context.dataStore.edit { it[keyCurrent] = name }
    }
}
