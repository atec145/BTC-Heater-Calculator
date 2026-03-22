package com.example.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LightBlueDark,
    onPrimary = DarkBlueDark,
    secondary = LightGreenDark,
    onSecondary = DarkBlueDark,
    background = DarkBlueDark,
    surface = DarkBlueDark,
    onBackground = White,
    onSurface = White,
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBlue,
    onPrimary = White,
    secondary = LightGreen,
    onSecondary = White,
    tertiary = LightGreenVariant,
    background = White,
    surface = LightSurface,
    onBackground = DarkBlue,
    onSurface = DarkBlue,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
