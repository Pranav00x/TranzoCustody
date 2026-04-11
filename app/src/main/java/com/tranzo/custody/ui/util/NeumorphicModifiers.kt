package com.tranzo.custody.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clean card surface — replaces old neumorphic extruded effect.
 */
fun Modifier.neumorphicExtruded(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    backgroundColor: Color,
    lightShadowColor: Color = Color.Transparent,
    darkShadowColor: Color = Color.Transparent
) = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor, RoundedCornerShape(cornerRadius))
    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(cornerRadius))

/**
 * Clean pressed/inset surface — replaces old neumorphic pressed effect.
 */
fun Modifier.neumorphicPressed(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    backgroundColor: Color,
    lightShadowColor: Color = Color.Transparent,
    darkShadowColor: Color = Color.Transparent
) = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor, RoundedCornerShape(cornerRadius))

/**
 * Clean card surface.
 */
@Composable
fun Modifier.neumorphicCard(
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 8.dp
) = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(cornerRadius))
    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(cornerRadius))
