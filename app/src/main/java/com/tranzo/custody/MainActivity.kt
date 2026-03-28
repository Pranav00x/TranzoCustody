package com.tranzo.custody

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.data.repository.AuthRepository
import com.tranzo.custody.navigation.Screen
import com.tranzo.custody.navigation.TranzoNavigation
import com.tranzo.custody.ui.theme.TranzoTheme
import com.tranzo.custody.ui.theme.White
import com.tranzo.custody.web3.SigningManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: UserSessionManager

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var signingManager: SigningManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TranzoTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    if (sessionManager.hasWallet()) {
                        // Silent re-auth if tokens are missing
                        if (!sessionManager.isAuthenticated()) {
                            tryReAuth()
                        }
                        startDestination = Screen.Home.route
                    } else {
                        startDestination = Screen.Welcome.route
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = White
                ) {
                    if (startDestination != null) {
                        TranzoNavigation(startDestination = startDestination!!)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { }
                    }
                }
            }
        }
    }

    /**
     * Attempt silent re-authentication using stored credentials.
     * If the user has a wallet but no valid tokens, re-sign SIWE
     * to get fresh JWT tokens without requiring user interaction.
     */
    private suspend fun tryReAuth() {
        try {
            val creds = signingManager.loadCredentials() ?: return
            val chainId = sessionManager.getChainId().takeIf { it > 0 }
                ?: BuildConfig.DEFAULT_CHAIN_ID
            authRepository.signIn(creds, chainId)
        } catch (_: Exception) {
            // Best-effort — app still works for read-only operations without auth
        }
    }
}
