package com.tranzo.custody.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tranzo.custody.ui.theme.LocalTranzoTheme

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tranzoTheme = LocalTranzoTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                "Help & Support",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Get in touch with the right team for your needs.",
            style = MaterialTheme.typography.bodyMedium,
            color = tranzoTheme.textMuted,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Legal Support
        SupportContactItem(
            icon = Icons.Default.Gavel,
            title = "Legal Support",
            subtitle = "Compliance, disputes, and legal queries",
            email = "legal@tranzo.money",
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:legal@tranzo.money")
                    putExtra(Intent.EXTRA_SUBJECT, "Legal Support Request")
                }
                context.startActivity(intent)
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // App Bugs & Suggestions
        SupportContactItem(
            icon = Icons.Default.BugReport,
            title = "App Bugs & Suggestions",
            subtitle = "Report issues or share feature ideas",
            email = "connect@tranzo.money",
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:connect@tranzo.money")
                    putExtra(Intent.EXTRA_SUBJECT, "Bug Report / Suggestion")
                }
                context.startActivity(intent)
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Onboarding & Partner Requests
        SupportContactItem(
            icon = Icons.Default.Handshake,
            title = "Onboarding & Partnerships",
            subtitle = "B2B inquiries, partner integrations",
            email = "hi@tranzo.money",
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:hi@tranzo.money")
                    putExtra(Intent.EXTRA_SUBJECT, "Partnership / Onboarding Request")
                }
                context.startActivity(intent)
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Founder
        SupportContactItem(
            icon = Icons.Default.Person,
            title = "Founder – Pranav",
            subtitle = "Direct line to the founder",
            email = "pranav@tranzo.money",
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:pranav@tranzo.money")
                    putExtra(Intent.EXTRA_SUBJECT, "Message for Pranav")
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SupportContactItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    email: String,
    onClick: () -> Unit
) {
    val tranzoTheme = LocalTranzoTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = tranzoTheme.textMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = tranzoTheme.textMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
