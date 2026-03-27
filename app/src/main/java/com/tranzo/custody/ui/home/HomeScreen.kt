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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.components.AssetListItem
import com.tranzo.custody.ui.components.QuickActionButton
import com.tranzo.custody.ui.components.ShimmerAssetItem
import com.tranzo.custody.ui.components.formatCurrency
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.BorderColor
import com.tranzo.custody.ui.theme.Negative
import com.tranzo.custody.ui.theme.Positive
import com.tranzo.custody.ui.theme.PositiveLight
import com.tranzo.custody.ui.theme.SurfaceSecondary
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onSwapClick: () -> Unit,
    onBuyClick: () -> Unit,
    onAddToSpend: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
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
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.QrCode, "QR Code", tint = Black, modifier = Modifier.size(24.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tranzo",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Black
                        )
                        if (state.smartWalletAddress.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.smartWalletAddress,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, "Notifications", tint = Black, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Total Portfolio Balance
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Portfolio", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))

                    if (state.isLoading) {
                        Text("Loading...", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    } else {
                        state.portfolio?.let { portfolio ->
                            Text(
                                text = formatCurrency(portfolio.totalBalanceFiat),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val changeColor = if (portfolio.dailyChangePercent >= 0) Positive else Negative
                            val prefix = if (portfolio.dailyChangePercent >= 0) "+" else ""
                            Text(
                                text = "$prefix${"%.2f".format(portfolio.dailyChangePercent)}% today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = changeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Two Balance Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Wallet Balance
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, null, tint = Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Wallet", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatCurrency(state.portfolio?.walletBalanceFiat ?: 0.0),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Black,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Deposited · Crypto assets", style = MaterialTheme.typography.labelSmall, color = TextMuted, lineHeight = 14.sp)
                        }
                    }

                    // Spendable Balance
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .clickable(onClick = onAddToSpend)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, null, tint = Positive, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Spendable", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatCurrency(state.portfolio?.spendableBalanceFiat ?: 0.0),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Positive,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Card balance · Payments", style = MaterialTheme.typography.labelSmall, color = TextMuted, lineHeight = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add to Spend button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PositiveLight)
                        .clickable(onClick = onAddToSpend)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = Positive, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add to Spendable Balance",
                            style = MaterialTheme.typography.labelLarge,
                            color = Positive,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(icon = Icons.Default.ArrowUpward, label = "Send", onClick = onSendClick)
                    QuickActionButton(icon = Icons.Default.ArrowDownward, label = "Receive", onClick = onReceiveClick)
                    QuickActionButton(icon = Icons.Default.SwapHoriz, label = "Swap", onClick = onSwapClick)
                    QuickActionButton(icon = Icons.Default.ShoppingCart, label = "Buy", onClick = onBuyClick)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Assets header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Your Assets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Black)
                        Text("Deposited crypto · Managed by Tranzo", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }

            if (state.isLoading) {
                items(5) { ShimmerAssetItem() }
            } else {
                items(state.portfolio?.tokens ?: emptyList()) { token ->
                    AssetListItem(token = token)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
