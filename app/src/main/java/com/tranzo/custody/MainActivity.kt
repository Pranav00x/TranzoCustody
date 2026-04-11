package com.tranzo.custody

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.tranzo.custody.data.backup.DriveBackupManager
import com.tranzo.custody.data.local.SecurityPreferencesManager
import com.tranzo.custody.data.local.ThemePreferencesManager
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.navigation.Screen
import com.tranzo.custody.navigation.TranzoNavigation
import com.tranzo.custody.ui.theme.AppFontId
import com.tranzo.custody.ui.theme.AppThemeId
import com.tranzo.custody.ui.theme.TranzoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.fragment.app.FragmentActivity

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionManager: UserSessionManager

    @Inject
    lateinit var driveBackupManager: DriveBackupManager

    @Inject
    lateinit var themePreferencesManager: ThemePreferencesManager

    @Inject
    lateinit var securityPrefs: SecurityPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTheme by themePreferencesManager.selectedTheme.collectAsState(initial = AppThemeId.MIDNIGHT)
            val selectedFont by themePreferencesManager.selectedFont.collectAsState(initial = AppFontId.INTER)

            TranzoTheme(themeId = selectedTheme, fontId = selectedFont) {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val hasWallet = sessionManager.hasWallet()
                    val isPinRequired = securityPrefs.isPinRequired.first()
                    val autoLockMin = securityPrefs.autoLockMinutes.first()
                    val lastActive = securityPrefs.getLastActiveTime()
                    val now = System.currentTimeMillis()

                    val shouldLock = hasWallet && isPinRequired && (autoLockMin > 0) && (now - lastActive > autoLockMin * 60 * 1000)

                    startDestination = if (hasWallet) {
                        if (shouldLock) Screen.VerifyPin.route else Screen.Home.route
                    } else {
                        Screen.Welcome.route
                    }
                }

                DisposableEffect(Unit) {
                    onDispose { }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (startDestination != null) {
                        TranzoNavigation(
                            startDestination = startDestination!!,
                            driveBackupManager = driveBackupManager,
                            sessionManager = sessionManager
                        )
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

    override fun onUserInteraction() {
        super.onUserInteraction()
        lifecycleScope.launch {
            securityPrefs.updateLastActiveTime()
        }
    }
}
