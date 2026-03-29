package com.tranzo.custody.data.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encrypted wallet backup to Google Drive App Data folder.
 *
 * The mnemonic is encrypted client-side with the user's password before uploading.
 * Google Drive App Data is invisible to the user and auto-deleted on app uninstall.
 */
@Singleton
class DriveBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val BACKUP_FILENAME = "tranzo_wallet_backup.enc"
        private const val PBKDF2_ITERATIONS = 600_000
        private const val AES_KEY_LENGTH = 256
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        private const val SALT_LENGTH = 32
    }

    // ──────────────── Google Sign-In ─────────────────

    fun getSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent = getSignInClient().signInIntent

    fun getSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    private fun buildDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("TranzoCustody")
            .build()
    }

    // ──────────────── Backup ──────────────────────────

    /**
     * Encrypt the mnemonic with the user's password and upload to Google Drive.
     * Requires a signed-in Google account with Drive App Data scope.
     */
    suspend fun backup(
        mnemonic: String,
        password: String,
        ownerAddr: String,
        account: GoogleSignInAccount
    ): Boolean = withContext(Dispatchers.IO) {
        val encrypted = encrypt(mnemonic, password, ownerAddr)
        val drive = buildDriveService(account)

        // Delete old backup if it exists
        deleteExisting(drive)

        // Upload new backup
        val metadata = com.google.api.services.drive.model.File().apply {
            name = BACKUP_FILENAME
            parents = listOf("appDataFolder")
        }
        val content = ByteArrayContent.fromString("application/json", encrypted)
        drive.files().create(metadata, content)
            .setFields("id")
            .execute()

        true
    }

    // ──────────────── Restore ─────────────────────────

    /**
     * Download and decrypt the wallet backup from Google Drive.
     * Returns the mnemonic if successful.
     */
    suspend fun restore(
        password: String,
        account: GoogleSignInAccount
    ): RestoreResult = withContext(Dispatchers.IO) {
        val drive = buildDriveService(account)

        val files = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILENAME'")
            .setFields("files(id, name)")
            .execute()

        val backupFile = files.files?.firstOrNull()
            ?: return@withContext RestoreResult.NoBackupFound

        val stream = drive.files().get(backupFile.id).executeMediaAsInputStream()
        val json = stream.bufferedReader().readText()

        try {
            val result = decrypt(json, password)
            RestoreResult.Success(mnemonic = result.mnemonic, ownerAddr = result.ownerAddr)
        } catch (_: org.json.JSONException) {
            RestoreResult.Error("Backup file is corrupted or not valid JSON")
        } catch (_: javax.crypto.AEADBadTagException) {
            RestoreResult.WrongPassword
        } catch (_: javax.crypto.BadPaddingException) {
            RestoreResult.WrongPassword
        } catch (e: Exception) {
            RestoreResult.Error(e.message ?: "Failed to restore backup")
        }
    }

    /**
     * Check if a backup exists on Google Drive.
     */
    suspend fun hasBackup(account: GoogleSignInAccount): Boolean = withContext(Dispatchers.IO) {
        val drive = buildDriveService(account)
        val files = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILENAME'")
            .setFields("files(id)")
            .setPageSize(1)
            .execute()
        !files.files.isNullOrEmpty()
    }

    private fun deleteExisting(drive: Drive) {
        val files = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILENAME'")
            .setFields("files(id)")
            .execute()
        files.files?.forEach { drive.files().delete(it.id).execute() }
    }

    // ──────────────── Crypto ──────────────────────────

    private fun encrypt(mnemonic: String, password: String, ownerAddr: String): String {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val ciphertext = cipher.doFinal(mnemonic.toByteArray(Charsets.UTF_8))

        return JSONObject().apply {
            put("version", 1)
            put("kdf", "pbkdf2-sha256")
            put("kdfIterations", PBKDF2_ITERATIONS)
            put("salt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            put("iv", android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
            put("ciphertext", android.util.Base64.encodeToString(ciphertext, android.util.Base64.NO_WRAP))
            put("ownerAddr", ownerAddr)
        }.toString()
    }

    private fun decrypt(json: String, password: String): DecryptResult {
        val obj = JSONObject(json)
        val salt = android.util.Base64.decode(obj.getString("salt"), android.util.Base64.NO_WRAP)
        val iv = android.util.Base64.decode(obj.getString("iv"), android.util.Base64.NO_WRAP)
        val ciphertext = android.util.Base64.decode(obj.getString("ciphertext"), android.util.Base64.NO_WRAP)
        val iterations = obj.optInt("kdfIterations", PBKDF2_ITERATIONS)
        val ownerAddr = obj.optString("ownerAddr", "")

        val key = deriveKey(password, salt, iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val plaintext = cipher.doFinal(ciphertext)
        return DecryptResult(
            mnemonic = String(plaintext, Charsets.UTF_8),
            ownerAddr = ownerAddr
        )
    }

    private fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int = PBKDF2_ITERATIONS
    ): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, AES_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private data class DecryptResult(val mnemonic: String, val ownerAddr: String)
}

sealed class RestoreResult {
    data class Success(val mnemonic: String, val ownerAddr: String) : RestoreResult()
    data object NoBackupFound : RestoreResult()
    data object WrongPassword : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}
