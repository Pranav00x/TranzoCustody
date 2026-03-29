package com.tranzo.custody.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.components.PinDots
import com.tranzo.custody.ui.components.PinKeypad
import com.tranzo.custody.ui.theme.LocalTranzoTheme

@Composable
fun SetPinScreen(
    onPinSet: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showError by remember { mutableStateOf(false) }
    val tranzoTheme = LocalTranzoTheme.current

    val currentPin = if (state.isSettingPin) state.pin else state.confirmPin

    LaunchedEffect(state.walletSetupComplete) {
        if (state.walletSetupComplete) onPinSet()
    }

    LaunchedEffect(state.setupError) {
        showError = state.setupError != null
    }

    if (showError && state.setupError != null) {
        AlertDialog(
            onDismissRequest = {
                showError = false
                viewModel.consumeSetupError()
                viewModel.resetAfterFailedSetup()
            },
            title = { Text("Could not create wallet") },
            text = { Text(state.setupError!!) },
            confirmButton = {
                TextButton(onClick = {
                    showError = false
                    viewModel.consumeSetupError()
                    viewModel.resetAfterFailedSetup()
                }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = if (state.isSettingPin) "Create PIN" else "Confirm PIN",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (state.isSettingPin) "Encrypts your keys on this device"
                else "Re-enter your PIN to confirm",
            style = MaterialTheme.typography.bodyMedium,
            color = tranzoTheme.textMuted
        )

        Spacer(modifier = Modifier.height(48.dp))

        PinDots(pinLength = 6, filledCount = currentPin.length)

        if (state.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.error!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Securing wallet…", style = MaterialTheme.typography.bodyMedium, color = tranzoTheme.textMuted)
        }

        Spacer(modifier = Modifier.weight(1f))

        PinKeypad(
            onNumberClick = { digit -> viewModel.onPinDigit(digit) },
            onDeleteClick = { viewModel.onPinDelete() }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
