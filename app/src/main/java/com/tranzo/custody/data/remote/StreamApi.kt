package com.tranzo.custody.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

// ── DTOs ──

data class StreamDto(
    @SerializedName("id") val id: String,
    @SerializedName("onChainStreamId") val onChainStreamId: Int?,
    @SerializedName("recipientAddr") val recipientAddr: String,
    @SerializedName("token") val token: String,
    @SerializedName("totalAmount") val totalAmount: String,
    @SerializedName("withdrawn") val withdrawn: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("status") val status: String,
    @SerializedName("txHash") val txHash: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateStreamRequest(
    @SerializedName("recipientAddr") val recipientAddr: String,
    @SerializedName("token") val token: String,
    @SerializedName("totalAmount") val totalAmount: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("txHash") val txHash: String? = null
)

// ── API ──

interface StreamApi {
    @POST("streams")
    suspend fun createStream(@Body request: CreateStreamRequest): StreamDto

    @GET("streams/sent")
    suspend fun listSentStreams(): List<StreamDto>

    @GET("streams/received")
    suspend fun listReceivedStreams(): List<StreamDto>

    @GET("streams/{id}")
    suspend fun getStream(@Path("id") id: String): StreamDto

    @POST("streams/{id}/cancel")
    suspend fun cancelStream(@Path("id") id: String): StreamDto

    @POST("streams/{id}/pause")
    suspend fun pauseStream(@Path("id") id: String): StreamDto

    @POST("streams/{id}/resume")
    suspend fun resumeStream(@Path("id") id: String): StreamDto
}
