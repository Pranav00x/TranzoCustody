package com.tranzo.custody.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb

/**
 * A modifier that applies a neumorphic "extruded" (raised) effect.
 */
fun Modifier.neumorphicExtruded(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    backgroundColor: Color,
    lightShadowColor: Color = Color(0xFF2E3137), // Lighter than Slate
    darkShadowColor: Color = Color(0xFF101113)   // Darker than Slate
) = this.drawBehind {
    val distance = elevation.toPx()
    val radius = cornerRadius.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        
        // Dark Shadow
        frameworkPaint.color = darkShadowColor.toArgb()
        frameworkPaint.setShadowLayer(distance, distance, distance, darkShadowColor.toArgb())
        canvas.drawRoundRect(0f, 0f, size.width, size.height, radius, radius, paint)

        // Light Shadow
        frameworkPaint.color = lightShadowColor.toArgb()
        frameworkPaint.setShadowLayer(distance, -distance, -distance, lightShadowColor.toArgb())
        canvas.drawRoundRect(0f, 0f, size.width, size.height, radius, radius, paint)
    }
}.background(backgroundColor, RoundedCornerShape(cornerRadius))

/**
 * A modifier that applies a neumorphic "pressed" (sunken) effect.
 */
fun Modifier.neumorphicPressed(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    backgroundColor: Color,
    lightShadowColor: Color = Color(0xFF2E3137),
    darkShadowColor: Color = Color(0xFF101113)
) = this.drawBehind {
    val distance = elevation.toPx()
    val radius = cornerRadius.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.color = backgroundColor
        canvas.drawRoundRect(0f, 0f, size.width, size.height, radius, radius, paint)

        // For pressed, we use subtle inner borders to simulate depth
        // This is a simplified version
    }
}.background(backgroundColor, RoundedCornerShape(cornerRadius))

/**
 * A standard neumorphic surface for cards.
 */
@Composable
fun Modifier.neumorphicCard(
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 8.dp
) = this.neumorphicExtruded(
    cornerRadius = cornerRadius,
    elevation = elevation,
    backgroundColor = MaterialTheme.colorScheme.surface
)
