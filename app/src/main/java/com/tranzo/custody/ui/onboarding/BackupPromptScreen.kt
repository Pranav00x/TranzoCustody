package com.tranzo.custody.ui.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.tranzo.custody.data.backup.DriveBackupManager
import com.tranzo.custody.data.local.UserSessionManager
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.Negative
import com.tranzo.custody.ui.theme.Positive
import com.tranzo.custody.ui.theme.SurfaceSecondary
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.TextSecondary
import com.tranzo.custody.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun BackupPromptScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel,
    driveBackupManager: DriveBackupManager,
    sessionManager: UserSessionManager
) {
    val scope = rememberCoroutineScope()
    var isBackingUp by remember { mutableStateOf(false) }
    var backupSuccess by remember { mutableStateOf<Boolean?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val account = result.data?.let {
                GoogleSignIn.getSignedInAccountFromIntent(it).result
            }
            if (account != null) {
                scope.launch {
                    isBackingUp = true
                    errorMessage = null
                    try {
                        val state = viewModel.state.value
                        driveBackupManager.backup(
                            mnemonic = state.mnemonic.ifBlank {
                                // mnemonic may have been cleared after setup — skip if empty
                                throw IllegalStateException("Mnemonic no longer available")
                            },
                            password = state.password,
                            ownerAddr = sessionManager.getOwnerAddress() ?: "",
                            account = account
                        )
                        sessionManager.markSeedBackedUp()
                        backupSuccess = true
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Backup failed"
                        backupSuccess = false
                    } finally {
                        isBackingUp = false
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "Secure your wallet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Back up your recovery phrase so you can restore your wallet if you lose this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Google Drive backup option
        BackupOptionCard(
            icon = { Icon(Icons.Filled.Cloud, contentDescription = null, tint = Black, modifier = Modifier.size(28.dp)) },
            title = "Google Drive backup",
            subtitle = "Encrypted with your password. Automatic and secure.",
            isLoading = isBackingUp,
            isSuccess = backupSuccess == true,
            onClick = {
                if (!isBackingUp) {
                    val intent = driveBackupManager.getSignInIntent()
                    googleSignInLauncher.launch(intent)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recovery phrase option (already shown during onboarding)
        BackupOptionCard(
            icon = { Icon(Icons.Filled.Key, contentDescription = null, tint = Black, modifier = Modifier.size(28.dp)) },
            title = "Recovery phrase",
            subtitle = "You already wrote it down during setup.",
            isLoading = false,
            isSuccess = true,
            onClick = {
                scope.launch {
                    sessionManager.markSeedBackedUp()
                }
            }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = Negative
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White),
            enabled = !isBackingUp
        ) {
            Text(
                if (backupSuccess == true) "Continue" else "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBackingUp
        ) {
            Icon(
                Icons.Filled.SkipNext,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Skip for now",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BackupOptionCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    isLoading: Boolean,
    isSuccess: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isSuccess) Positive.copy(alpha = 0.3f) else TextMuted.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .background(if (isSuccess) Positive.copy(alpha = 0.05f) else SurfaceSecondary)
            .clickable(enabled = !isLoading) { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Black
            )
        } else if (isSuccess) {
            Text("✓", color = Positive, fontWeight = FontWeight.Bold)
        }
    }
}
