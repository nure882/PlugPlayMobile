package com.plugplay.plugplaymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController // 💡 Важливий імпорт
import com.plugplay.plugplaymobile.presentation.AppNavigation
import com.plugplay.plugplaymobile.ui.theme.PlugPlayMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
// MainActivity.kt
// ...
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PlugPlayMobileTheme {
                // 💡 Створюємо navController тут
                val navController = rememberNavController()

                // 💡 Викликаємо наш навигаційний компонент з контролером
                AppNavigation(navController = navController)
            }
        }
    }
}
