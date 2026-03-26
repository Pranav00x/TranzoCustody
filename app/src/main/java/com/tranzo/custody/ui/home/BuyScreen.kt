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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tranzo.custody.ui.theme.Black
import com.tranzo.custody.ui.theme.BorderColor
import com.tranzo.custody.ui.theme.SurfaceSecondary
import com.tranzo.custody.ui.theme.TextMuted
import com.tranzo.custody.ui.theme.White

@Composable
fun BuyScreen(onBack: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var selectedToken by remember { mutableStateOf("ETH") }
    var selectedPayment by remember { mutableIntStateOf(0) }

    val currencies = listOf("USD", "EUR", "GBP")
    val tokens = listOf("ETH", "BTC", "SOL", "USDC")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Black)
            }
            Text("Buy Crypto", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("You Pay", style = MaterialTheme.typography.labelLarge, color = TextMuted)
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("0.00", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Black, unfocusedBorderColor = BorderColor, cursorColor = Black),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                currencies.forEach { cur ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (cur == selectedCurrency) Modifier.background(Black)
                                else Modifier.border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            )
                            .clickable { selectedCurrency = cur }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(cur, style = MaterialTheme.typography.labelSmall, color = if (cur == selectedCurrency) White else Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("You Receive", style = MaterialTheme.typography.labelLarge, color = TextMuted)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tokens.forEach { token ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (token == selectedToken) Modifier.border(2.dp, Black, RoundedCornerShape(12.dp))
                            else Modifier.border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        )
                        .clickable { selectedToken = token }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(token, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Payment Method", style = MaterialTheme.typography.labelLarge, color = TextMuted)
        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            Triple(Icons.Default.CreditCard, "Debit/Credit Card", "Visa, Mastercard"),
            Triple(Icons.Default.AccountBalance, "Bank Transfer", "ACH, SEPA")
        ).forEachIndexed { index, (icon, title, subtitle) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (index == selectedPayment) Modifier.border(2.dp, Black, RoundedCornerShape(12.dp))
                        else Modifier.border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    )
                    .clickable { selectedPayment = index }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Black, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
            if (index == 0) Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onBack() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Black, contentColor = White),
            enabled = amount.isNotBlank()
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
