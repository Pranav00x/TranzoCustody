package com.tranzo.custody.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.BuildConfig
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.web3.SigningManager
import com.tranzo.custody.web3.SmartAccountManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

data class RestorePinState(
    val pin: String = "",
    val confirmPin: String = "",
    val isSettingPin: Boolean = true,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isComplete: Boolean = false
)

@HiltViewModel
class RestorePinViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val signingManager: SigningManager,
    private val smartAccountManager: SmartAccountManager
) : ViewModel() {

    private val _state = MutableStateFlow(RestorePinState())
    val state = _state.asStateFlow()

    fun onPinDigit(digit: Int) {
        val s = _state.value
        if (s.isSettingPin) {
            if (s.pin.length < 6) {
                val newPin = s.pin + digit
                _state.update { it.copy(pin = newPin, error = null) }
                if (newPin.length == 6) {
                    _state.update { it.copy(isSettingPin = false) }
                }
            }
        } else {
            if (s.confirmPin.length < 6) {
                val newC = s.confirmPin + digit
                _state.update { it.copy(confirmPin = newC, error = null) }
                if (newC.length == 6) {
                    if (s.pin == newC) {
                        finalize()
                    } else {
                        _state.update { it.copy(confirmPin = "", error = "PINs do not match", isSettingPin = true, pin = "") }
                    }
                }
            }
        }
    }

    fun onPinDelete() {
        val s = _state.value
        if (s.isSettingPin) {
            if (s.pin.isNotEmpty()) _state.update { it.copy(pin = s.pin.dropLast(1)) }
        } else {
            if (s.confirmPin.isNotEmpty()) {
                _state.update { it.copy(confirmPin = s.confirmPin.dropLast(1)) }
            } else {
                _state.update { it.copy(isSettingPin = true, pin = "") }
            }
        }
    }

    private fun finalize() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val credentials = signingManager.loadCredentials()
                if (credentials != null) {
                    val predicted = smartAccountManager.computeCounterfactualAddress(
                        credentials.address,
                        BigInteger.ONE
                    )
                    sessionManager.saveWalletSession(
                        ownerAddress = credentials.address,
                        smartWalletAddress = predicted,
                        chainId = BuildConfig.DEFAULT_CHAIN_ID,
                        pin = _state.value.pin
                    )
                    _state.update { it.copy(isComplete = true, isLoading = false) }
                } else {
                    _state.update { it.copy(error = "Wallet credentials lost. Please try restoring again.", isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to save session", isLoading = false) }
            }
        }
    }
}
