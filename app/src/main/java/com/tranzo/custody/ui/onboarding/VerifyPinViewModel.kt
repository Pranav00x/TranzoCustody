package com.tranzo.custody.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.data.local.SecurityPreferencesManager
import com.tranzo.custody.data.local.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyPinState(
    val pin: String = "",
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val biometricEnabled: Boolean = false,
    val pinRequired: Boolean = true
)

@HiltViewModel
class VerifyPinViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val securityPrefs: SecurityPreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyPinState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                securityPrefs.isBiometricEnabled,
                securityPrefs.isPinRequired
            ) { bio, pinReq ->
                bio to pinReq
            }.collect { (bio, pinReq) ->
                _state.update { it.copy(biometricEnabled = bio, pinRequired = pinReq) }
                
                // If neither biometric nor PIN is required, just succeed
                if (!bio && !pinReq) {
                    _state.update { it.copy(isSuccess = true) }
                }
            }
        }
    }

    fun onBiometricSuccess() {
        _state.update { it.copy(isSuccess = true) }
    }

    fun addNumber(num: Int) {
        if (_state.value.pin.length < 6) {
            val nextPin = _state.value.pin + num
            _state.update { it.copy(pin = nextPin, error = null) }
            
            if (nextPin.length == 6) {
                verify(nextPin)
            }
        }
    }

    fun delete() {
        if (_state.value.pin.isNotEmpty()) {
            _state.update { it.copy(pin = it.pin.dropLast(1), error = null) }
        }
    }

    private fun verify(pin: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val isValid = sessionManager.verifyPin(pin)
            if (isValid) {
                _state.update { it.copy(isSuccess = true, isLoading = false) }
            } else {
                _state.update { it.copy(pin = "", error = "Incorrect PIN", isLoading = false) }
            }
        }
    }
}
