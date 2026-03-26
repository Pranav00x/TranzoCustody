package com.tranzo.custody.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.security.SeedPhraseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val seedPhrase: List<String> = emptyList(),
    val verificationIndices: List<Int> = emptyList(),
    val verificationAnswers: Map<Int, String> = emptyMap(),
    val pin: String = "",
    val confirmPin: String = "",
    val isSettingPin: Boolean = true,
    val error: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val seedPhraseManager: SeedPhraseManager
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun generateSeedPhrase() {
        val phrase = seedPhraseManager.generateSeedPhrase()
        val indices = seedPhraseManager.getVerificationIndices()
        _state.value = _state.value.copy(
            seedPhrase = phrase,
            verificationIndices = indices
        )
    }

    fun setVerificationAnswer(index: Int, word: String) {
        val answers = _state.value.verificationAnswers.toMutableMap()
        answers[index] = word.lowercase().trim()
        _state.value = _state.value.copy(verificationAnswers = answers)
    }

    fun verifySeedPhrase(): Boolean {
        val phrase = _state.value.seedPhrase
        val answers = _state.value.verificationAnswers
        val indices = _state.value.verificationIndices
        return indices.all { idx ->
            answers[idx]?.equals(phrase[idx], ignoreCase = true) == true
        }
    }

    fun validateImportedSeed(words: List<String>): Boolean {
        return seedPhraseManager.validateSeedPhrase(words)
    }

    fun importSeedPhrase(words: List<String>) {
        _state.value = _state.value.copy(seedPhrase = words)
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
            viewModelScope.launch {
                seedPhraseManager.storeSeedPhrase(_state.value.seedPhrase)
            }
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
