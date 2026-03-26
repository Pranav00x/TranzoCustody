package com.tranzo.custody.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.components.formatCurrency
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.BorderColor
import com.tranzo.custody.ui.theme.DividerColor
import com.tranzo.custody.ui.theme.Positive
import com.tranzo.custody.ui.theme.PositiveLight
import com.tranzo.custody.ui.theme.SurfaceSecondary
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.White

@Composable
fun BridgeScreen(
    onBack: () -> Unit,
    viewModel: BridgeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.success) {
        BridgeSuccessView(
            creditedAmount = state.creditedAmount,
            onDone = {
                viewModel.reset()
                onBack()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Black)
            }
            Text(
                "Add to Spend",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Convert your deposited crypto to card spending balance.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

        // From: Wallet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceSecondary)
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wallet, null, tint = Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("From: Wallet", style = MaterialTheme.typography.labelLarge, color = TextMuted)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Crypto", style = MaterialTheme.typography.labelSmall, color = TextMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(White)
                            .padding(horizontal = 8.dp, vertical = 3.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Token", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.tokens.take(5).forEach { token ->
                        val isSelected = token.symbol == state.selectedToken?.symbol
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Black, RoundedCornerShape(10.dp))
                                    else Modifier.border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                )
                                .background(White)
                                .clickable { viewModel.selectToken(token) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(token.symbol, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text("${"%.2f".format(token.balance)}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Amount", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.setAmount(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Black,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = Black,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    placeholder = { Text("0.00", color = TextMuted) },
                    suffix = { state.selectedToken?.let { Text(it.symbol, color = TextMuted) } },
                    singleLine = true,
                    isError = state.error != null
                )

                state.selectedToken?.let { token ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Available: ${"%.4f".format(token.balance)} ${token.symbol} ≈ ${formatCurrency(token.fiatValue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                state.error?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Medium)
                }
            }
        }

        // Arrow
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowDownward, null, tint = White, modifier = Modifier.size(22.dp))
            }
        }

        // To: Spendable Balance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceSecondary)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, null, tint = Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("To: Card Balance", style = MaterialTheme.typography.labelLarge, color = TextMuted)
                Spacer(modifier = Modifier.weight(1f))
                Text("Spendable", style = MaterialTheme.typography.labelSmall, color = Positive,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(PositiveLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Conversion Preview
        AnimatedVisibility(visible = state.preview != null && state.error == null) {
            state.preview?.let { preview ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Conversion Preview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        HorizontalDivider(color = DividerColor)
                        PreviewRow("You send", "${"%.4f".format(preview.fromAmount)} ${preview.fromToken.symbol}")
                        PreviewRow("Exchange rate", "1 ${preview.fromToken.symbol} = ${formatCurrency(preview.exchangeRate)}")
                        PreviewRow("Network fee", formatCurrency(preview.networkFee))
                        PreviewRow("Platform fee (0.5%)", formatCurrency(preview.platformFee))
                        HorizontalDivider(color = DividerColor)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("You receive", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(formatCurrency(preview.estimatedTotal), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Positive)
                        }
                        Text(
                            "Crypto → Fiat conversion. Funds will be available on your card balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        if (state.isLoadingPreview) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.executeTopUp() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White),
            enabled = state.preview != null && !state.isProcessing && state.error == null
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    "Add to Spendable Balance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BridgeSuccessView(creditedAmount: Double, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            tint = Positive,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Funds Added!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            formatCurrency(creditedAmount),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Positive
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "has been added to your spendable card balance.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White)
        ) {
            Text("Done", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
