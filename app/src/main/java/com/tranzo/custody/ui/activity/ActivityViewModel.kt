package com.tranzo.custody.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.domain.model.Transaction
import com.tranzo.custody.domain.model.TransactionType
import com.tranzo.custody.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityUiState(
    val transactions: List<Transaction> = emptyList(),
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

enum class TransactionFilter(val label: String) {
    ALL("All"),
    SENT("Sent"),
    RECEIVED("Received"),
    SWAPPED("Swapped"),
    CARD_SPEND("Card")
}

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityUiState())
    val state: StateFlow<ActivityUiState> = _state.asStateFlow()

    init {
        loadTransactions()
        // Fetch latest transactions from backend
        viewModelScope.launch {
            try { transactionRepository.refreshTransactions() } catch (_: Exception) {}
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { txs ->
                _state.value = _state.value.copy(
                    transactions = txs,
                    isLoading = false
                )
            }
        }
    }

    fun setFilter(filter: TransactionFilter) {
        _state.value = _state.value.copy(selectedFilter = filter)
        viewModelScope.launch {
            val flow = when (filter) {
                TransactionFilter.ALL -> transactionRepository.getAllTransactions()
                TransactionFilter.SENT -> transactionRepository.getTransactionsByType(TransactionType.SENT)
                TransactionFilter.RECEIVED -> transactionRepository.getTransactionsByType(TransactionType.RECEIVED)
                TransactionFilter.SWAPPED -> transactionRepository.getTransactionsByType(TransactionType.SWAPPED)
                TransactionFilter.CARD_SPEND -> transactionRepository.getTransactionsByType(TransactionType.CARD_SPEND)
            }
            flow.collect { txs ->
                _state.value = _state.value.copy(transactions = txs)
            }
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        if (query.isBlank()) {
            loadTransactions()
        } else {
            viewModelScope.launch {
                transactionRepository.searchTransactions(query).collect { txs ->
                    _state.value = _state.value.copy(transactions = txs)
                }
            }
        }
    }

    suspend fun getTransactionDetail(id: String): Transaction? {
        return transactionRepository.getTransactionDetail(id)
    }
}
