package com.tranzo.custody

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tranzo.custody.data.backup.DriveBackupManager
import com.tranzo.custody.data.local.ThemePreferencesManager
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.navigation.Screen
import com.tranzo.custody.navigation.TranzoNavigation
import com.tranzo.custody.ui.theme.AppFontId
import com.tranzo.custody.ui.theme.AppThemeId
import com.tranzo.custody.ui.theme.TranzoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: UserSessionManager

    @Inject
    lateinit var driveBackupManager: DriveBackupManager

    @Inject
    lateinit var themePreferencesManager: ThemePreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTheme by themePreferencesManager.selectedTheme.collectAsState(initial = AppThemeId.MIDNIGHT)
            val selectedFont by themePreferencesManager.selectedFont.collectAsState(initial = AppFontId.INTER)

            TranzoTheme(themeId = selectedTheme, fontId = selectedFont) {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    startDestination = if (sessionManager.hasWallet()) {
                        Screen.VerifyPin.route
                    } else {
                        Screen.Welcome.route
                    }
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
}
