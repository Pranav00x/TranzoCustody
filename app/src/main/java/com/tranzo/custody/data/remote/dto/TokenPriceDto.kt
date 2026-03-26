package com.tranzo.custody.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TokenPriceDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: Double,
    @SerializedName("change_24h") val change24h: Double
)

data class BalanceResponseDto(
    @SerializedName("address") val address: String,
    @SerializedName("balances") val balances: List<TokenBalanceDto>
)

data class TokenBalanceDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("balance") val balance: String,
    @SerializedName("decimals") val decimals: Int
)
