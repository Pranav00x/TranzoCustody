package com.tranzo.custody.ui.activity

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tranzo.custody.domain.model.Transaction
import com.tranzo.custody.domain.model.TransactionStatus
import com.tranzo.custody.domain.model.TransactionType
import com.tranzo.custody.ui.theme.LocalTranzoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onBack: () -> Unit,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    var transaction by remember { mutableStateOf<Transaction?>(null) }
    val tranzoTheme = LocalTranzoTheme.current

    LaunchedEffect(transactionId) {
        transaction = viewModel.getTransactionDetail(transactionId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Transaction Details", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        transaction?.let { tx ->
            Spacer(modifier = Modifier.height(32.dp))

            val (icon, bgColor, iconColor) = when (tx.type) {
                TransactionType.SENT -> Triple(Icons.Default.ArrowUpward, MaterialTheme.colorScheme.error.copy(alpha = 0.15f), MaterialTheme.colorScheme.error)
                TransactionType.RECEIVED -> Triple(Icons.Default.ArrowDownward, tranzoTheme.positive.copy(alpha = 0.15f), MaterialTheme.colorScheme.tertiary)
                TransactionType.SWAPPED -> Triple(Icons.Default.SwapHoriz, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onBackground)
                TransactionType.CARD_SPEND -> Triple(Icons.Default.CreditCard, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onBackground)
                TransactionType.BOUGHT -> Triple(Icons.Default.ShoppingCart, tranzoTheme.positive.copy(alpha = 0.15f), MaterialTheme.colorScheme.tertiary)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(tx.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(tx.amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Text(tx.fiatAmount, style = MaterialTheme.typography.bodyMedium, color = tranzoTheme.textMuted)

                Spacer(modifier = Modifier.height(12.dp))

                val statusColor = when (tx.status) {
                    TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.tertiary
                    TransactionStatus.PENDING -> Color(0xFFF59E0B)
                    TransactionStatus.FAILED -> MaterialTheme.colorScheme.error
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tx.status.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("Date", SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US).format(Date(tx.timestamp)))
                    tx.networkName?.let { DetailRow("Network", it) }
                    tx.fee?.let { DetailRow("Fee", it) }
                    tx.confirmations.takeIf { it > 0 }?.let { DetailRow("Confirmations", it.toString()) }
                    tx.fromAddress?.let { DetailRow("From", it) }
                    tx.toAddress?.let { DetailRow("To", it) }
                    tx.txHash?.let { DetailRow("Tx Hash", it) }
                }
            }

            tx.txHash?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onBackground)
                ) {
                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("View on Block Explorer", fontWeight = FontWeight.SemiBold)
                }
            }
        } ?: run {
            Spacer(modifier = Modifier.height(100.dp))
            Text("Transaction not found", style = MaterialTheme.typography.bodyMedium, color = tranzoTheme.textMuted, modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val tranzoTheme = LocalTranzoTheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = tranzoTheme.textMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
