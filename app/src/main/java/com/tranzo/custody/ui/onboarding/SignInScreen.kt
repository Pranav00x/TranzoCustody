package com.tranzo.custody.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.BorderColor
import com.tranzo.custody.ui.theme.Negative
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.White

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome\nBack",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to access your wallet and card.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text("Email Address", style = MaterialTheme.typography.labelLarge, color = TextMuted)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.signInEmail,
            onValueChange = { viewModel.setSignInEmail(it) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Black,
                unfocusedBorderColor = BorderColor,
                cursorColor = Black
            ),
            placeholder = { Text("you@email.com", color = TextMuted.copy(alpha = 0.5f)) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("PIN", style = MaterialTheme.typography.labelLarge, color = TextMuted)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.signInPin,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) viewModel.setSignInPin(it) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Black,
                unfocusedBorderColor = BorderColor,
                cursorColor = Black
            ),
            placeholder = { Text("Enter your 6-digit PIN", color = TextMuted.copy(alpha = 0.5f)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        if (state.signInError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.signInError!!,
                style = MaterialTheme.typography.bodySmall,
                color = Negative,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.signIn(onSignInSuccess) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White),
            enabled = state.signInEmail.isNotBlank() && state.signInPin.length == 6 && !state.isLoading
        ) {
            Text(
                if (state.isLoading) "Signing in..." else "Sign In",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
