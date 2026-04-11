package com.tranzo.custody.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tranzo.custody.domain.model.Chain
import com.tranzo.custody.ui.theme.LocalTranzoTheme

@Composable
fun NetworkScreen(
    onBack: () -> Unit
) {
    val tranzoTheme = LocalTranzoTheme.current
    val supportedChains = Chain.values().filter { it != Chain.BITCOIN && it != Chain.SOLANA } // EVM Chains

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Networks & Relayers", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Gas Relayers (ERC-4337)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Relayers allow you to pay transaction fees in stablecoins or enjoy gasless transactions.",
            style = MaterialTheme.typography.bodySmall,
            color = tranzoTheme.textMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        supportedChains.forEach { chain ->
            NetworkRelayerRow(chain = chain)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun NetworkRelayerRow(chain: Chain) {
    val tranzoTheme = LocalTranzoTheme.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(chain.iconLetter, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(chain.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Relayer Active", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
            }
        }

        Switch(
            checked = true,
            onCheckedChange = { },
            enabled = false, // Always active for supported chains
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF10B981)
            )
        )
    }
}
