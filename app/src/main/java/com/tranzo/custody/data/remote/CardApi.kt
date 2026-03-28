package com.tranzo.custody.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

// ── DTOs ──

data class CardDto(
    @SerializedName("id") val id: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String,
    @SerializedName("last4") val last4: String?,
    @SerializedName("dailyLimit") val dailyLimit: String,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateCardRequest(
    @SerializedName("provider") val provider: String = "IMMERSVE",
    @SerializedName("type") val type: String = "VIRTUAL",
    @SerializedName("dailyLimit") val dailyLimit: String? = null
)

data class UpdateLimitRequest(
    @SerializedName("dailyLimit") val dailyLimit: String
)

data class CardSessionDto(
    @SerializedName("id") val id: String,
    @SerializedName("sessionKeyAddr") val sessionKeyAddr: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("dailyLimit") val dailyLimit: String,
    @SerializedName("perTxLimit") val perTxLimit: String,
    @SerializedName("validUntil") val validUntil: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

// ── API ──

interface CardApi {
    @GET("cards")
    suspend fun listCards(): List<CardDto>

    @POST("cards")
    suspend fun createCard(@Body request: CreateCardRequest): CardDto

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") id: String): CardDto

    @POST("cards/{id}/freeze")
    suspend fun freezeCard(@Path("id") id: String): CardDto

    @POST("cards/{id}/unfreeze")
    suspend fun unfreezeCard(@Path("id") id: String): CardDto

    @POST("cards/{id}/cancel")
    suspend fun cancelCard(@Path("id") id: String): CardDto

    @PATCH("cards/{id}/limit")
    suspend fun updateLimit(@Path("id") id: String, @Body request: UpdateLimitRequest): CardDto

    @GET("cards/sessions/active")
    suspend fun listActiveSessions(): List<CardSessionDto>
}
