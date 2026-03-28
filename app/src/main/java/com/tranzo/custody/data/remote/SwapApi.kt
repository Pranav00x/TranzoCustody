package com.tranzo.custody.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class SwapQuoteRequest(
    @SerializedName("chainId") val chainId: Int,
    @SerializedName("fromToken") val fromToken: String,
    @SerializedName("toToken") val toToken: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("slippageBps") val slippageBps: Int = 50
)

data class SwapQuoteResponse(
    @SerializedName("fromToken") val fromToken: String,
    @SerializedName("toToken") val toToken: String,
    @SerializedName("fromAmount") val fromAmount: String,
    @SerializedName("toAmount") val toAmount: String,
    @SerializedName("exchangeRate") val exchangeRate: Double,
    @SerializedName("priceImpact") val priceImpact: Double,
    @SerializedName("gasEstimate") val gasEstimate: String,
    @SerializedName("route") val route: String,
    @SerializedName("callData") val callData: String,
    @SerializedName("to") val to: String,
    @SerializedName("value") val value: String
)

interface SwapApi {
    @POST("swap/quote")
    suspend fun getQuote(@Body request: SwapQuoteRequest): SwapQuoteResponse
}
