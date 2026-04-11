package com.tranzo.custody.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium glassmorphism card — semi-transparent surface with
 * gradient border glow and subtle inner highlight.
 */
fun Modifier.glassCard(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.55f,
    shadowElevation: Dp = 0.dp
): Modifier = this
    .then(
        if (shadowElevation > 0.dp) {
            Modifier.shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color(0xFF635BFF).copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
        } else Modifier
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.verticalGradient(
            listOf(
                Color(0xFF1E2235).copy(alpha = alpha + 0.15f),
                Color(0xFF161929).copy(alpha = alpha)
            )
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.04f),
                Color.White.copy(alpha = 0.02f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Minimal card — clean, slightly elevated.
 */
fun Modifier.minimalCard(
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = Color(0xFF1E2235),
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color(0x1AFFFFFF),
    elevation: Dp = 0.dp
): Modifier = this
    .then(
        if (elevation > 0.dp) {
            Modifier.shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
        } else Modifier
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(cornerRadius))

/**
 * Glass card tuned for dark gradient backgrounds.
 */
fun Modifier.glassOnDark(
    cornerRadius: Dp = 20.dp,
    alpha: Float = 0.4f
): Modifier = this.glassCard(
    cornerRadius = cornerRadius,
    alpha = alpha,
    shadowElevation = 4.dp
)

/**
 * Subtle accent-tinted glass — for active/selected states.
 */
fun Modifier.glassAccent(
    cornerRadius: Dp = 16.dp,
    accentColor: Color = Color(0xFF635BFF),
    alpha: Float = 0.12f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.verticalGradient(
            listOf(
                accentColor.copy(alpha = alpha),
                accentColor.copy(alpha = alpha * 0.5f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.3f),
                accentColor.copy(alpha = 0.08f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Subtle glow effect behind elements (accent halo).
 */
fun Modifier.accentGlow(
    accentColor: Color = Color(0xFF635BFF),
    cornerRadius: Dp = 20.dp,
    glowAlpha: Float = 0.15f
) = this.shadow(
    elevation = 16.dp,
    shape = RoundedCornerShape(cornerRadius),
    ambientColor = accentColor.copy(alpha = glowAlpha),
    spotColor = accentColor.copy(alpha = glowAlpha)
)
