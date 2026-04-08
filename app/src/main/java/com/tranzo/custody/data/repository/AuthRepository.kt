package com.tranzo.custody.data.repository

import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.data.remote.AuthApi
import com.tranzo.custody.data.remote.AuthUser
import com.tranzo.custody.data.remote.ForgotPasswordRequest
import com.tranzo.custody.data.remote.LoginRequest
import com.tranzo.custody.data.remote.MessageResponse
import com.tranzo.custody.data.remote.OtpSendRequest
import com.tranzo.custody.data.remote.OtpVerifyRequest
import com.tranzo.custody.data.remote.ResetPasswordRequest
import com.tranzo.custody.data.remote.SignupRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates email/password authentication:
 *   1. Signup — create account with email + password + wallet address
 *   2. Login — authenticate with email + password
 *   3. Password recovery — forgot/reset password
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: UserSessionManager
) {

    /**
     * Sign up with email/password. Call after wallet creation.
     * The ownerAddr is derived locally from the mnemonic.
     */
    suspend fun signup(
        email: String,
        password: String,
        ownerAddr: String,
        chainId: Int
    ): AuthUser {
        val response = authApi.signup(
            SignupRequest(
                email = email,
                password = password,
                ownerAddr = ownerAddr,
                chainId = chainId
            )
        )

        sessionManager.saveAuthTokens(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.user.id
        )

        return response.user
    }

    /**
     * Log in with email/password. Returns user data.
     */
    suspend fun login(email: String, password: String): AuthUser {
        val response = authApi.login(
            LoginRequest(email = email, password = password)
        )

        sessionManager.saveAuthTokens(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.user.id
        )

        return response.user
    }

    /**
     * Request a password reset email.
     */
    suspend fun forgotPassword(email: String): String {
        val response = authApi.forgotPassword(ForgotPasswordRequest(email = email))
        return response.message
    }

    /**
     * Reset password with a token from the reset email.
     */
    suspend fun resetPassword(token: String, newPassword: String): String {
        val response = authApi.resetPassword(
            ResetPasswordRequest(token = token, newPassword = newPassword)
        )
        return response.message
    }

    /**
     * Log in with a Google ID token.
     */
    suspend fun googleLogin(idToken: String, ownerAddr: String, chainId: Int): AuthUser {
        val response = authApi.googleLogin(
            GoogleLoginRequest(idToken = idToken, ownerAddr = ownerAddr, chainId = chainId)
        )
        sessionManager.saveAuthTokens(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.user.id
        )
        return response.user
    }

    /**
     * Signup via OAuth or Passkey.
     */
    suspend fun oauthSignup(
        email: String,
        googleId: String? = null,
        publicKey: String? = null,
        ownerAddr: String,
        chainId: Int,
        emailVerified: Boolean? = null
    ): AuthUser {
        val response = authApi.oauthSignup(
            OAuthSignupRequest(
                email = email,
                googleId = googleId,
                publicKey = publicKey,
                ownerAddr = ownerAddr,
                chainId = chainId,
                emailVerified = emailVerified
            )
        )
        sessionManager.saveAuthTokens(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.user.id
        )
        return response.user
    }

    /**
     * Send an OTP to the given email.
     */
    suspend fun sendOtp(email: String): String {
        return authApi.sendOtp(OtpSendRequest(email = email)).message
    }

    /**
     * Verify an OTP.
     */
    suspend fun verifyOtp(email: String, otp: String): String {
        return authApi.verifyOtp(OtpVerifyRequest(email = email, otp = otp)).message
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
