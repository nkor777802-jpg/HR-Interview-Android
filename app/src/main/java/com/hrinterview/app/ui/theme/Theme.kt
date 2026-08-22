package com.hrinterview.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hrinterview.app.domain.ThemeMode

private val LightColors = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = BrandNavySoft,
    onPrimaryContainer = BrandNavyDeep,
    secondary = BrandRed,
    onSecondary = Color.White,
    secondaryContainer = BrandRedSoft,
    onSecondaryContainer = BrandNavyDeep,
    tertiary = BrandNavy,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Color.Transparent,
    outline = BorderNeutral,
    outlineVariant = BorderNeutral,
    error = ErrorRed,
    onError = Color.White,
    inverseSurface = BrandNavyDeep,
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFF8FB0D8)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FB0D8),
    onPrimary = BrandNavyDeep,
    primaryContainer = Color(0xFF163456),
    onPrimaryContainer = Color(0xFFD5E3F4),
    secondary = Color(0xFFE57384),
    onSecondary = Color(0xFF3B0712),
    secondaryContainer = Color(0xFF5C1522),
    onSecondaryContainer = Color(0xFFFFD9DE),
    background = DarkBackground,
    onBackground = DarkOn,
    surface = DarkSurface,
    onSurface = DarkOn,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB7C2D1),
    surfaceTint = Color.Transparent,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = Color(0xFFFFB3B8),
    onError = Color(0xFF3B0712)
)

@Composable
fun HrInterviewTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
