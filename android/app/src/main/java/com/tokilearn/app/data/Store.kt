package com.tokilearn.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("tokilearn")

/** DataStore 键全部按学科分键（纯函数，可单元测试）。 */
fun wrongKey(subjectId: String): Preferences.Key<Set<String>> = stringSetPreferencesKey("wrong_$subjectId")
fun answeredKey(subjectId: String): Preferences.Key<Set<String>> = stringSetPreferencesKey("answered_$subjectId")
fun correctKey(subjectId: String): Preferences.Key<Set<String>> = stringSetPreferencesKey("correct_$subjectId")

private val THEME_MODE = stringPreferencesKey("theme_mode") // dark | light | system，默认 dark
private val GEMINI_KEY = stringPreferencesKey("gemini_key")
private val GEMINI_MODEL = stringPreferencesKey("gemini_model")
private val LAST_SUBJECT = stringPreferencesKey("last_subject_id")

/** 主题模式：dark（默认）/ light / system。 */
enum class ThemeMode { DARK, LIGHT, SYSTEM }

/**
 * 本地存储：做题进度、错题本按学科分键；Gemini Key 与模型名只存本机，
 * 请求用 OkHttp 直连 Google Gemini API，不经过任何服务器。
 */
class LearnStore(private val context: Context) {
    private val store get() = context.dataStore

    val themeMode: Flow<ThemeMode> = store.data.map {
        when (it[THEME_MODE]) {
            "light" -> ThemeMode.LIGHT
            "system" -> ThemeMode.SYSTEM
            else -> ThemeMode.DARK
        }
    }

    val geminiKey: Flow<String> = store.data.map { it[GEMINI_KEY] ?: "" }
    val geminiModel: Flow<String> = store.data.map { it[GEMINI_MODEL] ?: "gemini-2.0-flash" }
    val lastSubjectId: Flow<String?> = store.data.map { it[LAST_SUBJECT] }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[THEME_MODE] = mode.name.lowercase() }
    }

    suspend fun setGeminiKey(key: String) {
        store.edit { it[GEMINI_KEY] = key.trim() }
    }

    suspend fun setGeminiModel(model: String) {
        store.edit { it[GEMINI_MODEL] = model.trim().ifEmpty { "gemini-2.0-flash" } }
    }

    suspend fun setLastSubject(subjectId: String) {
        store.edit { it[LAST_SUBJECT] = subjectId }
    }

    fun wrongIds(subjectId: String): Flow<Set<String>> =
        store.data.map { it[wrongKey(subjectId)] ?: emptySet() }

    fun answeredIds(subjectId: String): Flow<Set<String>> =
        store.data.map { it[answeredKey(subjectId)] ?: emptySet() }

    fun correctIds(subjectId: String): Flow<Set<String>> =
        store.data.map { it[correctKey(subjectId)] ?: emptySet() }

    /** 记录一次作答：correct 为 true 时记答对，否则进错题本。 */
    suspend fun recordAnswer(subjectId: String, questionId: String, correct: Boolean) {
        store.edit { prefs ->
            val answered = (prefs[answeredKey(subjectId)] ?: emptySet()) + questionId
            prefs[answeredKey(subjectId)] = answered
            if (correct) {
                prefs[correctKey(subjectId)] = (prefs[correctKey(subjectId)] ?: emptySet()) + questionId
                prefs[wrongKey(subjectId)] = (prefs[wrongKey(subjectId)] ?: emptySet()) - questionId
            } else {
                prefs[wrongKey(subjectId)] = (prefs[wrongKey(subjectId)] ?: emptySet()) + questionId
            }
        }
    }

    suspend fun removeWrong(subjectId: String, questionId: String) {
        store.edit { it[wrongKey(subjectId)] = (it[wrongKey(subjectId)] ?: emptySet()) - questionId }
    }

    suspend fun clearWrong(subjectId: String) {
        store.edit { it[wrongKey(subjectId)] = emptySet() }
    }

    suspend fun wrongCount(subjectId: String): Int =
        store.data.first()[wrongKey(subjectId)]?.size ?: 0
}
