package com.tranzo.custody.domain.repository

import com.tranzo.custody.domain.model.BridgePreview
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.domain.model.SpendableBalance
import com.tranzo.custody.domain.model.Token
import com.tranzo.custody.domain.model.WalletPortfolio
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getPortfolio(): Flow<WalletPortfolio>
    fun getTokens(): Flow<List<Token>>
    fun getSpendableBalance(): Flow<SpendableBalance>
    suspend fun getWalletAddress(chain: Chain): String
    suspend fun refreshBalances()
    suspend fun sendTransaction(chain: Chain, toAddress: String, amount: Double, token: Token): Result<String>
    suspend fun getBridgePreview(token: Token, amount: Double): BridgePreview
    suspend fun executeTopUp(token: Token, amount: Double): Result<Double>
}
