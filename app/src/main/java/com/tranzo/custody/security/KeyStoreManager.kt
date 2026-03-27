package com.tranzo.custody.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyStoreManager @Inject constructor(@ApplicationContext private val context: Context
) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "tranzo_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val PREFS = "tranzo_wallet_secure"
        private const val PK_IV = "pk_iv_b64"
        private const val PK_CIPHER = "pk_cipher_b64"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun encrypt(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return Pair(iv, encrypted)
    }

    fun decrypt(iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
        return cipher.doFinal(encryptedData)
    }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) return existingKey.secretKey

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun hasKey(): Boolean = keyStore.containsAlias(KEY_ALIAS)

    fun deleteKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    fun storeEncryptedPrivateKey(privateKeyBytes: ByteArray) {
        val (iv, cipher) = encrypt(privateKeyBytes)
        encryptedPrefs.edit()
            .putString(PK_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .putString(PK_CIPHER, Base64.encodeToString(cipher, Base64.NO_WRAP))
            .apply()
    }

    fun hasStoredPrivateKey(): Boolean {
        val iv = encryptedPrefs.getString(PK_IV, null)
        val ct = encryptedPrefs.getString(PK_CIPHER, null)
        return !iv.isNullOrBlank() && !ct.isNullOrBlank()
    }

    fun loadDecryptedPrivateKey(): ByteArray? {
        val ivB64 = encryptedPrefs.getString(PK_IV, null) ?: return null
        val ctB64 = encryptedPrefs.getString(PK_CIPHER, null) ?: return null
        return try {
            val iv = Base64.decode(ivB64, Base64.NO_WRAP)
            val ct = Base64.decode(ctB64, Base64.NO_WRAP)
            decrypt(iv, ct)
        } catch (_: Exception) {
            null
        }
    }

    fun clearPrivateKey() {
        encryptedPrefs.edit()
            .remove(PK_IV)
            .remove(PK_CIPHER)
            .apply()
    }

    fun wipeAll() {
        clearPrivateKey()
        deleteKey()
    }
}

