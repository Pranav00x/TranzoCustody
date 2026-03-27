package com.tranzo.custody.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.Token
import com.tranzo.custody.domain.model.WalletPortfolio
import com.tranzo.custody.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val portfolio: WalletPortfolio? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val smartWalletAddress: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadPortfolio()
        viewModelScope.launch {
            val addr = sessionManager.getSmartWalletAddress()
                .ifEmpty { walletRepository.getWalletAddress(Chain.POLYGON) }
            _state.value = _state.value.copy(smartWalletAddress = addr)
        }
    }

    private fun loadPortfolio() {
        viewModelScope.launch {
            walletRepository.getPortfolio().collect { portfolio ->
                _state.value = _state.value.copy(
                    portfolio = portfolio,
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            walletRepository.refreshBalances()
            val addr = sessionManager.getSmartWalletAddress()
                .ifEmpty { walletRepository.getWalletAddress(Chain.POLYGON) }
            _state.value = _state.value.copy(smartWalletAddress = addr)
        }
    }
}
