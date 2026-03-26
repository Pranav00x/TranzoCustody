package com.tranzo.custody.domain.model

data class Wallet(
    val address: String,
    val chain: Chain,
    val balanceFiat: Double,
    val isDefault: Boolean = false
)

data class WalletPortfolio(
    val totalBalanceFiat: Double,
    val dailyChangePercent: Double,
    val dailyChangeAmount: Double,
    val tokens: List<Token>,
    val wallets: List<Wallet>
)
