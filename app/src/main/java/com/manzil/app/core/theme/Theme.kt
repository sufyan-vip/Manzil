package com.manzil.app.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ThemeMode { SYSTEM, LIGHT, DARK }

// ---------------------------------------------------------------------------------------------
// Palette — one calm accent (teal) plus four semantic colours used consistently everywhere.
// ---------------------------------------------------------------------------------------------
private val Teal = Color(0xFF0F766E)
private val TealLight = Color(0xFF14B8A6)
private val TealContainer = Color(0xFFB9EDE4)
private val TealDeep = Color(0xFF0B4F4A)
private val TealDarkPrimary = Color(0xFF5DD8C7)

private val ManzilLightScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealDeep,
    secondary = Color(0xFF3F6B63),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE9E3),
    onSecondaryContainer = Color(0xFF0B3B35),
    tertiary = Color(0xFF2563EB),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDBE6FF),
    onTertiaryContainer = Color(0xFF10265E),
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF1A1C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1B),
    surfaceVariant = Color(0xFFE6EFEC),
    onSurfaceVariant = Color(0xFF44514D),
    surfaceContainerHigh = Color(0xFFEDF3F1),
    outline = Color(0xFF9BAAA5),
    outlineVariant = Color(0xFFD3DFDB),
    error = Color(0xFFC0392B),
    onError = Color.White,
    errorContainer = Color(0xFFFBE3DF),
    onErrorContainer = Color(0xFF5A1109),
    scrim = Color(0x99000000)
)

private val ManzilDarkScheme = darkColorScheme(
    primary = TealDarkPrimary,
    onPrimary = Color(0xFF00332E),
    primaryContainer = Color(0xFF0E4A45),
    onPrimaryContainer = Color(0xFFB9EDE4),
    secondary = Color(0xFF9FCFC6),
    onSecondary = Color(0xFF0A3A34),
    secondaryContainer = Color(0xFF24443F),
    onSecondaryContainer = Color(0xFFCFE9E3),
    tertiary = Color(0xFF9DBEFF),
    onTertiary = Color(0xFF0F2A5C),
    tertiaryContainer = Color(0xFF1E355F),
    onTertiaryContainer = Color(0xFFDBE6FF),
    background = Color(0xFF0E1514),
    onBackground = Color(0xFFE2E9E6),
    surface = Color(0xFF141C1B),
    onSurface = Color(0xFFE2E9E6),
    surfaceVariant = Color(0xFF23302E),
    onSurfaceVariant = Color(0xFFBFCFCB),
    surfaceContainerHigh = Color(0xFF1B2523),
    outline = Color(0xFF6E7C78),
    outlineVariant = Color(0xFF33403D),
    error = Color(0xFFFF8A7A),
    onError = Color(0xFF4A0F07),
    errorContainer = Color(0xFF5A1F16),
    onErrorContainer = Color(0xFFFFDAD4),
    scrim = Color(0xCC000000)
)

data class ManzilSemanticColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val teal: Color,
    val priority1: Color,
    val priority2: Color,
    val priority3: Color,
    val priority4: Color,
    val heatEmpty: Color
) {
    fun priority(level: Int): Color = when (level.coerceIn(1, 4)) {
        1 -> priority1
        2 -> priority2
        3 -> priority3
        else -> priority4
    }

    val priorityLabels = mapOf(1 to "P1", 2 to "P2", 3 to "P3", 4 to "P4")
}

private val LightSemantic = ManzilSemanticColors(
    success = Color(0xFF16A34A),
    warning = Color(0xFFD97706),
    danger = Color(0xFFDC2626),
    info = Color(0xFF2563EB),
    teal = Teal,
    priority1 = Color(0xFFDC2626),
    priority2 = Color(0xFFD97706),
    priority3 = Color(0xFF2563EB),
    priority4 = Color(0xFF6B7280),
    heatEmpty = Color(0xFFE3EAE8)
)

private val DarkSemantic = ManzilSemanticColors(
    success = Color(0xFF4ADE80),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFFF8A7A),
    info = Color(0xFF93B4FF),
    teal = TealDarkPrimary,
    priority1 = Color(0xFFFF8A7A),
    priority2 = Color(0xFFFBBF24),
    priority3 = Color(0xFF93B4FF),
    priority4 = Color(0xFF98A2B3),
    heatEmpty = Color(0xFF243230)
)

val LocalManzilColors = staticCompositionLocalOf { LightSemantic }

object ManzilColors {
    val success: Color @Composable get() = LocalManzilColors.current.success
    val warning: Color @Composable get() = LocalManzilColors.current.warning
    val danger: Color @Composable get() = LocalManzilColors.current.danger
    val info: Color @Composable get() = LocalManzilColors.current.info
    val teal: Color @Composable get() = LocalManzilColors.current.teal
    val heatEmpty: Color @Composable get() = LocalManzilColors.current.heatEmpty

    @Composable
    fun priority(level: Int): Color = LocalManzilColors.current.priority(level)
}

private val ManzilShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

/** Tabular numerals so counters, timers and money never jitter while ticking. */
private fun TextStyle.tabular(): TextStyle =
    copy(fontFeatureSettings = "tnum")

private val AppTypography: Typography = Typography().let { base ->
    base.copy(
        displaySmall = base.displaySmall.tabular(),
        headlineMedium = base.headlineMedium.tabular(),
        headlineSmall = base.headlineSmall.tabular(),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = base.labelSmall.copy(letterSpacing = 0.6.sp),
        bodySmall = base.bodySmall.copy(lineHeight = 18.sp)
    )
}

@Composable
fun ManzilTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (mode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val scheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> ManzilDarkScheme
        else -> ManzilLightScheme
    }
    val semantic = if (dark) DarkSemantic else LightSemantic

    CompositionLocalProvider(LocalManzilColors provides semantic) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            shapes = ManzilShapes,
            content = content
        )
    }
}

/** Monospace-free way to render perfectly aligned digit columns. */
val TabularFont: FontFamily = FontFamily.Default
