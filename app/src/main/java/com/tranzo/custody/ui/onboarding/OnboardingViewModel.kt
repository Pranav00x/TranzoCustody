package com.tranzo.custody.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.data.local.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    // Sign Up fields
    val email: String = "",
    val fullName: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val isSettingPin: Boolean = true,
    val error: String? = null,
    val isLoading: Boolean = false,
    /** Set when confirm PIN matches; navigation runs once via LaunchedEffect */
    val pinConfirmed: Boolean = false,

    // Sign In fields
    val signInEmail: String = "",
    val signInPin: String = "",
    val signInError: String? = null,
    val signInSuccess: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    // ─── Sign Up ─────────────────────────────────────────────

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
            if (newConfirm.length == 6) {
                if (_state.value.pin == newConfirm) {
                    _state.value = _state.value.copy(pinConfirmed = true)
                    // Save session when PIN is confirmed
                    saveSession()
                } else {
                    _state.value = _state.value.copy(
                        confirmPin = "",
                        error = "PINs don't match. Try again."
                    )
                }
            }
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

    private fun saveSession() {
        viewModelScope.launch {
            sessionManager.saveSession(
                name = _state.value.fullName.trim(),
                email = _state.value.email.trim(),
                pin = _state.value.pin
            )
        }
    }

    // ─── Sign In ─────────────────────────────────────────────

    fun setSignInEmail(email: String) {
        _state.value = _state.value.copy(signInEmail = email, signInError = null)
    }

    fun setSignInPin(pin: String) {
        _state.value = _state.value.copy(signInPin = pin, signInError = null)
    }

    fun signIn(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, signInError = null)

            val savedEmail = sessionManager.getSavedEmail()
            val emailMatches = savedEmail.equals(_state.value.signInEmail.trim(), ignoreCase = true)
            val pinMatches = sessionManager.verifyPin(_state.value.signInPin)

            if (savedEmail.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    signInError = "No account found. Please sign up first."
                )
                return@launch
            }

            if (!emailMatches) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    signInError = "Email does not match. Please try again."
                )
                return@launch
            }

            if (!pinMatches) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    signInError = "Incorrect PIN. Please try again."
                )
                return@launch
            }

            // Mark as logged in again (in case session was cleared)
            sessionManager.saveSession(
                name = sessionManager.getSavedName(),
                email = savedEmail,
                pin = _state.value.signInPin
            )

            _state.value = _state.value.copy(isLoading = false, signInSuccess = true)
            onSuccess()
        }
    }

    // ─── Logout ─────────────────────────────────────────────

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onComplete()
        }
    }
}
