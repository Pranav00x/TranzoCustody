package com.tranzo.custody.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Brand-accurate background colors and gradients for popular tokens.
 */
object TokenColors {
    // Ethereum
    val ethPrimary = Color(0xFF627EEA)
    val ethSecondary = Color(0xFF3C3C3D)

    // Bitcoin
    val btcPrimary = Color(0xFFF7931A)
    val btcSecondary = Color(0xFFE8860D)

    // USDC (Circle)
    val usdcPrimary = Color(0xFF2775CA)
    val usdcSecondary = Color(0xFF1A5FA8)

    // USDT (Tether)
    val usdtPrimary = Color(0xFF26A17B)
    val usdtSecondary = Color(0xFF1E8C6A)

    // Polygon (POL / MATIC)
    val polPrimary = Color(0xFF8247E5)
    val polSecondary = Color(0xFF6B30C7)

    // Solana
    val solPrimary = Color(0xFF9945FF)
    val solSecondary = Color(0xFF14F195)

    // Arbitrum
    val arbPrimary = Color(0xFF28A0F0)
    val arbSecondary = Color(0xFF1B6CB0)

    // Base
    val basePrimary = Color(0xFF0052FF)
    val baseSecondary = Color(0xFF003AD6)

    // DAI
    val daiPrimary = Color(0xFFF5AC37)
    val daiSecondary = Color(0xFFD4922F)

    // BNB
    val bnbPrimary = Color(0xFFF0B90B)
    val bnbSecondary = Color(0xFFD4A30A)

    // LINK (Chainlink)
    val linkPrimary = Color(0xFF2A5ADA)
    val linkSecondary = Color(0xFF1F45AF)

    // AAVE
    val aavePrimary = Color(0xFFB6509E)
    val aaveSecondary = Color(0xFF9B3F85)

    // UNI (Uniswap)
    val uniPrimary = Color(0xFFFF007A)
    val uniSecondary = Color(0xFFD60066)

    // AVAX (Avalanche)
    val avaxPrimary = Color(0xFFE84142)
    val avaxSecondary = Color(0xFFB31212)

    // OP (Optimism)
    val opPrimary = Color(0xFFFF0420)
    val opSecondary = Color(0xFFD1031A)

    // ZK (zkSync)
    val zkPrimary = Color(0xFFFFFFFF)
    val zkSecondary = Color(0xFF000000)

    // MNT (Mantle)
    val mntPrimary = Color(0xFF000000)
    val mntSecondary = Color(0xFF282828)

    // BLAST
    val blastPrimary = Color(0xFFFCFC03)
    val blastSecondary = Color(0xFFD9D902)

    // Default
    val defaultPrimary = Color(0xFF6B7280)
    val defaultSecondary = Color(0xFF4B5563)

    fun getColors(symbol: String): Pair<Color, Color> = when (symbol.uppercase()) {
        "ETH", "WETH" -> ethPrimary to ethSecondary
        "BTC", "WBTC" -> btcPrimary to btcSecondary
        "USDC", "USDC.E" -> usdcPrimary to usdcSecondary
        "USDT" -> usdtPrimary to usdtSecondary
        "POL", "MATIC" -> polPrimary to polSecondary
        "SOL", "WSOL" -> solPrimary to solSecondary
        "ARB" -> arbPrimary to arbSecondary
        "BASE" -> basePrimary to baseSecondary
        "DAI" -> daiPrimary to daiSecondary
        "BNB" -> bnbPrimary to bnbSecondary
        "LINK" -> linkPrimary to linkSecondary
        "AAVE" -> aavePrimary to aaveSecondary
        "UNI" -> uniPrimary to uniSecondary
        "AVAX" -> avaxPrimary to avaxSecondary
        "OP" -> opPrimary to opSecondary
        "ZK" -> zkPrimary to zkSecondary
        "MNT" -> mntPrimary to mntSecondary
        "BLAST" -> blastPrimary to blastSecondary
        else -> defaultPrimary to defaultSecondary
    }

    fun getIconLetter(symbol: String): String = when (symbol.uppercase()) {
        "ETH", "WETH" -> "Ξ"
        "BTC", "WBTC" -> "₿"
        "USDC", "USDC.E" -> "$"
        "USDT" -> "₮"
        "POL", "MATIC" -> "P"
        "SOL", "WSOL" -> "◎"
        "DAI" -> "◈"
        "LINK" -> "⬡"
        "UNI" -> "🦄"
        "AVAX" -> "A"
        "OP" -> "O"
        "ZK" -> "Z"
        "MNT" -> "M"
        "BLAST" -> "B"
        else -> symbol.take(1).uppercase()
    }
}

/**
 * A branded token icon composable with gradient background and symbol character.
 * Uses brand-accurate colors for major cryptocurrencies.
 */
@Composable
fun TokenIcon(
    symbol: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val (primary, secondary) = TokenColors.getColors(symbol)
    val iconChar = TokenColors.getIconLetter(symbol)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(primary, secondary),
                    start = Offset(0f, 0f),
                    end = Offset(this.size.width, this.size.height)
                )
            )
        }
        Text(
            text = iconChar,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4f).sp
        )
    }
}
