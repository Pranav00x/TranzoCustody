package com.tranzo.custody.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "Tranzo User",
    val userEmail: String = "user@tranzo.money",
    val biometricEnabled: Boolean = true,
    val defaultCurrency: String = "USD",
    val defaultChain: String = "Ethereum",
    val pushNotificationsEnabled: Boolean = true,
    val autoLockMinutes: Int = 5,
    val appVersion: String = "1.0.0"
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

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
}
