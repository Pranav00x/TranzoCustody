package com.tranzo.custody.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.ui.theme.LocalTranzoTheme

@Composable
fun SendScreen(
    onBack: () -> Unit,
    viewModel: SendViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var toAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedTokenIndex by remember { mutableStateOf(0) }

    val tokens = state.portfolioTokens
    val selectedToken = tokens.getOrNull(selectedTokenIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Send",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select asset", style = MaterialTheme.typography.labelLarge, color = LocalTranzoTheme.current.textMuted)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tokens.take(4).forEachIndexed { index, token ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (index == selectedTokenIndex) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        )
                        .clickable { selectedTokenIndex = index }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(token.iconColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = token.symbol.first().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = token.symbol,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Recipient", style = MaterialTheme.typography.labelLarge, color = LocalTranzoTheme.current.textMuted)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = toAddress,
            onValueChange = { toAddress = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            placeholder = { Text("0x?", color = LocalTranzoTheme.current.textMuted) },
            trailingIcon = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.QrCodeScanner, "Scan", tint = MaterialTheme.colorScheme.onBackground)
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Amount", style = MaterialTheme.typography.labelLarge, color = LocalTranzoTheme.current.textMuted)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            placeholder = { Text("0.00", color = LocalTranzoTheme.current.textMuted) },
            suffix = { selectedToken?.let { Text(it.symbol, color = LocalTranzoTheme.current.textMuted) } },
            singleLine = true
        )

        selectedToken?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Available: ${"%.4f".format(it.balance)} ${it.symbol}",
                style = MaterialTheme.typography.bodySmall,
                color = LocalTranzoTheme.current.textMuted
            )
        }

        state.sendError?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        state.sendMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val t = selectedToken ?: return@Button
                val amt = amount.toDoubleOrNull() ?: return@Button
                viewModel.send(Chain.POLYGON, toAddress, amt, t) { }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            enabled = toAddress.isNotBlank() && amount.isNotBlank() && !state.isSending && selectedToken != null
        ) {
            Text(
                if (state.isSending) "Sending?" else "Submit (testnet)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
