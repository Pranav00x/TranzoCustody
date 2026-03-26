package com.tranzo.custody.domain.repository

import com.tranzo.custody.domain.model.CardTransaction
import com.tranzo.custody.domain.model.CryptoCard
import com.tranzo.custody.domain.model.KycStatus
import com.tranzo.custody.domain.model.SpendMode
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCard(): Flow<CryptoCard>
    fun getCardTransactions(): Flow<List<CardTransaction>>
    suspend fun freezeCard(freeze: Boolean)
    suspend fun setMonthlyLimit(limit: Double)
    suspend fun setDailyLimit(limit: Double)
    suspend fun toggleOnlineTransactions(enabled: Boolean)
    suspend fun toggleAtmWithdrawals(enabled: Boolean)
    suspend fun setSpendMode(mode: SpendMode)
    suspend fun addToSpendable(amount: Double)
}
