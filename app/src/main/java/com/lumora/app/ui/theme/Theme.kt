package com.lumora.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LumiaColorScheme = darkColorScheme(
    primary = LumiaPrimary,
    secondary = LumiaAccent,
    background = LumiaBackground,
    surface = LumiaSurface,
    surfaceVariant = LumiaSurfaceAlt,
    onPrimary = LumiaTextPrimary,
    onBackground = LumiaTextPrimary,
    onSurface = LumiaTextPrimary,
    error = LumiaDanger
)

@Composable
fun LumiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LumiaColorScheme,
        typography = LumiaTypography,
        content = content
    )
}
