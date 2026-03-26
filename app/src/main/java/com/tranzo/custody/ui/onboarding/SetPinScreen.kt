package com.tranzo.custody.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tranzo.custody.ui.components.PinDots
import com.tranzo.custody.ui.components.PinKeypad
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.Negative
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.White

@Composable
fun SetPinScreen(
    onPinSet: () -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isSettingPin by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

    val currentPin = if (isSettingPin) pin else confirmPin

    fun onDigit(digit: Int) {
        if (done) return
        error = null
        if (isSettingPin) {
            if (pin.length < 6) {
                pin += digit
                if (pin.length == 6) {
                    isSettingPin = false
                }
            }
        } else {
            if (confirmPin.length < 6) {
                confirmPin += digit
                if (confirmPin.length == 6) {
                    if (pin == confirmPin) {
                        done = true
                        onPinSet()
                    } else {
                        confirmPin = ""
                        error = "PINs don't match. Try again."
                    }
                }
            }
        }
    }

    fun onDelete() {
        if (done) return
        error = null
        if (isSettingPin) {
            if (pin.isNotEmpty()) pin = pin.dropLast(1)
        } else {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                isSettingPin = true
                pin = ""
            }
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Black)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = if (isSettingPin) "Create PIN" else "Confirm PIN",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSettingPin) "Set a 6-digit PIN to secure your account"
            else "Re-enter your PIN to confirm",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(48.dp))

        PinDots(pinLength = 6, filledCount = currentPin.length)

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error!!,
                style = MaterialTheme.typography.bodySmall,
                color = Negative,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PinKeypad(
            onNumberClick = { onDigit(it) },
            onDeleteClick = { onDelete() }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
