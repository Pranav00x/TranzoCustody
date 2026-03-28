package com.tranzo.custody.data.repository

import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.data.remote.AuthApi
import com.tranzo.custody.data.remote.AuthUser
import com.tranzo.custody.data.remote.VerifyRequest
import com.tranzo.custody.web3.SiweManager
import org.web3j.crypto.Credentials
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the full SIWE authentication flow:
 *   1. Request nonce from backend
 *   2. Build + sign SIWE message on device
 *   3. Submit to backend, receive JWT tokens
 *   4. Persist tokens locally
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val siweManager: SiweManager,
    private val sessionManager: UserSessionManager
) {

    /**
     * Full sign-in flow. Call after wallet creation/import.
     * Returns the authenticated user data from the backend.
     */
    suspend fun signIn(credentials: Credentials, chainId: Int): AuthUser {
        // 1. Get nonce
        val nonceResponse = authApi.getNonce()

        // 2. Build SIWE message
        val address = credentials.address
        val message = siweManager.buildMessage(
            address = address,
            chainId = chainId,
            nonce = nonceResponse.nonce
        )

        // 3. Sign message
        val signature = siweManager.signMessage(message, credentials)

        // 4. Verify with backend
        val verifyResponse = authApi.verify(VerifyRequest(message = message, signature = signature))

        // 5. Persist tokens
        sessionManager.saveAuthTokens(
            accessToken = verifyResponse.accessToken,
            refreshToken = verifyResponse.refreshToken,
            userId = verifyResponse.user.id
        )

        return verifyResponse.user
    }

    /**
     * Logout — revoke all tokens on backend + clear local storage.
     */
    suspend fun logout() {
        try {
            authApi.logout()
        } catch (_: Exception) {
            // Best-effort — clear locally even if server call fails
        }
        sessionManager.clearAuthTokens()
    }

    /**
     * Check if we have stored auth tokens.
     */
    suspend fun isAuthenticated(): Boolean = sessionManager.isAuthenticated()
}
