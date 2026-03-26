package com.tranzo.custody.domain.model

data class Wallet(
    val address: String,
    val chain: Chain,
    val balanceFiat: Double,
    val isDefault: Boolean = false
)

data class WalletPortfolio(
    val walletBalanceFiat: Double,
    val spendableBalanceFiat: Double,
    val dailyChangePercent: Double,
    val dailyChangeAmount: Double,
    val tokens: List<Token>,
    val wallets: List<Wallet>
) {
    val totalBalanceFiat: Double
        get() = walletBalanceFiat + spendableBalanceFiat
}

data class SpendableBalance(
    val amount: Double,
    val currency: String = "USD",
    val lastTopUpTimestamp: Long? = null
)

enum class SpendMode {
    SPENDABLE_ONLY,
    AUTO_CONVERT
}

enum class KycStatus {
    NOT_STARTED,
    PENDING,
    VERIFIED,
    REJECTED
}
