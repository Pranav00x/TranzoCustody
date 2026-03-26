package com.tranzo.custody.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.domain.model.BridgePreview
import com.tranzo.custody.domain.model.Token
import com.tranzo.custody.domain.repository.CardRepository
import com.tranzo.custody.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BridgeUiState(
    val tokens: List<Token> = emptyList(),
    val selectedToken: Token? = null,
    val amount: String = "",
    val preview: BridgePreview? = null,
    val isLoadingPreview: Boolean = false,
    val isProcessing: Boolean = false,
    val success: Boolean = false,
    val creditedAmount: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class BridgeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BridgeUiState())
    val state: StateFlow<BridgeUiState> = _state.asStateFlow()

    init {
        loadTokens()
    }

    private fun loadTokens() {
        viewModelScope.launch {
            walletRepository.getTokens().collect { tokens ->
                _state.value = _state.value.copy(
                    tokens = tokens,
                    selectedToken = tokens.firstOrNull()
                )
            }
        }
    }

    fun selectToken(token: Token) {
        _state.value = _state.value.copy(selectedToken = token, preview = null)
        if (_state.value.amount.isNotBlank()) {
            loadPreview()
        }
    }

    fun setAmount(amount: String) {
        _state.value = _state.value.copy(amount = amount, preview = null, error = null)
        val parsed = amount.toDoubleOrNull()
        if (parsed != null && parsed > 0 && _state.value.selectedToken != null) {
            if (parsed > (_state.value.selectedToken?.balance ?: 0.0)) {
                _state.value = _state.value.copy(error = "Insufficient balance")
            } else {
                loadPreview()
            }
        }
    }

    private fun loadPreview() {
        val token = _state.value.selectedToken ?: return
        val amount = _state.value.amount.toDoubleOrNull() ?: return
        _state.value = _state.value.copy(isLoadingPreview = true)
        viewModelScope.launch {
            val preview = walletRepository.getBridgePreview(token, amount)
            _state.value = _state.value.copy(preview = preview, isLoadingPreview = false)
        }
    }

    fun executeTopUp() {
        val token = _state.value.selectedToken ?: return
        val amount = _state.value.amount.toDoubleOrNull() ?: return
        _state.value = _state.value.copy(isProcessing = true, error = null)
        viewModelScope.launch {
            walletRepository.executeTopUp(token, amount)
                .onSuccess { credited ->
                    cardRepository.addToSpendable(credited)
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        success = true,
                        creditedAmount = credited
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        error = e.message ?: "Top-up failed"
                    )
                }
        }
    }

    fun reset() {
        _state.value = _state.value.copy(
            amount = "",
            preview = null,
            success = false,
            creditedAmount = 0.0,
            error = null
        )
    }
}
