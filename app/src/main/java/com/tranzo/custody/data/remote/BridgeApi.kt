package com.tranzo.custody.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class BridgeQuoteRequest(
    @SerializedName("fromChainId") val fromChainId: Int,
    @SerializedName("toChainId") val toChainId: Int,
    @SerializedName("fromToken") val fromToken: String,
    @SerializedName("toToken") val toToken: String,
    @SerializedName("amount") val amount: String
)

data class BridgeQuoteResponse(
    @SerializedName("fromChainId") val fromChainId: Int,
    @SerializedName("toChainId") val toChainId: Int,
    @SerializedName("fromAmount") val fromAmount: String,
    @SerializedName("toAmount") val toAmount: String,
    @SerializedName("bridgeName") val bridgeName: String,
    @SerializedName("estimatedTime") val estimatedTime: Int,
    @SerializedName("fee") val fee: String,
    @SerializedName("callData") val callData: String,
    @SerializedName("to") val to: String,
    @SerializedName("value") val value: String
)

interface BridgeApi {
    @POST("bridge/quote")
    suspend fun getQuote(@Body request: BridgeQuoteRequest): BridgeQuoteResponse
}
