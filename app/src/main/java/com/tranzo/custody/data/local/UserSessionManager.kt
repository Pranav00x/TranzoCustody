package com.tranzo.custody.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: ""
    }

    val pinHash: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PIN_HASH] ?: ""
    }

    suspend fun saveSession(name: String, email: String, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_PIN_HASH] = pin.hashCode().toString()
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        return context.dataStore.data.first()[KEY_IS_LOGGED_IN] ?: false
    }

    suspend fun getSavedEmail(): String {
        return context.dataStore.data.first()[KEY_USER_EMAIL] ?: ""
    }

    suspend fun getSavedName(): String {
        return context.dataStore.data.first()[KEY_USER_NAME] ?: ""
    }

    suspend fun getSavedPinHash(): String {
        return context.dataStore.data.first()[KEY_PIN_HASH] ?: ""
    }

    suspend fun verifyPin(pin: String): Boolean {
        val savedHash = getSavedPinHash()
        return savedHash.isNotEmpty() && savedHash == pin.hashCode().toString()
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
