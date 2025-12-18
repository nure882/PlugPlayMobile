package com.plugplay.plugplaymobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color




private val PlugPlayBlue = Color(0xFF1E88E5)
private val PlugPlayLightBlue = Color(0xFF64B5F6)
private val PlugPlayDarkBlue = Color(0xFF005CBF)


private val LightColorScheme = lightColorScheme(
    primary = PlugPlayBlue,
    onPrimary = Color.White,
    secondary = PlugPlayLightBlue,
    tertiary = PlugPlayDarkBlue,
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,

    outlineVariant = Color(0xFFE0E0E0),

    surfaceContainerHigh = Color(0xFFF0F0F0)
)

@Composable
fun PlugPlayMobileTheme(
    darkTheme: Boolean = false,

    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
