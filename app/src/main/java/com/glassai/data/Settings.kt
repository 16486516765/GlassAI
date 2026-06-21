package com.glassai.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "glassai_settings")

@Serializable
data class HistoryMessage(val role: String, val content: String)

class SettingsRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val keyProviders = stringPreferencesKey("providers")
    private val keyCurrent = stringPreferencesKey("current")
    private val keyHistory = stringPreferencesKey("history")

    val providers: Flow<List<Provider>> = context.dataStore.data
        .map { prefs ->
            prefs[keyProviders]?.let {
                runCatching { json.decodeFromString<List<Provider>>(it) }.getOrNull()
            } ?: Presets.builtin
        }
        .catch { emit(Presets.builtin) }

    val currentName: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[keyCurrent] ?: Presets.builtin.first().name }
        .catch { emit(Presets.builtin.first().name) }

    val history: Flow<List<HistoryMessage>> = context.dataStore.data
        .map { prefs ->
            prefs[keyHistory]?.let {
                runCatching { json.decodeFromString<List<HistoryMessage>>(it) }.getOrNull()
            } ?: emptyList()
        }
        .catch { emit(emptyList()) }

    suspend fun saveProviders(list: List<Provider>) {
        context.dataStore.edit { it[keyProviders] = json.encodeToString(ListSerializer(Provider.serializer()), list) }
    }

    suspend fun selectProvider(name: String) {
        context.dataStore.edit { it[keyCurrent] = name }
    }

    suspend fun saveHistory(list: List<HistoryMessage>) {
        runCatching {
            context.dataStore.edit { it[keyHistory] = json.encodeToString(ListSerializer(HistoryMessage.serializer()), list) }
        }
    }
}
