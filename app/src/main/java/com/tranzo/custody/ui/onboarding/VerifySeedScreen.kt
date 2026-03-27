package com.tranzo.custody.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.BorderColor
import com.tranzo.custody.ui.theme.Negative
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.White

@Composable
fun VerifySeedScreen(
    onVerified: () -> Unit,
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
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Confirm your phrase",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the correct word for each position.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(24.dp))

        state.challenges.forEach { ch ->
            Text(
                text = "Word #${ch.wordIndex + 1}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                ch.choices.forEach { choice ->
                    val selected = state.verificationPicks[ch.wordIndex] == choice
                    Text(
                        text = choice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) Black else BorderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setVerificationPick(ch.wordIndex, choice) }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (state.error != null) {
            Text(state.error!!, color = Negative, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.submitVerification { onVerified() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White),
            enabled = state.challenges.isNotEmpty() && state.challenges.all { state.verificationPicks.containsKey(it.wordIndex) }
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

