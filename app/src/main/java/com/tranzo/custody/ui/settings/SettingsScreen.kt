package com.tranzo.custody.ui.settings

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tranzo.custody.domain.model.KycStatus
import com.tranzo.custody.domain.model.SpendMode
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.BorderColor
import com.tranzo.custody.ui.theme.Negative
import com.tranzo.custody.ui.theme.NegativeLight
import com.tranzo.custody.ui.theme.Positive
import com.tranzo.custody.ui.theme.PositiveLight
import com.tranzo.custody.ui.theme.SurfaceSecondary
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.White

@Composable
fun SettingsScreen(
    onSecurityClick: () -> Unit,
    onDripperClick: () -> Unit,
    onCardSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // Profile + KYC
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SurfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Black, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(state.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Black)
                Text(state.userEmail, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            KycBadge(status = state.kycStatus)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Spend Mode Section
        SectionTitle("Spending")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceSecondary)
                .padding(16.dp)
        ) {
            Column {
                Text("Card Spend Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Choose how your card payments are funded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                SpendModeOption(
                    title = "Use Spendable Balance",
                    description = "Spend from pre-loaded card balance (prepaid)",
                    isSelected = state.spendMode == SpendMode.SPENDABLE_ONLY,
                    onClick = { viewModel.setSpendMode(SpendMode.SPENDABLE_ONLY) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SpendModeOption(
                    title = "Auto-convert from Wallet",
                    description = "Instantly sell crypto when you tap your card",
                    isSelected = state.spendMode == SpendMode.AUTO_CONVERT,
                    onClick = { viewModel.setSpendMode(SpendMode.AUTO_CONVERT) }
                )

                if (state.spendMode == SpendMode.AUTO_CONVERT) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(10.dp)
                    ) {
                        Text(
                            "Auto-convert may fail if liquidity is insufficient. A spendable balance fallback is recommended.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Security
        SectionTitle("Security")
        SettingsItem(Icons.Default.Security, "Security Settings", onClick = onSecurityClick)
        SettingsItem(Icons.Default.UsbOff, "Dripper Hardware Wallet", subtitle = "No device paired", onClick = onDripperClick)

        Spacer(modifier = Modifier.height(8.dp))

        // Card
        SectionTitle("Card")
        SettingsItem(Icons.Default.CreditCard, "Card Management", onClick = onCardSettingsClick)

        Spacer(modifier = Modifier.height(8.dp))

        // Preferences
        SectionTitle("Preferences")
        SettingsItem(Icons.Default.Language, "Default Currency", subtitle = state.defaultCurrency)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Notifications, null, tint = Black, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Push Notifications", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(
                checked = state.pushNotificationsEnabled,
                onCheckedChange = { viewModel.togglePushNotifications(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = Black,
                    uncheckedThumbColor = White,
                    uncheckedTrackColor = TextMuted.copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Support
        SectionTitle("Support")
        SettingsItem(Icons.AutoMirrored.Filled.HelpOutline, "Help Center")
        SettingsItem(Icons.Default.Description, "Terms of Service")
        SettingsItem(Icons.Default.Description, "Privacy Policy")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.logout { onLogout() } }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Negative, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Log Out", style = MaterialTheme.typography.bodyLarge, color = Negative, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Tranzo v${state.appVersion}", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp))
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun KycBadge(status: KycStatus) {
    val (bg, fg, text) = when (status) {
        KycStatus.VERIFIED -> Triple(PositiveLight, Positive, "Verified")
        KycStatus.PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFFF59E0B), "Pending")
        KycStatus.REJECTED -> Triple(NegativeLight, Negative, "Rejected")
        KycStatus.NOT_STARTED -> Triple(SurfaceSecondary, TextMuted, "KYC Required")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when (status) {
                KycStatus.VERIFIED -> Icons.Default.CheckCircle
                KycStatus.PENDING -> Icons.Default.Schedule
                else -> Icons.Default.Warning
            }
            Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SpendModeOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) Modifier.border(2.dp, Black, RoundedCornerShape(12.dp))
                else Modifier.border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            )
            .background(White)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) Modifier.background(Black)
                    else Modifier.border(1.5.dp, BorderColor, CircleShape)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(White))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Black)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = TextMuted, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Black, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Black)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextMuted) }
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
    }
}
