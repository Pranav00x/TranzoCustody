package com.tranzo.custody.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.components.PinDots
import com.tranzo.custody.ui.components.PinKeypad
import com.tranzo.custody.ui.theme.LocalTranzoTheme

import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import com.tranzo.custody.security.BiometricHelper
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun VerifyPinScreen(
    onSuccess: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: VerifyPinViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tranzoTheme = LocalTranzoTheme.current
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper() }

    fun showBiometric() {
        val activity = context as? FragmentActivity ?: return
        biometricHelper.showBiometricPrompt(
            activity = activity,
            title = "Unlock Tranzo",
            subtitle = "Verify your identity to continue",
            onSuccess = { viewModel.onBiometricSuccess() },
            onError = { /* ViewModel handles errors if needed */ }
        )
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSuccess()
        }
    }

    LaunchedEffect(state.biometricEnabled) {
        if (state.biometricEnabled && !state.isSuccess) {
            showBiometric()
        }
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
            if (onBack != null) {
                Box(modifier = Modifier.align(Alignment.Start)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Enter PIN",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secure your sessions with your 6-digit PIN",
                style = MaterialTheme.typography.bodyLarge,
                color = tranzoTheme.textMuted
            )

            Spacer(modifier = Modifier.height(48.dp))

            PinDots(
                pinLength = 6,
                filledCount = state.pin.length
            )

            if (state.error != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PinKeypad(
                onNumberClick = { viewModel.addNumber(it) },
                onDeleteClick = { viewModel.delete() },
                onBiometricClick = if (state.biometricEnabled) { { showBiometric() } } else null
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
