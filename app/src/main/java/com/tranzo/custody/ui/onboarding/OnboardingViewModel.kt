package com.tranzo.custody.ui.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OnboardingState(
    val email: String = "",
    val fullName: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val isSettingPin: Boolean = true,
    val error: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun setFullName(name: String) {
        _state.value = _state.value.copy(fullName = name, error = null)
    }

    fun validateSignUp(): Boolean {
        val email = _state.value.email.trim()
        val name = _state.value.fullName.trim()
        if (name.length < 2) {
            _state.value = _state.value.copy(error = "Please enter your name")
            return false
        }
        if (!email.contains("@") || !email.contains(".")) {
            _state.value = _state.value.copy(error = "Please enter a valid email")
            return false
        }
        return true
    }

    fun onPinDigit(digit: Int) {
        val current = if (_state.value.isSettingPin) _state.value.pin else _state.value.confirmPin
        if (current.length >= 6) return

        if (_state.value.isSettingPin) {
            val newPin = _state.value.pin + digit
            _state.value = _state.value.copy(pin = newPin, error = null)
            if (newPin.length == 6) {
                _state.value = _state.value.copy(isSettingPin = false)
            }
        } else {
            val newConfirm = _state.value.confirmPin + digit
            _state.value = _state.value.copy(confirmPin = newConfirm, error = null)
        }
    }

    fun onPinDelete() {
        if (_state.value.isSettingPin) {
            if (_state.value.pin.isNotEmpty()) {
                _state.value = _state.value.copy(pin = _state.value.pin.dropLast(1))
            }
        } else {
            if (_state.value.confirmPin.isNotEmpty()) {
                _state.value = _state.value.copy(confirmPin = _state.value.confirmPin.dropLast(1))
            } else {
                _state.value = _state.value.copy(isSettingPin = true, pin = "")
            }
        }
    }

    fun confirmPin(): Boolean {
        return if (_state.value.pin == _state.value.confirmPin) {
            true
        } else {
            _state.value = _state.value.copy(
                confirmPin = "",
                error = "PINs don't match. Try again."
            )
            false
        }
    }
}
