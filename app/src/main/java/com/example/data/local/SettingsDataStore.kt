package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.ResponseStyle
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hamshira_settings")

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val RESPONSE_STYLE = stringPreferencesKey("response_style")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DISCLAIMER_MINIMIZED = booleanPreferencesKey("disclaimer_minimized")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            language = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.TG,
            responseStyle = prefs[Keys.RESPONSE_STYLE]?.let { runCatching { ResponseStyle.valueOf(it) }.getOrNull() }
                ?: ResponseStyle.BALANCED,
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            disclaimerMinimized = prefs[Keys.DISCLAIMER_MINIMIZED] ?: false
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setResponseStyle(style: ResponseStyle) {
        context.dataStore.edit { it[Keys.RESPONSE_STYLE] = style.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDisclaimerMinimized(minimized: Boolean) {
        context.dataStore.edit { it[Keys.DISCLAIMER_MINIMIZED] = minimized }
    }
}
