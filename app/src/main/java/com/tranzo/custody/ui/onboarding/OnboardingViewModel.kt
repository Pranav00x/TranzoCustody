package com.tranzo.custody.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.BuildConfig
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.data.repository.AuthRepository
import com.tranzo.custody.web3.MnemonicManager
import com.tranzo.custody.web3.SigningManager
import com.tranzo.custody.web3.SmartAccountManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.math.BigInteger
import javax.inject.Inject

enum class OnboardingMode { CREATE, IMPORT }

data class SeedChallenge(val wordIndex: Int, val choices: List<String>)

data class OnboardingState(
    val mode: OnboardingMode = OnboardingMode.CREATE,
    val mnemonic: String = "",
    val importMnemonicInput: String = "",
    val challenges: List<SeedChallenge> = emptyList(),
    val verificationPicks: Map<Int, String> = emptyMap(),
    val pin: String = "",
    val confirmPin: String = "",
    val isSettingPin: Boolean = true,
    val error: String? = null,
    val isLoading: Boolean = false,
    val walletSetupComplete: Boolean = false,
    val setupError: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val mnemonicManager: MnemonicManager,
    private val signingManager: SigningManager,
    private val smartAccountManager: SmartAccountManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    companion object {
        private const val SALT = 1L
    }

    fun setMode(mode: OnboardingMode) {
        _state.value = _state.value.copy(mode = mode, error = null)
    }

    fun generateNewMnemonic() {
        val m = mnemonicManager.generateMnemonic()
        _state.value = _state.value.copy(mnemonic = m, error = null)
    }

    fun setImportMnemonic(text: String) {
        _state.value = _state.value.copy(importMnemonicInput = text, error = null)
    }

    fun validateImportMnemonic(): Boolean {
        val t = _state.value.importMnemonicInput
        if (!mnemonicManager.validateMnemonic(t)) {
            _state.value = _state.value.copy(error = "Invalid recovery phrase. Check the words and try again.")
            return false
        }
        _state.value = _state.value.copy(
            mnemonic = mnemonicManager.normalizeMnemonic(t),
            error = null
        )
        return true
    }

    fun buildVerificationChallenges() {
        val phrase = _state.value.mnemonic
        if (phrase.isBlank()) return
        val words = phrase.split(" ")
        if (words.size != 12) return
        val pickIndices = (0 until 12).shuffled().take(3)
        val challenges = pickIndices.map { idx ->
            val correct = words[idx]
            val wrongPool = words.filterIndexed { i, w -> i != idx && w != correct }.shuffled().take(3)
            val choices = (wrongPool + correct).shuffled()
            SeedChallenge(idx, choices)
        }
        _state.value = _state.value.copy(challenges = challenges, verificationPicks = emptyMap(), error = null)
    }

    fun setVerificationPick(wordIndex: Int, word: String) {
        val picks = _state.value.verificationPicks.toMutableMap()
        picks[wordIndex] = word
        _state.value = _state.value.copy(verificationPicks = picks, error = null)
    }

    fun verificationSatisfied(): Boolean {
        val words = _state.value.mnemonic.split(" ")
        if (words.size != 12) return false
        return _state.value.challenges.all { ch ->
            _state.value.verificationPicks[ch.wordIndex] == words[ch.wordIndex]
        }
    }

    fun onPinDigit(digit: Int) {
        val s = _state.value
        if (s.isLoading) return
        if (s.isSettingPin) {
            if (s.pin.length >= 6) return
            val newPin = s.pin + digit
            val next = s.copy(pin = newPin, error = null)
            _state.value = if (newPin.length == 6) next.copy(isSettingPin = false) else next
        } else {
            if (s.confirmPin.length >= 6) return
            val newC = s.confirmPin + digit
            _state.value = s.copy(confirmPin = newC, error = null)
            if (newC.length == 6) {
                if (s.pin == newC) {
                    finalizeWalletSetup(s.pin)
                } else {
                    _state.value = s.copy(
                        confirmPin = "",
                        error = "PINs do not match. Try again."
                    )
                }
            }
        }
    }

    fun onPinDelete() {
        val s = _state.value
        if (s.isSettingPin) {
            if (s.pin.isNotEmpty()) _state.value = s.copy(pin = s.pin.dropLast(1))
        } else {
            if (s.confirmPin.isNotEmpty()) {
                _state.value = s.copy(confirmPin = s.confirmPin.dropLast(1))
            } else {
                _state.value = s.copy(isSettingPin = true, pin = "")
            }
        }
    }

    private fun finalizeWalletSetup(pin: String) {
        val mnemonic = _state.value.mnemonic
        if (mnemonic.isBlank()) {
            _state.value = _state.value.copy(setupError = "Missing recovery phrase")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, setupError = null, error = null)
            try {
                withContext(Dispatchers.IO) {
                    val creds = mnemonicManager.deriveCredentials(mnemonic)
                    val predicted = smartAccountManager.computeCounterfactualAddress(
                        creds.address,
                        BigInteger.valueOf(SALT)
                    )

                    // Authenticate via SIWE — backend creates user + returns JWT tokens
                    val smart = try {
                        val authUser = authRepository.signIn(
                            credentials = creds,
                            chainId = BuildConfig.DEFAULT_CHAIN_ID
                        )
                        authUser.smartWalletAddr.lowercase().takeIf { it.isNotBlank() } ?: predicted
                    } catch (_: Exception) {
                        // Auth failed — continue with locally computed address
                        predicted
                    }

                    signingManager.persistCredentials(creds)
                    sessionManager.saveWalletSession(
                        ownerAddress = creds.address,
                        smartWalletAddress = smart,
                        chainId = BuildConfig.DEFAULT_CHAIN_ID,
                        pin = pin
                    )
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    walletSetupComplete = true,
                    mnemonic = "",
                    pin = "",
                    confirmPin = ""
                )
            } catch (e: Exception) {
                val chain = sequenceOf(e, e.cause).mapNotNull { it?.message }.joinToString("\n")
                val setupError = when {
                    chain.contains("Cleartext", ignoreCase = true) ->
                        "HTTP to the dev server was blocked. Rebuild the app or use HTTPS."
                    e is IOException || e.cause is IOException ->
                        "Network issue: ${e.message ?: e.cause?.message ?: e.javaClass.simpleName}"
                    e.message?.contains("Could not reach Polygon Amoy", ignoreCase = true) == true ->
                        e.message!!
                    e.message?.isNotBlank() == true -> e.message!!
                    else -> "Could not finish setup: ${e.javaClass.simpleName}"
                }
                _state.value = _state.value.copy(isLoading = false, setupError = setupError)
            }
        }
    }

    
    fun submitVerification(onSuccess: () -> Unit) {
        if (!verificationSatisfied()) {
            _state.value = _state.value.copy(error = "One or more words are incorrect.")
            return
        }
        _state.value = _state.value.copy(error = null)
        onSuccess()
    }

    fun consumeSetupError() {
        _state.value = _state.value.copy(setupError = null)
    }

    fun resetAfterFailedSetup() {
        _state.value = _state.value.copy(
            isLoading = false,
            pin = "",
            confirmPin = "",
            isSettingPin = true,
            setupError = null
        )
    }

    suspend fun markSeedBackedUp() {
        sessionManager.markSeedBackedUp()
    }

    fun resetWallet(onComplete: () -> Unit) {
        viewModelScope.launch {
            signingManager.clearWalletKeys()
            sessionManager.clearSession()
            onComplete()
        }
    }
}

