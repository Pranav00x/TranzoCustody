package com.tranzo.custody.data.remote

import com.tranzo.custody.data.local.UserSessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that:
 * 1. Attaches the Bearer access token to every request (if available)
 * 2. On 401 responses, attempts a single token refresh and retries the original request
 *
 * Endpoints under /auth/nonce, /auth/verify, /auth/refresh are excluded from token injection
 * to avoid circular dependencies.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val tokenRefresher: TokenRefresher
) : Interceptor {

    companion object {
        private val EXCLUDED_PATHS = listOf("/auth/nonce", "/auth/verify", "/auth/refresh")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Skip auth header for public auth endpoints
        if (EXCLUDED_PATHS.any { path.endsWith(it) }) {
            return chain.proceed(originalRequest)
        }

        // Attach access token
        val accessToken = runBlocking { sessionManager.getAccessToken() }
        val authenticatedRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // If 401 and we have a refresh token, try to refresh
        if (response.code == 401) {
            val refreshToken = runBlocking { sessionManager.getRefreshToken() } ?: return response

            val newTokens = runBlocking { tokenRefresher.refresh(refreshToken) }
            if (newTokens != null) {
                // Save new tokens
                runBlocking {
                    sessionManager.updateTokens(newTokens.accessToken, newTokens.refreshToken)
                }

                // Close old response and retry with new token
                response.close()
                val retriedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
                return chain.proceed(retriedRequest)
            }
        }

        return response
    }
}

/**
 * Isolated token refresh interface to avoid circular dependency with Retrofit.
 * The implementation makes a direct OkHttp call to /auth/refresh.
 */
interface TokenRefresher {
    suspend fun refresh(refreshToken: String): RefreshResponse?
}
