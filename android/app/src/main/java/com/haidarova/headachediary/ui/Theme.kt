package com.haidarova.headachediary.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Deep = Color(0xFF0B4F44)
val Mint = Color(0xFFA8E6CF)
val Mid = Color(0xFF5CC8A1)
val Pale = Color(0xFFD8F5E8)
val Bg = Color(0xFFEFFAF5)
val Ink = Color(0xFF0E2F2A)
val Txt = Color(0xFF3F5F57)
val Line = Color(0xFFCDEBDD)
val Accent = Color(0xFFFF8A65)
val Amber = Color(0xFFF2C94C)

private val Scheme = lightColorScheme(
    primary = Deep,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = Deep,
    secondary = Mid,
    onSecondary = Deep,
    secondaryContainer = Pale,
    onSecondaryContainer = Deep,
    tertiary = Accent,
    onTertiary = Color.White,
    background = Bg,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Pale,
    onSurfaceVariant = Txt,
    outline = Line
)

@Composable
fun HeadacheTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}

fun painColor(intensity: Int): Color = when {
    intensity >= 7 -> Accent
    intensity >= 5 -> Amber
    else -> Mid
}
