package com.tranzo.custody.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.tranzo.custody.R

/**
 * All available app font families.
 * Uses Google Fonts provider to download fonts at runtime.
 */
enum class AppFontId(val displayName: String) {
    INTER("Inter"),
    POPPINS("Poppins"),
    SPACE_GROTESK("Space Grotesk"),
    JETBRAINS_MONO("JetBrains Mono"),
    DM_SANS("DM Sans"),
    OUTFIT("Outfit");
}

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun googleFontFamily(name: String): FontFamily {
    val font = GoogleFont(name)
    return FontFamily(
        Font(googleFont = font, fontProvider = googleFontProvider, weight = FontWeight.Normal),
        Font(googleFont = font, fontProvider = googleFontProvider, weight = FontWeight.Medium),
        Font(googleFont = font, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = font, fontProvider = googleFontProvider, weight = FontWeight.Bold),
        Font(googleFont = font, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
    )
}

val AppFonts: Map<AppFontId, FontFamily> = mapOf(
    AppFontId.INTER to googleFontFamily("Inter"),
    AppFontId.POPPINS to googleFontFamily("Poppins"),
    AppFontId.SPACE_GROTESK to googleFontFamily("Space Grotesk"),
    AppFontId.JETBRAINS_MONO to googleFontFamily("JetBrains Mono"),
    AppFontId.DM_SANS to googleFontFamily("DM Sans"),
    AppFontId.OUTFIT to googleFontFamily("Outfit"),
)
