package com.tranzo.custody.domain.model

data class CryptoCard(
    val id: String,
    val lastFourDigits: String,
    val expiryDate: String,
    val cardholderName: String,
    val isFrozen: Boolean,
    val isVirtual: Boolean,
    val monthlySpent: Double,
    val monthlyLimit: Double,
    val dailyLimit: Double,
    val onlineTransactionsEnabled: Boolean = true,
    val atmWithdrawalsEnabled: Boolean = true
)

data class CardTransaction(
    val id: String,
    val merchantName: String,
    val merchantIcon: String,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val status: TransactionStatus
)
