package com.tranzo.custody.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Resolves the hosting [Activity] from a Compose [Context]. */
private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** Composition local providing the current [TranzoAppTheme] for direct color access. */
val LocalTranzoTheme = compositionLocalOf { AppThemes.getValue(AppThemeId.CLASSIC) }

@Composable
fun TranzoTheme(
    themeId: AppThemeId = AppThemeId.CLASSIC,
    fontId: AppFontId = AppFontId.INTER,
    content: @Composable () -> Unit
) {
    val appTheme = AppThemes[themeId] ?: AppThemes.getValue(AppThemeId.CLASSIC)

    val colorScheme = if (appTheme.isDark) {
        darkColorScheme(
            primary = appTheme.primary,
            onPrimary = appTheme.onPrimary,
            primaryContainer = appTheme.surfaceSecondary,
            onPrimaryContainer = appTheme.textPrimary,
            secondary = appTheme.textSecondary,
            onSecondary = appTheme.onPrimary,
            secondaryContainer = appTheme.surfaceSecondary,
            onSecondaryContainer = appTheme.textPrimary,
            tertiary = appTheme.positive,
            onTertiary = appTheme.onPrimary,
            background = appTheme.background,
            onBackground = appTheme.textPrimary,
            surface = appTheme.surface,
            onSurface = appTheme.textPrimary,
            surfaceVariant = appTheme.cardBackground,
            onSurfaceVariant = appTheme.textSecondary,
            outline = appTheme.border,
            outlineVariant = appTheme.divider,
            error = appTheme.negative,
            onError = appTheme.onPrimary,
        )
    } else {
        lightColorScheme(
            primary = appTheme.primary,
            onPrimary = appTheme.onPrimary,
            primaryContainer = appTheme.surfaceSecondary,
            onPrimaryContainer = appTheme.textPrimary,
            secondary = appTheme.textSecondary,
            onSecondary = appTheme.onPrimary,
            secondaryContainer = appTheme.surfaceSecondary,
            onSecondaryContainer = appTheme.textPrimary,
            tertiary = appTheme.positive,
            onTertiary = appTheme.onPrimary,
            background = appTheme.background,
            onBackground = appTheme.textPrimary,
            surface = appTheme.surface,
            onSurface = appTheme.textPrimary,
            surfaceVariant = appTheme.cardBackground,
            onSurfaceVariant = appTheme.textSecondary,
            outline = appTheme.border,
            outlineVariant = appTheme.divider,
            error = appTheme.negative,
            onError = appTheme.onPrimary,
        )
    }

    val fontFamily = AppFonts[fontId] ?: AppFonts.getValue(AppFontId.INTER)
    val typography = buildTypography(fontFamily)

    val activity = LocalContext.current.findActivity()
    val view = LocalView.current
    if (!view.isInEditMode && activity != null) {
        SideEffect {
            val window = activity.window
            window.statusBarColor = appTheme.background.toArgb()
            window.navigationBarColor = appTheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !appTheme.isDark
            insetsController.isAppearanceLightNavigationBars = !appTheme.isDark
        }
    }

    CompositionLocalProvider(LocalTranzoTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
