package com.tranzo.custody.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Resolves the hosting [Activity] from a Compose [Context] (unwraps [ContextThemeWrapper]). */
private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private val TranzoColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = SurfaceSecondary,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = White,
    secondaryContainer = SurfaceSecondary,
    onSecondaryContainer = TextPrimary,
    tertiary = Positive,
    onTertiary = White,
    background = Background,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = DividerColor,
    error = Negative,
    onError = White
)

@Composable
fun TranzoTheme(content: @Composable () -> Unit) {
    val activity = LocalContext.current.findActivity()
    val view = LocalView.current
    if (!view.isInEditMode && activity != null) {
        SideEffect {
            val window = activity.window
            window.statusBarColor = White.toArgb()
            window.navigationBarColor = White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = TranzoColorScheme,
        typography = TranzoTypography,
        content = content
    )
}
