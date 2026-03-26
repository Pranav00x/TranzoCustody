package com.tranzo.custody.data.repository

import com.tranzo.custody.domain.model.CardTransaction
import com.tranzo.custody.domain.model.CryptoCard
import com.tranzo.custody.domain.model.KycStatus
import com.tranzo.custody.domain.model.SpendMode
import com.tranzo.custody.domain.model.TransactionStatus
import com.tranzo.custody.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepositoryImpl @Inject constructor() : CardRepository {

    private val _card = MutableStateFlow(
        CryptoCard(
            id = "card_001",
            lastFourDigits = "5546",
            expiryDate = "09/28",
            cardholderName = "TRANZO USER",
            isFrozen = false,
            isVirtual = false,
            spendableBalance = 420.50,
            monthlySpent = 1240.80,
            monthlyLimit = 2000.0,
            dailyLimit = 500.0,
            onlineTransactionsEnabled = true,
            atmWithdrawalsEnabled = true,
            kycStatus = KycStatus.VERIFIED,
            spendMode = SpendMode.SPENDABLE_ONLY
        )
    )

    private val _transactions = MutableStateFlow(
        listOf(
            CardTransaction("ct_1", "Amazon", "A", 89.99, "USD", System.currentTimeMillis() - 3600000, TransactionStatus.CONFIRMED, "Card Balance"),
            CardTransaction("ct_2", "Starbucks", "S", 6.45, "USD", System.currentTimeMillis() - 7200000, TransactionStatus.CONFIRMED, "Card Balance"),
            CardTransaction("ct_3", "Shell Gas", "S", 52.30, "USD", System.currentTimeMillis() - 86400000, TransactionStatus.CONFIRMED, "Card Balance"),
            CardTransaction("ct_4", "Spotify", "S", 9.99, "USD", System.currentTimeMillis() - 172800000, TransactionStatus.CONFIRMED, "Card Balance"),
            CardTransaction("ct_5", "Uber Eats", "U", 24.50, "USD", System.currentTimeMillis() - 259200000, TransactionStatus.CONFIRMED, "Auto-converted ETH"),
            CardTransaction("ct_6", "Netflix", "N", 15.99, "USD", System.currentTimeMillis() - 345600000, TransactionStatus.CONFIRMED, "Card Balance"),
            CardTransaction("ct_7", "Apple Store", "A", 149.00, "USD", System.currentTimeMillis() - 432000000, TransactionStatus.CONFIRMED, "Card Balance")
        )
    )

    override fun getCard(): Flow<CryptoCard> = _card.asStateFlow()

    override fun getCardTransactions(): Flow<List<CardTransaction>> = _transactions.asStateFlow()

    override suspend fun freezeCard(freeze: Boolean) {
        _card.value = _card.value.copy(isFrozen = freeze)
    }

    override suspend fun setMonthlyLimit(limit: Double) {
        _card.value = _card.value.copy(monthlyLimit = limit)
    }

    override suspend fun setDailyLimit(limit: Double) {
        _card.value = _card.value.copy(dailyLimit = limit)
    }

    override suspend fun toggleOnlineTransactions(enabled: Boolean) {
        _card.value = _card.value.copy(onlineTransactionsEnabled = enabled)
    }

    override suspend fun toggleAtmWithdrawals(enabled: Boolean) {
        _card.value = _card.value.copy(atmWithdrawalsEnabled = enabled)
    }

    override suspend fun setSpendMode(mode: SpendMode) {
        _card.value = _card.value.copy(spendMode = mode)
    }

    override suspend fun addToSpendable(amount: Double) {
        _card.value = _card.value.copy(
            spendableBalance = _card.value.spendableBalance + amount
        )
    }
}
