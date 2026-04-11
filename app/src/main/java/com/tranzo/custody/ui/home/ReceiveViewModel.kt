package com.tranzo.custody.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.data.local.TokenRegistry
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.domain.model.Token
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val tokenRegistry: TokenRegistry
) : ViewModel() {

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _tokens = MutableStateFlow<List<Token>>(emptyList())
    val tokens: StateFlow<List<Token>> = _tokens.asStateFlow()

    init {
        viewModelScope.launch {
            _address.value = sessionManager.getSmartWalletAddress()
            _tokens.value = tokenRegistry.getSupportedDepositTokens()
        }
    }
}
