package com.tranzo.custody.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.ui.components.AssetListItem
import com.tranzo.custody.ui.components.QuickActionButton
import com.tranzo.custody.ui.components.ShimmerAssetItem
import com.tranzo.custody.ui.components.formatCurrency
import com.tranzo.custody.ui.theme.LocalTranzoTheme
import com.tranzo.custody.ui.util.glassCard

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
    val theme = LocalTranzoTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .glassCard(cornerRadius = 22.dp)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCode, "QR Code", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }

                        Text(
                            text = "TRANZO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .glassCard(cornerRadius = 22.dp)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Portfolio Balance
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Portfolio Value", style = MaterialTheme.typography.bodyMedium, color = theme.textMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (state.isLoading) {
                            Text("Loading...", style = MaterialTheme.typography.bodyMedium, color = theme.textMuted)
                        } else {
                            state.portfolio?.let { portfolio ->
                                Text(
                                    text = formatCurrency(portfolio.totalBalanceFiat),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val changeColor = if (portfolio.dailyChangePercent >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                val prefix = if (portfolio.dailyChangePercent >= 0) "+" else ""
                                Text(
                                    text = "$prefix${"%.2f".format(portfolio.dailyChangePercent)}% today",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = changeColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Balance Cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .glassCard(cornerRadius = 24.dp, shadowElevation = 4.dp)
                                .padding(20.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary.copy(0.8f), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Wallet", style = MaterialTheme.typography.labelSmall, color = theme.textMuted)
                                Text(
                                    text = formatCurrency(state.portfolio?.walletBalanceFiat ?: 0.0),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .glassCard(cornerRadius = 24.dp, shadowElevation = 4.dp)
                                .clickable(onClick = onAddToSpend)
                                .padding(20.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.CreditCard, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Spendable", style = MaterialTheme.typography.labelSmall, color = theme.textMuted)
                                Text(
                                    text = formatCurrency(state.portfolio?.spendableBalanceFiat ?: 0.0),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Quick Actions
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionButton(icon = Icons.Default.ArrowUpward, label = "Send", onClick = onSendClick)
                        QuickActionButton(icon = Icons.Default.ArrowDownward, label = "Receive", onClick = onReceiveClick)
                        QuickActionButton(icon = Icons.Default.SwapHoriz, label = "Swap", onClick = onSwapClick)
                        QuickActionButton(icon = Icons.Default.ShoppingCart, label = "Buy", onClick = onBuyClick)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Assets
                item {
                    Text(
                        text = "Assets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }

                if (state.isLoading) {
                    items(5) { ShimmerAssetItem() }
                } else {
                    items(state.portfolio?.tokens ?: emptyList()) { token ->
                        AssetListItem(token = token)
                    }
                }
            }
        }
    }
}
