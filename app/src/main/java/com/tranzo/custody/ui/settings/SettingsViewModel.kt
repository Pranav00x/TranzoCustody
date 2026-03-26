package com.tranzo.custody.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.domain.model.KycStatus
import com.tranzo.custody.domain.model.SpendMode
import com.tranzo.custody.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "Tranzo User",
    val userEmail: String = "user@tranzo.money",
    val biometricEnabled: Boolean = true,
    val defaultCurrency: String = "USD",
    val defaultChain: String = "Ethereum",
    val pushNotificationsEnabled: Boolean = true,
    val autoLockMinutes: Int = 5,
    val appVersion: String = "1.0.0",
    val spendMode: SpendMode = SpendMode.SPENDABLE_ONLY,
    val kycStatus: KycStatus = KycStatus.VERIFIED
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            cardRepository.getCard().collect { card ->
                _state.value = _state.value.copy(
                    spendMode = card.spendMode,
                    kycStatus = card.kycStatus
                )
            }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        _state.value = _state.value.copy(biometricEnabled = enabled)
    }

    fun togglePushNotifications(enabled: Boolean) {
        _state.value = _state.value.copy(pushNotificationsEnabled = enabled)
    }

    fun setDefaultCurrency(currency: String) {
        _state.value = _state.value.copy(defaultCurrency = currency)
    }

    fun setAutoLockMinutes(minutes: Int) {
        _state.value = _state.value.copy(autoLockMinutes = minutes)
    }

    fun setSpendMode(mode: SpendMode) {
        _state.value = _state.value.copy(spendMode = mode)
        viewModelScope.launch {
            cardRepository.setSpendMode(mode)
        }
    }
}
