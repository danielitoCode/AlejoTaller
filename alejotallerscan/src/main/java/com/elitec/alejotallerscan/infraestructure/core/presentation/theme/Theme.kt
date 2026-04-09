package com.elitec.alejotallerscan.infraestructure.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OperatorTeal,
    onPrimary = OperatorNight,
    primaryContainer = Color(0xFF123B45),
    onPrimaryContainer = Color(0xFFCFF6F1),
    secondary = OperatorBlue,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF18304A),
    onSecondaryContainer = Color(0xFFDCE9FF),
    tertiary = OperatorAmber,
    onTertiary = OperatorNight,
    tertiaryContainer = Color(0xFF4A3310),
    onTertiaryContainer = Color(0xFFFFE5BF),
    background = OperatorNight,
    onBackground = Color(0xFFEAF1F8),
    surface = Color(0xFF111B25),
    onSurface = Color(0xFFEAF1F8),
    surfaceVariant = Color(0xFF223041),
    onSurfaceVariant = Color(0xFFBFCCDB),
    outline = Color(0xFF708195),
    outlineVariant = Color(0xFF2D3A4B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = OperatorTeal,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = OperatorTealContainer,
    onPrimaryContainer = OperatorInk,
    secondary = OperatorBlue,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = OperatorBlueContainer,
    onSecondaryContainer = OperatorInk,
    tertiary = OperatorAmber,
    onTertiary = OperatorNight,
    tertiaryContainer = OperatorAmberContainer,
    onTertiaryContainer = Color(0xFF563300),
    background = OperatorMist,
    onBackground = OperatorInk,
    surface = OperatorCard,
    onSurface = OperatorInk,
    surfaceVariant = Color(0xFFE8EEF6),
    onSurfaceVariant = OperatorSlate,
    outline = Color(0xFF7A8796),
    outlineVariant = OperatorBorder,
    error = OperatorRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = OperatorRedContainer,
    onErrorContainer = Color(0xFF5D1616)
)

@Composable
fun AlejoTallerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
