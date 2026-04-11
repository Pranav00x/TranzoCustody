package com.tranzo.custody.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.data.local.SecurityPreferencesManager
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.data.repository.AuthRepository
import com.tranzo.custody.domain.model.KycStatus
import com.tranzo.custody.domain.model.SpendMode
import com.tranzo.custody.domain.repository.CardRepository
import com.tranzo.custody.web3.SigningManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val ownerAddressShort: String = "",
    val smartWalletAddressShort: String = "",
    val biometricEnabled: Boolean = false,
    val defaultCurrency: String = "USD",
    val defaultChain: String = "Polygon Amoy",
    val pushNotificationsEnabled: Boolean = true,
    val autoLockMinutes: Int = 5,
    val pinRequired: Boolean = true,
    val appVersion: String = "1.0.0",
    val spendMode: SpendMode = SpendMode.SPENDABLE_ONLY,
    val kycStatus: KycStatus = KycStatus.NOT_STARTED,
    val seedBackedUp: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val sessionManager: UserSessionManager,
    private val signingManager: SigningManager,
    private val authRepository: AuthRepository,
    private val securityPrefs: SecurityPreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        loadWalletInfo()
        viewModelScope.launch {
            cardRepository.getCard().collect { card ->
                _state.value = _state.value.copy(
                    spendMode = card.spendMode,
                    kycStatus = card.kycStatus
                )
            }
        }
        viewModelScope.launch {
            sessionManager.seedBackedUp.collect { backed ->
                _state.value = _state.value.copy(seedBackedUp = backed)
            }
        }
        viewModelScope.launch {
            combine(
                securityPrefs.isBiometricEnabled,
                securityPrefs.autoLockMinutes,
                securityPrefs.isPinRequired
            ) { bio, lock, pinReq -> Triple(bio, lock, pinReq) }
                .collectLatest { (bio, lock, pinReq) ->
                    _state.value = _state.value.copy(
                        biometricEnabled = bio,
                        autoLockMinutes = lock,
                        pinRequired = pinReq
                    )
                }
        }
    }

    private fun loadWalletInfo() {
        viewModelScope.launch {
            val owner = sessionManager.getOwnerAddress()
            val smart = sessionManager.getSmartWalletAddress()
            _state.value = _state.value.copy(
                ownerAddressShort = shorten(owner),
                smartWalletAddressShort = shorten(smart)
            )
        }
    }

    private fun shorten(addr: String): String {
        if (addr.length < 12) return addr
        return addr.take(6) + "…" + addr.takeLast(4)
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            securityPrefs.setBiometricEnabled(enabled)
        }
    }

    fun togglePinRequired(enabled: Boolean) {
        viewModelScope.launch {
            securityPrefs.setPinRequired(enabled)
        }
    }

    fun setAutoLockMinutes(minutes: Int) {
        viewModelScope.launch {
            securityPrefs.setAutoLockMinutes(minutes)
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _state.value = _state.value.copy(pushNotificationsEnabled = enabled)
    }

    fun setDefaultCurrency(currency: String) {
        _state.value = _state.value.copy(defaultCurrency = currency)
    }

    fun setSpendMode(mode: SpendMode) {
        _state.value = _state.value.copy(spendMode = mode)
        viewModelScope.launch {
            cardRepository.setSpendMode(mode)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Revoke tokens on backend + clear local auth state
            try { authRepository.logout() } catch (_: Exception) {}
            signingManager.clearWalletKeys()
            sessionManager.clearSession()
            onComplete()
        }
    }
}
