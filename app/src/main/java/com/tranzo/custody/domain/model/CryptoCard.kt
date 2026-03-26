package com.tranzo.custody.domain.model

data class CryptoCard(
    val id: String,
    val lastFourDigits: String,
    val expiryDate: String,
    val cardholderName: String,
    val isFrozen: Boolean,
    val isVirtual: Boolean,
    val spendableBalance: Double,
    val monthlySpent: Double,
    val monthlyLimit: Double,
    val dailyLimit: Double,
    val onlineTransactionsEnabled: Boolean = true,
    val atmWithdrawalsEnabled: Boolean = true,
    val kycStatus: KycStatus = KycStatus.VERIFIED,
    val spendMode: SpendMode = SpendMode.SPENDABLE_ONLY
)

data class CardTransaction(
    val id: String,
    val merchantName: String,
    val merchantIcon: String,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val status: TransactionStatus,
    val sourceLabel: String = "Card Balance"
)

data class BridgePreview(
    val fromToken: Token,
    val fromAmount: Double,
    val toAmountFiat: Double,
    val exchangeRate: Double,
    val networkFee: Double,
    val platformFee: Double,
    val estimatedTotal: Double,
    val slippage: Double = 0.5
)
