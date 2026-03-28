package com.tranzo.custody.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class BuyQuoteRequest(
    @SerializedName("fiatAmount") val fiatAmount: Double,
    @SerializedName("fiatCurrency") val fiatCurrency: String = "USD",
    @SerializedName("cryptoToken") val cryptoToken: String,
    @SerializedName("chainId") val chainId: Int
)

data class BuyQuoteResponse(
    @SerializedName("fiatAmount") val fiatAmount: Double,
    @SerializedName("fiatCurrency") val fiatCurrency: String,
    @SerializedName("cryptoAmount") val cryptoAmount: String,
    @SerializedName("cryptoToken") val cryptoToken: String,
    @SerializedName("exchangeRate") val exchangeRate: Double,
    @SerializedName("networkFee") val networkFee: Double,
    @SerializedName("processingFee") val processingFee: Double,
    @SerializedName("totalFiat") val totalFiat: Double,
    @SerializedName("provider") val provider: String
)

data class BuySessionRequest(
    @SerializedName("fiatAmount") val fiatAmount: Double,
    @SerializedName("fiatCurrency") val fiatCurrency: String = "USD",
    @SerializedName("cryptoToken") val cryptoToken: String,
    @SerializedName("chainId") val chainId: Int,
    @SerializedName("paymentMethod") val paymentMethod: String = "card"
)

data class BuySessionResponse(
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("widgetUrl") val widgetUrl: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("expiresAt") val expiresAt: String
)

interface BuyApi {
    @POST("buy/quote")
    suspend fun getQuote(@Body request: BuyQuoteRequest): BuyQuoteResponse

    @POST("buy/session")
    suspend fun createSession(@Body request: BuySessionRequest): BuySessionResponse
}
