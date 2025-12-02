package com.plugplay.plugplaymobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Базовые цветовые схемы (настроены под голубо-бело-черный стиль)

// Голубой акцент для бренда (например, PlugPlay Blue)
private val PlugPlayBlue = Color(0xFF1E88E5) // Средний, яркий голубой
private val PlugPlayLightBlue = Color(0xFF64B5F6) // Светлый голубой
private val PlugPlayDarkBlue = Color(0xFF005CBF) // Темный голубой


private val LightColorScheme = lightColorScheme(
    primary = PlugPlayBlue, // Голубой для кнопок/акцентов
    onPrimary = Color.White, // Белый текст на голубом (для контраста)
    secondary = PlugPlayLightBlue,
    tertiary = PlugPlayDarkBlue,
    background = Color.White, // Белый фон
    surface = Color.White, // Белая поверхность карточек
    onBackground = Color.Black, // Черный текст на белом фоне
    onSurface = Color.Black, // Черный текст на белой поверхности
    // Для OutlinedTextField и Divider используем outlineVariant
    outlineVariant = Color(0xFFE0E0E0),
    // Для Card background (surfaceContainerHigh)
    surfaceContainerHigh = Color(0xFFF0F0F0) // Очень светлый серый для карточек, как в LoginScreen
)

@Composable
fun PlugPlayMobileTheme(
    darkTheme: Boolean = false,
    // Динамические цвета Material 3 (пока не используем)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Используем дефолтную типографику
        content = content
    )
}
