package com.tranzo.custody.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.BuildConfig
import com.tranzo.custody.data.backup.DriveBackupManager
import com.tranzo.custody.data.backup.RestoreResult
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.data.repository.AuthRepository
import com.tranzo.custody.web3.MnemonicManager
import com.tranzo.custody.web3.SigningManager
import com.tranzo.custody.web3.SmartAccountManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

data class LoginState(
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val needsWalletRestore: Boolean = false,
    val restoreError: String? = null,
    val isRestoring: Boolean = false,
    val restoreNeedsPin: Boolean = false,
    val restoreSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: UserSessionManager,
    private val signingManager: SigningManager,
    private val mnemonicManager: MnemonicManager,
    private val smartAccountManager: SmartAccountManager,
    private val driveBackupManager: DriveBackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun setEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun setPassword(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Enter email and password")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val user = authRepository.login(s.email, s.password)

                // Check if wallet credentials exist locally
                val hasWallet = signingManager.loadCredentials() != null
                if (hasWallet) {
                    // Wallet exists locally — just ensure session is active and go to home
                    // We don't want to call saveWalletSession("") because it overwrites the existing PIN
                    _state.value = _state.value.copy(isLoading = false, loginSuccess = true)
                } else {
                    // No local wallet — need to restore from Drive or Seed
                    _state.value = _state.value.copy(
                        isLoading = false,
                        needsWalletRestore = true
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed"
                )
            }
        }
    }

    fun loginWithBiometrics() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val hasWallet = signingManager.loadCredentials() != null
                if (hasWallet) {
                    val owner = sessionManager.getOwnerAddress()
                    val smart = sessionManager.getSmartWalletAddress()
                    if (owner.isNotEmpty() && smart.isNotEmpty()) {
                        _state.value = _state.value.copy(isLoading = false, loginSuccess = true)
                    } else {
                        _state.value = _state.value.copy(isLoading = false, error = "Account data missing locally. Please log in with email/password once.")
                    }
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "No wallet found on this device. Please log in with email.")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Biometric login failed")
            }
        }
    }

    fun restoreFromDrive(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRestoring = true, restoreError = null)
            try {
                val password = _state.value.password
                when (val result = driveBackupManager.restore(password, account)) {
                    is RestoreResult.Success -> {
                        withContext(Dispatchers.IO) {
                            val creds = mnemonicManager.deriveCredentials(result.mnemonic)
                            signingManager.persistCredentials(creds)
                        }
                        _state.value = _state.value.copy(
                            isRestoring = false,
                            restoreNeedsPin = true
                        )
                    }
                    is RestoreResult.NoBackupFound -> {
                        _state.value = _state.value.copy(
                            isRestoring = false,
                            restoreError = "No backup found on Google Drive"
                        )
                    }
                    is RestoreResult.WrongPassword -> {
                        _state.value = _state.value.copy(
                            isRestoring = false,
                            restoreError = "Wrong password — backup was encrypted with a different password"
                        )
                    }
                    is RestoreResult.Error -> {
                        _state.value = _state.value.copy(
                            isRestoring = false,
                            restoreError = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRestoring = false,
                    restoreError = e.message ?: "Restore failed"
                )
            }
        }
    }

    fun restoreFromMnemonic(mnemonic: String) {
        if (!mnemonicManager.validateMnemonic(mnemonic)) {
            _state.value = _state.value.copy(restoreError = "Invalid recovery phrase")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isRestoring = true, restoreError = null)
            try {
                withContext(Dispatchers.IO) {
                    val normalized = mnemonicManager.normalizeMnemonic(mnemonic)
                    val creds = mnemonicManager.deriveCredentials(normalized)
                    signingManager.persistCredentials(creds)
                }
                _state.value = _state.value.copy(isRestoring = false, restoreNeedsPin = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRestoring = false,
                    restoreError = e.message ?: "Restore failed"
                )
            }
        }
    }

    fun forgotPassword() {
        val email = _state.value.email
        if (email.isBlank()) {
            _state.value = _state.value.copy(error = "Enter your email first")
            return
        }
        viewModelScope.launch {
            try {
                authRepository.forgotPassword(email)
                _state.value = _state.value.copy(
                    error = null
                )
            } catch (_: Exception) { }
        }
    }
}
