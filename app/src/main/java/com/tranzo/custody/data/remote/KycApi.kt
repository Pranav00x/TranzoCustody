package com.tranzo.custody.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class KycStatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("provider") val provider: String?
)

data class KycStartRequest(
    @SerializedName("provider") val provider: String = "immersve"
)

data class KycStartResponse(
    @SerializedName("status") val status: String,
    @SerializedName("provider") val provider: String?,
    @SerializedName("sessionUrl") val sessionUrl: String? = null,
    @SerializedName("sessionId") val sessionId: String? = null
)

interface KycApi {
    @GET("kyc/status")
    suspend fun getStatus(): KycStatusResponse

    @POST("kyc/start")
    suspend fun start(@Body request: KycStartRequest = KycStartRequest()): KycStartResponse
}
