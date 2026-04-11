package com.tranzo.custody.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.theme.LocalTranzoTheme
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import com.tranzo.custody.security.BiometricHelper
import androidx.compose.runtime.remember

@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tranzoTheme = LocalTranzoTheme.current
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper() }

    fun tryEnableBiometric() {
        if (!biometricHelper.isBiometricAvailable(context)) {
            // In a real app, show a snackbar or dialog: "Biometrics not available"
            return
        }

        val activity = context as? FragmentActivity ?: return
        biometricHelper.showBiometricPrompt(
            activity = activity,
            title = "Confirm Biometric",
            subtitle = "Verify to enable biometric unlock",
            onSuccess = { viewModel.toggleBiometric(true) },
            onError = { /* Handle error */ }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Security", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Authentication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        SecuritySwitchRow(
            title = "PIN Security",
            subtitle = "Require PIN to access wallet",
            checked = state.pinRequired,
            onCheckedChange = { viewModel.togglePinRequired(it) }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        SecuritySwitchRow(
            title = "Biometric Unlock",
            subtitle = "Use fingerprint or face to unlock",
            checked = state.biometricEnabled,
            onCheckedChange = { 
                if (it) {
                    tryEnableBiometric()
                } else {
                    viewModel.toggleBiometric(false)
                }
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        val lockTimes = listOf(1, 5, 10, 30, 0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val currentIdx = lockTimes.indexOf(state.autoLockMinutes)
                    val nextIdx = (currentIdx + 1) % lockTimes.size
                    viewModel.setAutoLockMinutes(lockTimes[nextIdx])
                }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Auto-Lock Timer", style = MaterialTheme.typography.bodyLarge)
                Text("Lock app after inactivity", style = MaterialTheme.typography.bodySmall, color = tranzoTheme.textMuted)
            }
            Text(
                text = if (state.autoLockMinutes == 0) "Never" else "${state.autoLockMinutes} min",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Change PIN Flow */ }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Change PIN", style = MaterialTheme.typography.bodyLarge)
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = tranzoTheme.textMuted, modifier = Modifier.graphicsLayer(rotationZ = 180f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Account Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Two-Factor Authentication", style = MaterialTheme.typography.bodyLarge)
                Text("Add extra protection to your account", style = MaterialTheme.typography.bodySmall, color = tranzoTheme.textMuted)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Active Sessions", style = MaterialTheme.typography.bodyLarge)
                Text("Manage your login sessions", style = MaterialTheme.typography.bodySmall, color = tranzoTheme.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SecuritySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tranzoTheme = LocalTranzoTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = tranzoTheme.textMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedTrackColor = tranzoTheme.textMuted.copy(alpha = 0.3f)
            )
        )
    }
}
