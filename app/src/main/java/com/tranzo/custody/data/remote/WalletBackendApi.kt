package com.tranzo.custody.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class CreateWalletRequest(
    @SerializedName("owner") val owner: String,
    @SerializedName("salt") val salt: Long,
    @SerializedName("chainId") val chainId: Int
)

data class CreateWalletResponse(
    @SerializedName("smartWalletAddr") val smartWalletAddr: String? = null,
    @SerializedName("ownerAddr") val ownerAddr: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("chainId") val chainId: Int? = null
)

data class BackendWalletResponse(
    val id: String? = null,
    val ownerAddr: String? = null,
    val smartWalletAddr: String? = null,
    val chainId: Int? = null
)

data class SendUserOpRequest(
    @SerializedName("chainId") val chainId: Int,
    @SerializedName("userOp") val userOp: Map<String, Any?>
)

data class SendUserOpResponse(
    @SerializedName("hash") val hash: String? = null,
    @SerializedName("error") val error: String? = null
)

interface WalletBackendApi {
    @POST("wallet/create")
    suspend fun registerWallet(@Body request: CreateWalletRequest): CreateWalletResponse

    @GET("wallet/details/{owner}")
    suspend fun getWalletDetails(@Path("owner") owner: String): BackendWalletResponse

    @POST("wallet/send-userop")
    suspend fun sendUserOperation(@Body body: SendUserOpRequest): SendUserOpResponse
}
