package com.manzil.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Material 3 Expressive Theme - BS SE Edition
private val TealPrimary = Color(0xFF0F766E)
private val TealDark = Color(0xFF115E59)
private val Success = Color(0xFF16A34A)
private val Warning = Color(0xFFD97706)
private val Danger = Color(0xFFDC2626)
private val Info = Color(0xFF2563EB)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF4A635E),
    tertiary = Info,
    error = Danger,
    background = Color(0xFFFBFDF9),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8F5E9)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5DD8C7),
    onPrimary = Color(0xFF003730),
    primaryContainer = TealDark,
    secondary = Color(0xFFB1CCC5),
    tertiary = Color(0xFF8FB7FF),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2B2930),
    surfaceVariant = Color(0xFF3A3A3A)
)

@Composable
fun ManzilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
