package com.tranzo.custody.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tranzo_wallet_session")

class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_WALLET_READY = booleanPreferencesKey("wallet_ready")
        private val KEY_OWNER_ADDR = stringPreferencesKey("owner_address")
        private val KEY_SMART_WALLET = stringPreferencesKey("smart_wallet_address")
        private val KEY_CHAIN_ID = intPreferencesKey("chain_id")
        private val KEY_SEED_BACKED_UP = booleanPreferencesKey("seed_backed_up")
        private val KEY_PIN_SALT = stringPreferencesKey("pin_salt_b64")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
    }

    val seedBackedUp: Flow<Boolean> = context.dataStore.data.map { it[KEY_SEED_BACKED_UP] ?: false }

    suspend fun hasWallet(): Boolean =
        context.dataStore.data.first()[KEY_WALLET_READY] == true

    suspend fun saveWalletSession(
        ownerAddress: String,
        smartWalletAddress: String,
        chainId: Int,
        pin: String
    ) {
        val salt = java.util.Base64.getEncoder().encodeToString(
            ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        )
        val hash = hashPin(pin, salt)
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLET_READY] = true
            prefs[KEY_OWNER_ADDR] = ownerAddress.lowercase()
            prefs[KEY_SMART_WALLET] = smartWalletAddress.lowercase()
            prefs[KEY_CHAIN_ID] = chainId
            prefs[KEY_SEED_BACKED_UP] = true
            prefs[KEY_PIN_SALT] = salt
            prefs[KEY_PIN_HASH] = hash
        }
    }

    suspend fun markSeedBackedUp() {
        context.dataStore.edit { it[KEY_SEED_BACKED_UP] = true }
    }

    suspend fun getOwnerAddress(): String =
        context.dataStore.data.first()[KEY_OWNER_ADDR].orEmpty()

    suspend fun getSmartWalletAddress(): String =
        context.dataStore.data.first()[KEY_SMART_WALLET].orEmpty()

    suspend fun getChainId(): Int =
        context.dataStore.data.first()[KEY_CHAIN_ID] ?: 0

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val salt = prefs[KEY_PIN_SALT] ?: return false
        val expected = prefs[KEY_PIN_HASH] ?: return false
        return hashPin(pin, salt) == expected
    }

    // ── Auth Token Management ──

    suspend fun saveAuthTokens(accessToken: String, refreshToken: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.first()[KEY_ACCESS_TOKEN]

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.first()[KEY_REFRESH_TOKEN]

    suspend fun getUserId(): String? =
        context.dataStore.data.first()[KEY_USER_ID]

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearAuthTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_USER_ID)
        }
    }

    suspend fun isAuthenticated(): Boolean =
        getAccessToken() != null

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    private fun hashPin(pin: String, saltB64: String): String {
        val salt = java.util.Base64.getDecoder().decode(saltB64)
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(pin.toByteArray(Charsets.UTF_8))
        return md.digest().joinToString("") { b -> "%02x".format(b) }
    }
}

