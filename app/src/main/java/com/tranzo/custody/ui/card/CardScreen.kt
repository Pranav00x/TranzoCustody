package com.tranzo.custody.ui.card

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.domain.model.CardTransaction
import com.tranzo.custody.domain.model.KycStatus
import com.tranzo.custody.domain.model.SpendMode
import com.tranzo.custody.ui.components.CryptoCardView
import com.tranzo.custody.ui.components.formatCurrency
import com.tranzo.custody.ui.theme.LocalTranzoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CardScreen(
    onCardSettings: () -> Unit,
    onAddFunds: () -> Unit,
    viewModel: CardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tranzoTheme = LocalTranzoTheme.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Card", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Lock, "Lock", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(22.dp))
                }
            }
        }

        // KYC Banner
        item {
            state.card?.let { card ->
                if (card.kycStatus != KycStatus.VERIFIED) {
                    KycBanner(status = card.kycStatus)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Card Visual
        item {
            state.card?.let { card ->
                CryptoCardView(card = card, modifier = Modifier.padding(horizontal = 20.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Spendable Balance (prominent)
        item {
            state.card?.let { card ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Spendable Balance", style = MaterialTheme.typography.labelLarge, color = tranzoTheme.textMuted)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                if (card.spendMode == SpendMode.AUTO_CONVERT) "Auto-convert ON" else "Prepaid",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (card.spendMode == SpendMode.AUTO_CONVERT) MaterialTheme.colorScheme.tertiary else tranzoTheme.textMuted,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (card.spendMode == SpendMode.AUTO_CONVERT) tranzoTheme.positive.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            formatCurrency(card.spendableBalance),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Card balance · Used for payments",
                            style = MaterialTheme.typography.bodySmall,
                            color = tranzoTheme.textMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onAddFunds,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            enabled = card.kycStatus == KycStatus.VERIFIED
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Funds", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Card Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CardActionItem(
                    icon = if (state.card?.isFrozen == true) Icons.Default.AcUnit else Icons.Default.AcUnit,
                    label = if (state.card?.isFrozen == true) "Unfreeze" else "Freeze",
                    onClick = { viewModel.toggleFreeze() }
                )
                CardActionItem(
                    icon = Icons.Default.RemoveRedEye,
                    label = "Details",
                    onClick = { viewModel.toggleShowDetails() }
                )
                CardActionItem(
                    icon = Icons.Default.Wallet,
                    label = "Google Pay",
                    onClick = { }
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Monthly Spending
        item {
            state.card?.let { card ->
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text("Monthly Spending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(formatCurrency(card.monthlySpent), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text("of ${formatCurrency(card.monthlyLimit)}", style = MaterialTheme.typography.bodyMedium, color = tranzoTheme.textMuted)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val progress = (if (card.monthlyLimit > 0) card.monthlySpent / card.monthlyLimit else 0.0)
                                .coerceIn(0.0, 1.0)
                                .toFloat()
                                .takeIf { !it.isNaN() } ?: 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${(progress * 100).toInt()}% of monthly limit", style = MaterialTheme.typography.bodySmall, color = tranzoTheme.textMuted)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Recent Transactions
        item {
            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(state.transactions) { tx ->
            CardTransactionItem(transaction = tx)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun KycBanner(status: KycStatus) {
    val bgColor: Color
    val iconColor: Color
    val icon: ImageVector
    val text: String

    when (status) {
        KycStatus.NOT_STARTED -> {
            bgColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f); iconColor = MaterialTheme.colorScheme.error; icon = Icons.Default.Warning; text = "KYC required to activate card"
        }
        KycStatus.PENDING -> {
            bgColor = Color(0xFFFEF3C7); iconColor = Color(0xFFF59E0B); icon = Icons.Default.Schedule; text = "KYC verification in progress"
        }
        KycStatus.REJECTED -> {
            bgColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f); iconColor = MaterialTheme.colorScheme.error; icon = Icons.Default.Warning; text = "KYC rejected — please resubmit"
        }
        else -> return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = iconColor)
        }
    }
}

@Composable
private fun CardActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    val tranzoTheme = LocalTranzoTheme.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(icon, label, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = tranzoTheme.textMuted)
    }
}

@Composable
private fun CardTransactionItem(transaction: CardTransaction) {
    val tranzoTheme = LocalTranzoTheme.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(transaction.merchantIcon, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(transaction.merchantName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            Row {
                Text(
                    SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = tranzoTheme.textMuted
                )
                Text(
                    " · ${transaction.sourceLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = tranzoTheme.textMuted
                )
            }
        }
        Text("-${formatCurrency(transaction.amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    }
}
