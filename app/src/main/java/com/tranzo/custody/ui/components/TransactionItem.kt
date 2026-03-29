package com.tranzo.custody.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tranzo.custody.domain.model.Transaction
import com.tranzo.custody.domain.model.TransactionStatus
import com.tranzo.custody.domain.model.TransactionType
import com.tranzo.custody.ui.theme.LocalTranzoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val positive = MaterialTheme.colorScheme.tertiary
    val negative = MaterialTheme.colorScheme.error
    val positiveLight = LocalTranzoTheme.current.positive.copy(alpha = 0.15f)
    val negativeLight = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val (icon, bgColor, iconColor) = when (transaction.type) {
        TransactionType.SENT -> Triple(Icons.Default.ArrowUpward, negativeLight, negative)
        TransactionType.RECEIVED -> Triple(Icons.Default.ArrowDownward, positiveLight, positive)
        TransactionType.SWAPPED -> Triple(Icons.Default.SwapHoriz, primaryContainer, MaterialTheme.colorScheme.onBackground)
        TransactionType.CARD_SPEND -> Triple(Icons.Default.CreditCard, primaryContainer, MaterialTheme.colorScheme.onBackground)
        TransactionType.BOUGHT -> Triple(Icons.Default.ShoppingCart, positiveLight, positive)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTimestamp(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalTranzoTheme.current.textMuted
                )
                if (transaction.status == TransactionStatus.PENDING) {
                    Text(
                        text = " • Pending",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.amount,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = transaction.fiatAmount,
                style = MaterialTheme.typography.bodySmall,
                color = LocalTranzoTheme.current.textMuted
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(timestamp))
        else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(timestamp))
    }
}
