package com.tranzo.custody.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.components.PinDots
import com.tranzo.custody.ui.components.PinKeypad
import com.tranzo.custody.ui.theme.LocalTranzoTheme

@Composable
fun RestoreSetPinScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: RestorePinViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tranzoTheme = LocalTranzoTheme.current

    val currentPin = if (state.isSettingPin) state.pin else state.confirmPin

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                text = if (state.isSettingPin) "Secure your wallet" else "Confirm your PIN",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (state.isSettingPin) "Create a 6-digit PIN to encrypt your keys"
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
                Text("Finalizing restoration…", style = MaterialTheme.typography.bodyMedium, color = tranzoTheme.textMuted)
            }

            Spacer(modifier = Modifier.weight(1f))

            PinKeypad(
                onNumberClick = { digit -> viewModel.onPinDigit(digit) },
                onDeleteClick = { viewModel.onPinDelete() }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
