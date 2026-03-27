package com.tranzo.custody.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.Token
import com.tranzo.custody.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SendUiState(
    val portfolioTokens: List<Token> = emptyList(),
    val sendMessage: String? = null,
    val sendError: String? = null,
    val isSending: Boolean = false
)

@HiltViewModel
class SendViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SendUiState())
    val state: StateFlow<SendUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            walletRepository.getPortfolio().collect { p ->
                _state.value = _state.value.copy(portfolioTokens = p.tokens)
            }
        }
    }

    fun send(chain: Chain, to: String, amount: Double, token: Token, onDone: (Result<String>) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true, sendError = null, sendMessage = null)
            val result = walletRepository.sendTransaction(chain, to, amount, token)
            _state.value = _state.value.copy(isSending = false)
            result.onSuccess { h ->
                _state.value = _state.value.copy(sendMessage = "Submitted: $h")
                onDone(result)
            }.onFailure { e ->
                _state.value = _state.value.copy(sendError = e.message)
                onDone(result)
            }
        }
    }

    fun clearSendFeedback() {
        _state.value = _state.value.copy(sendMessage = null, sendError = null)
    }
}
