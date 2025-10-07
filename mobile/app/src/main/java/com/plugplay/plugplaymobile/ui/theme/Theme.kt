package com.plugplay.plugplaymobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Базовые цветовые схемы (можно настроить под бренд PlugPlay)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC), // Пурпурный
    secondary = Color(0xFF03DAC5), // Голубой
    tertiary = Color(0xFF3700B3), // Темно-пурпурный
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE), // Пурпурный
    secondary = Color(0xFF03DAC5), // Голубой
    tertiary = Color(0xFF3700B3), // Темно-пурпурный
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun PlugPlayMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Динамические цвета Material 3 (пока не используем)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Используем дефолтную типографику
        content = content
    )
}
