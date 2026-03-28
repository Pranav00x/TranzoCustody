package com.tranzo.custody.data.remote

import com.google.gson.Gson
import com.tranzo.custody.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performs token refresh using a plain OkHttpClient (no auth interceptor)
 * to avoid the circular dependency: AuthInterceptor → Retrofit → AuthInterceptor.
 */
@Singleton
class TokenRefresherImpl @Inject constructor(
    private val gson: Gson
) : TokenRefresher {

    // Bare client with no interceptors — avoids re-triggering AuthInterceptor
    private val plainClient = OkHttpClient.Builder().build()

    override suspend fun refresh(refreshToken: String): RefreshResponse? {
        val base = BuildConfig.WALLET_BACKEND_URL.trimEnd('/') + "/"
        val url = "${base}auth/refresh"

        val jsonBody = gson.toJson(RefreshRequest(refreshToken))
        val body = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            val response = plainClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { gson.fromJson(it, RefreshResponse::class.java) }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
