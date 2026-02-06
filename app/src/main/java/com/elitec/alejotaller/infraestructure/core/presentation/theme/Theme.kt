package com.elitec.alejotaller.infraestructure.core.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF0F2F18),

    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFA5D6A7),

    secondary = Color(0xFF66BB6A),
    onSecondary = Color(0xFF0C2E16),

    secondaryContainer = Color(0xFF2E7D32),
    onSecondaryContainer = Color(0xFFA5D6A7),

    tertiary = Color(0xFF4CAF50),
    onTertiary = Color(0xFF0B2A14),

    background = Color(0xFF121412),
    onBackground = Color(0xFFE2E3DE),

    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DE),

    surfaceVariant = Color(0xFF44483E),
    onSurfaceVariant = Color(0xFFC4C8BE),

    outline = Color(0xFF8E9287),
    outlineVariant = Color(0xFF44483E),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF388E3C),
    onPrimary = Color(0xFFFFFFFF),

    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF102015),

    secondary = Color(0xFF4CAF50),
    onSecondary = Color(0xFFFFFFFF),

    secondaryContainer = Color(0xFFD8F3DC),
    onSecondaryContainer = Color(0xFF0F1F13),

    tertiary = Color(0xFF2E7D32),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFF6F8F5),
    onBackground = Color(0xFF1A1C19),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C19),

    surfaceVariant = Color(0xFFE0E3DD),
    onSurfaceVariant = Color(0xFF44483E),

    outline = Color(0xFF74796E),
    outlineVariant = Color(0xFFC4C8BE),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun AlejoTallerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}