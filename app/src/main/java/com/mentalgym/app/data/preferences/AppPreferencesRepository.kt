package com.mentalgym.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences"
)

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.appPreferencesDataStore

    val groqApiKey: Flow<String> = dataStore.data.map { prefs ->
        prefs[GROQ_API_KEY].orEmpty()
    }

    val dailyReminderEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DAILY_REMINDER_ENABLED] ?: false
    }

    val dailyReminderTime: Flow<String> = dataStore.data.map { prefs ->
        prefs[DAILY_REMINDER_TIME] ?: REMINDER_TIME_630
    }

    suspend fun setGroqApiKey(key: String) {
        dataStore.edit { it[GROQ_API_KEY] = key.trim() }
    }

    suspend fun clearGroqApiKey() {
        dataStore.edit { it.remove(GROQ_API_KEY) }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[DAILY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setDailyReminderTime(timeKey: String) {
        val normalized = if (timeKey == REMINDER_TIME_700) REMINDER_TIME_700 else REMINDER_TIME_630
        dataStore.edit { it[DAILY_REMINDER_TIME] = normalized }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    /**
     * Short snippets from prior generated topics/prompts for this exercise, oldest → newest.
     * Used to nudge Groq / local fallbacks away from repeating the same debate or brainstorm.
     */
    suspend fun getRecentDrillSnippets(exerciseId: String, limit: Int = 14): List<String> {
        val raw = dataStore.data.first()[DRILL_PROMPT_HISTORY].orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val p = line.indexOf('|')
                if (p <= 0 || p >= line.lastIndex) null
                else {
                    val id = line.substring(0, p)
                    val snippet = line.substring(p + 1).trim()
                    if (id == exerciseId && snippet.isNotEmpty()) snippet else null
                }
            }
            .toList()
            .takeLast(limit)
    }

    /** Records a drill prompt fingerprint after a successful load (one line per exercise). */
    suspend fun appendDrillSnippet(exerciseId: String, snippet: String, maxTotalLines: Int = 72) {
        val clean = snippet.trim().replace("\n", " ").take(COACH_SNIPPET_MAX_LEN).trim()
        if (clean.length < COACH_SNIPPET_MIN_LEN) return
        dataStore.edit { prefs ->
            val lines = prefs[DRILL_PROMPT_HISTORY].orEmpty()
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toMutableList()
            lines.add("$exerciseId|$clean")
            while (lines.size > maxTotalLines) lines.removeAt(0)
            prefs[DRILL_PROMPT_HISTORY] = lines.joinToString("\n")
        }
    }

    companion object {
        const val REMINDER_TIME_630 = "18:30"
        const val REMINDER_TIME_700 = "19:00"

        private val GROQ_API_KEY = stringPreferencesKey("groq_api_key")
        private val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        private val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        private val DRILL_PROMPT_HISTORY = stringPreferencesKey("drill_prompt_history_v1")

        private const val COACH_SNIPPET_MIN_LEN = 12
        private const val COACH_SNIPPET_MAX_LEN = 220
    }
}
