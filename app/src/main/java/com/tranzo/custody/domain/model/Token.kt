package com.tranzo.custody.domain.model

data class Token(
    val symbol: String,
    val name: String,
    val chain: Chain,
    val balance: Double,
    val fiatValue: Double,
    val priceChange24h: Double,
    val contractAddress: String? = null,
    val decimals: Int = 18,
    val iconColor: Long = 0xFF000000
)
