package com.tranzo.custody.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glass-style raised card surface.
 * Keeps the old signature for compatibility but renders as translucent glass.
 */
fun Modifier.neumorphicExtruded(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    backgroundColor: Color,
    lightShadowColor: Color = Color.Transparent,
    darkShadowColor: Color = Color.Transparent
) = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.verticalGradient(
            listOf(
                backgroundColor.copy(alpha = 0.85f),
                backgroundColor.copy(alpha = 0.65f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.03f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Glass-style inset/pressed surface.
 */
fun Modifier.neumorphicPressed(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    backgroundColor: Color,
    lightShadowColor: Color = Color.Transparent,
    darkShadowColor: Color = Color.Transparent
) = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.verticalGradient(
            listOf(
                backgroundColor.copy(alpha = 0.50f),
                backgroundColor.copy(alpha = 0.35f)
            )
        )
    )

/**
 * Composable glass card using MaterialTheme surface color.
 */
@Composable
fun Modifier.neumorphicCard(
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 8.dp
) = this.glassCard(cornerRadius = cornerRadius, shadowElevation = elevation.coerceAtMost(6.dp))
