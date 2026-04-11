package com.tranzo.custody.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "tranzo_security_prefs")

@Singleton
class SecurityPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes") // 0 = Never
        private val KEY_LAST_ACTIVE_TIME = longPreferencesKey("last_active_time")
        private val KEY_PIN_REQUIRED = booleanPreferencesKey("pin_required")
    }

    val isBiometricEnabled: Flow<Boolean> = context.securityDataStore.data.map {
        it[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val autoLockMinutes: Flow<Int> = context.securityDataStore.data.map {
        it[KEY_AUTO_LOCK_MINUTES] ?: 5 // Default 5 minutes
    }

    val isPinRequired: Flow<Boolean> = context.securityDataStore.data.map {
        it[KEY_PIN_REQUIRED] ?: true
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAutoLockMinutes(minutes: Int) {
        context.securityDataStore.edit { it[KEY_AUTO_LOCK_MINUTES] = minutes }
    }

    suspend fun setPinRequired(required: Boolean) {
        context.securityDataStore.edit { it[KEY_PIN_REQUIRED] = required }
    }

    suspend fun updateLastActiveTime() {
        context.securityDataStore.edit { it[KEY_LAST_ACTIVE_TIME] = System.currentTimeMillis() }
    }

    suspend fun getLastActiveTime(): Long {
        return context.securityDataStore.edit { }.get(KEY_LAST_ACTIVE_TIME) ?: 0L
    }
}
