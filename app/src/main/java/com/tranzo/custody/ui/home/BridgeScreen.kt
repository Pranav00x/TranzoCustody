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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import com.tranzo.custody.ui.components.formatCurrency
import com.tranzo.custody.ui.theme.LocalTranzoTheme
import com.tranzo.custody.ui.util.glassCard
import com.tranzo.custody.ui.util.glassOnDark

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1).copy(0.12f), Color.Transparent)
                    ),
                    radius = size.width * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEC4899).copy(0.10f), Color.Transparent)
                    ),
                    radius = size.width * 0.7f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.4f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                "Add to Spend",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Convert your deposited crypto to card spending balance.",
            style = MaterialTheme.typography.bodyMedium,
            color = LocalTranzoTheme.current.textMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

        // From: Wallet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = 16.dp, alpha = 0.08f)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wallet, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("From: Wallet", style = MaterialTheme.typography.labelLarge, color = LocalTranzoTheme.current.textMuted)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Crypto", style = MaterialTheme.typography.labelSmall, color = LocalTranzoTheme.current.textMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 8.dp, vertical = 3.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Token", style = MaterialTheme.typography.labelMedium, color = LocalTranzoTheme.current.textMuted)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.tokens.take(5).forEach { token ->
                        val isSelected = token.symbol == state.selectedToken?.symbol
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                )
                                .background(MaterialTheme.colorScheme.background)
                                .clickable { viewModel.selectToken(token) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(token.symbol, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text("${"%.2f".format(token.balance)}", style = MaterialTheme.typography.labelSmall, color = LocalTranzoTheme.current.textMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Amount", style = MaterialTheme.typography.labelMedium, color = LocalTranzoTheme.current.textMuted)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.setAmount(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    placeholder = { Text("0.00", color = LocalTranzoTheme.current.textMuted) },
                    suffix = { state.selectedToken?.let { Text(it.symbol, color = LocalTranzoTheme.current.textMuted) } },
                    singleLine = true,
                    isError = state.error != null
                )

                state.selectedToken?.let { token ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Available: ${"%.4f".format(token.balance)} ${token.symbol} ≈ ${formatCurrency(token.fiatValue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalTranzoTheme.current.textMuted
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
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowDownward, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp))
            }
        }

        // To: Spendable Balance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassOnDark(cornerRadius = 16.dp, alpha = 0.12f)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("To: Card Balance", style = MaterialTheme.typography.labelLarge, color = LocalTranzoTheme.current.textMuted)
                Spacer(modifier = Modifier.weight(1f))
                Text("Spendable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(LocalTranzoTheme.current.positive.copy(alpha = 0.15f))
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
                        .glassCard(cornerRadius = 16.dp, alpha = 0.05f)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Conversion Preview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        PreviewRow("You send", "${"%.4f".format(preview.fromAmount)} ${preview.fromToken.symbol}")
                        PreviewRow("Exchange rate", "1 ${preview.fromToken.symbol} = ${formatCurrency(preview.exchangeRate)}")
                        PreviewRow("Network fee", formatCurrency(preview.networkFee))
                        PreviewRow("Platform fee (0.5%)", formatCurrency(preview.platformFee))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("You receive", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(formatCurrency(preview.estimatedTotal), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Text(
                            "Crypto → Fiat conversion. Funds will be available on your card balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalTranzoTheme.current.textMuted
                        )
                    }
                }
            }
        }

        if (state.isLoadingPreview) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.executeTopUp() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            enabled = state.preview != null && !state.isProcessing && state.error == null
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
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
        Text(label, style = MaterialTheme.typography.bodySmall, color = LocalTranzoTheme.current.textMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BridgeSuccessView(creditedAmount: Double, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            tint = MaterialTheme.colorScheme.tertiary,
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
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "has been added to your spendable card balance.",
            style = MaterialTheme.typography.bodyMedium,
            color = LocalTranzoTheme.current.textMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Done", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
