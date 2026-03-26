package com.tranzo.custody.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.components.PinDots
import com.tranzo.custody.ui.components.PinKeypad
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.Negative
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.White

@Composable
fun SetPinScreen(
    onPinSet: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentPin = if (state.isSettingPin) state.pin else state.confirmPin

    if (!state.isSettingPin && state.confirmPin.length == 6) {
        if (viewModel.confirmPin()) {
            onPinSet()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Black
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = if (state.isSettingPin) "Create PIN" else "Confirm PIN",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (state.isSettingPin) "Set a 6-digit PIN to secure your wallet"
            else "Re-enter your PIN to confirm",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(48.dp))

        PinDots(pinLength = 6, filledCount = currentPin.length)

        if (state.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.error!!,
                style = MaterialTheme.typography.bodySmall,
                color = Negative,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PinKeypad(
            onNumberClick = { viewModel.onPinDigit(it) },
            onDeleteClick = { viewModel.onPinDelete() }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
