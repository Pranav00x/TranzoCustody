package com.tranzo.custody.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A modifier that applies a premium minimal card look.
 * Replaces the old glassmorphism effect with a clean, modern aesthetic.
 */
fun Modifier.minimalCard(
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = Color.White,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color(0x0D000000), // Very subtle black border
    elevation: Dp = 0.dp
): Modifier = this
    .then(
        if (elevation > 0.dp) {
            Modifier.shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
        } else Modifier
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(
        width = borderWidth,
        color = borderColor,
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Compatibility alias for glassCard to minimalCard.
 */
fun Modifier.glassCard(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.15f,
    shadowElevation: Dp = 0.dp
): Modifier = this.minimalCard(
    cornerRadius = cornerRadius,
    backgroundColor = Color.White.copy(alpha = 0.6f + (alpha * 0.4f)), // Lighter but still has some transparency if needed
    borderWidth = borderWidth,
    borderColor = Color.Black.copy(alpha = 0.08f),
    elevation = shadowElevation
)

fun Modifier.glassOnDark(
    cornerRadius: Dp = 20.dp,
    alpha: Float = 0.2f
): Modifier = this.minimalCard(
    cornerRadius = cornerRadius,
    backgroundColor = Color(0xFF1A1A1A).copy(alpha = 0.95f), // Minimal dark card
    borderWidth = 1.dp,
    borderColor = Color.White.copy(alpha = 0.1f)
)
