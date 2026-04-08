package com.tranzo.custody.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tranzo.custody.ui.theme.AppFontId
import com.tranzo.custody.ui.theme.AppThemeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "tranzo_theme_prefs")

class ThemePreferencesManager(private val context: Context) {

    companion object {
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_FONT = stringPreferencesKey("app_font")
    }

    val selectedTheme: Flow<AppThemeId> = context.themeDataStore.data.map { prefs ->
        val name = prefs[KEY_THEME] ?: AppThemeId.MIDNIGHT.name
        try { AppThemeId.valueOf(name) } catch (_: Exception) { AppThemeId.MIDNIGHT }
    }

    val selectedFont: Flow<AppFontId> = context.themeDataStore.data.map { prefs ->
        val name = prefs[KEY_FONT] ?: AppFontId.INTER.name
        try { AppFontId.valueOf(name) } catch (_: Exception) { AppFontId.INTER }
    }

    suspend fun setTheme(themeId: AppThemeId) {
        context.themeDataStore.edit { it[KEY_THEME] = themeId.name }
    }

    suspend fun setFont(fontId: AppFontId) {
        context.themeDataStore.edit { it[KEY_FONT] = fontId.name }
    }
}
