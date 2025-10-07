package com.plugplay.plugplaymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.plugplay.plugplaymobile.presentation.AppNavigation // Импорт навигации
import com.plugplay.plugplaymobile.ui.theme.PlugPlayMobileTheme // 💡 ИСПРАВЛЕНИЕ: Импорт темы
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PlugPlayMobileTheme {
                // Теперь приложение запускает наш навигационный граф,
                // начиная с LoginScreen
                AppNavigation()
            }
        }
    }
}
