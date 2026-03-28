package com.tranzo.custody.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// ── Request / Response DTOs ──

data class NonceResponse(
    @SerializedName("nonce") val nonce: String
)

data class VerifyRequest(
    @SerializedName("message") val message: String,
    @SerializedName("signature") val signature: String
)

data class VerifyResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user") val user: AuthUser
)

data class AuthUser(
    @SerializedName("id") val id: String,
    @SerializedName("smartWalletAddr") val smartWalletAddr: String,
    @SerializedName("ownerAddr") val ownerAddr: String,
    @SerializedName("chainId") val chainId: Int
)

data class RefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class LogoutResponse(
    @SerializedName("message") val message: String
)

// ── Retrofit Interface ──

interface AuthApi {
    @GET("auth/nonce")
    suspend fun getNonce(): NonceResponse

    @POST("auth/verify")
    suspend fun verify(@Body request: VerifyRequest): VerifyResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse

    @POST("auth/logout")
    suspend fun logout(): LogoutResponse
}
