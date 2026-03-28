package com.tranzo.custody.data.repository

import com.tranzo.custody.data.remote.CardApi
import com.tranzo.custody.data.remote.CardDto
import com.tranzo.custody.data.remote.CreateCardRequest
import com.tranzo.custody.data.remote.UpdateLimitRequest
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
class CardRepositoryImpl @Inject constructor(
    private val cardApi: CardApi
) : CardRepository {

    private val _card = MutableStateFlow(emptyCard())
    private val _transactions = MutableStateFlow<List<CardTransaction>>(emptyList())

    override fun getCard(): Flow<CryptoCard> = _card.asStateFlow()
    override fun getCardTransactions(): Flow<List<CardTransaction>> = _transactions.asStateFlow()

    override suspend fun freezeCard(freeze: Boolean) {
        val card = _card.value
        try {
            val updated = if (freeze) {
                cardApi.freezeCard(card.id)
            } else {
                cardApi.unfreezeCard(card.id)
            }
            _card.value = updated.toDomain()
        } catch (e: Exception) {
            // Optimistic update fallback
            _card.value = card.copy(isFrozen = freeze)
        }
    }

    override suspend fun setMonthlyLimit(limit: Double) {
        _card.value = _card.value.copy(monthlyLimit = limit)
    }

    override suspend fun setDailyLimit(limit: Double) {
        val card = _card.value
        try {
            // Convert dollar amount to USDC (6 decimals)
            val limitUsdc = (limit * 1_000_000).toLong().toString()
            val updated = cardApi.updateLimit(card.id, UpdateLimitRequest(limitUsdc))
            _card.value = updated.toDomain()
        } catch (e: Exception) {
            _card.value = card.copy(dailyLimit = limit)
        }
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

    /**
     * Fetch cards from backend and update local state.
     * Called after authentication.
     */
    suspend fun refreshCards() {
        try {
            val cards = cardApi.listCards()
            if (cards.isNotEmpty()) {
                _card.value = cards.first().toDomain()
            }
        } catch (_: Exception) {
            // Keep current state on failure
        }
    }

    /**
     * Create a new virtual card via the backend.
     */
    suspend fun createCard(): CryptoCard {
        val dto = cardApi.createCard(CreateCardRequest())
        val card = dto.toDomain()
        _card.value = card
        return card
    }

    private fun CardDto.toDomain(): CryptoCard {
        val limitUsd = dailyLimit.toLongOrNull()?.let { it / 1_000_000.0 } ?: 500.0
        return CryptoCard(
            id = id,
            lastFourDigits = last4 ?: "****",
            expiryDate = "",
            cardholderName = "TRANZO USER",
            isFrozen = status == "FROZEN",
            isVirtual = type == "VIRTUAL",
            spendableBalance = 0.0,
            monthlySpent = 0.0,
            monthlyLimit = 2000.0,
            dailyLimit = limitUsd,
            onlineTransactionsEnabled = status == "ACTIVE",
            atmWithdrawalsEnabled = status == "ACTIVE",
            kycStatus = KycStatus.NOT_STARTED,
            spendMode = SpendMode.SPENDABLE_ONLY
        )
    }

    private fun emptyCard() = CryptoCard(
        id = "",
        lastFourDigits = "----",
        expiryDate = "--/--",
        cardholderName = "TRANZO USER",
        isFrozen = false,
        isVirtual = true,
        spendableBalance = 0.0,
        monthlySpent = 0.0,
        monthlyLimit = 2000.0,
        dailyLimit = 500.0
    )
}
