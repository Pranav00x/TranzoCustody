package com.tranzo.custody.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.domain.model.CardTransaction
import com.tranzo.custody.domain.model.CryptoCard
import com.tranzo.custody.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardUiState(
    val card: CryptoCard? = null,
    val transactions: List<CardTransaction> = emptyList(),
    val isLoading: Boolean = true,
    val showDetails: Boolean = false
)

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CardUiState())
    val state: StateFlow<CardUiState> = _state.asStateFlow()

    init {
        loadCard()
        loadTransactions()
    }

    private fun loadCard() {
        viewModelScope.launch {
            cardRepository.getCard().collect { card ->
                _state.value = _state.value.copy(card = card, isLoading = false)
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            cardRepository.getCardTransactions().collect { txs ->
                _state.value = _state.value.copy(transactions = txs)
            }
        }
    }

    fun toggleFreeze() {
        viewModelScope.launch {
            _state.value.card?.let { card ->
                cardRepository.freezeCard(!card.isFrozen)
            }
        }
    }

    fun toggleShowDetails() {
        _state.value = _state.value.copy(showDetails = !_state.value.showDetails)
    }

    fun setMonthlyLimit(limit: Double) {
        viewModelScope.launch { cardRepository.setMonthlyLimit(limit) }
    }

    fun setDailyLimit(limit: Double) {
        viewModelScope.launch { cardRepository.setDailyLimit(limit) }
    }

    fun toggleOnlineTransactions(enabled: Boolean) {
        viewModelScope.launch { cardRepository.toggleOnlineTransactions(enabled) }
    }

    fun toggleAtmWithdrawals(enabled: Boolean) {
        viewModelScope.launch { cardRepository.toggleAtmWithdrawals(enabled) }
    }
}
