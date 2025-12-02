package com.plugplay.plugplaymobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.plugplay.plugplaymobile.presentation.AppNavigation
import com.plugplay.plugplaymobile.ui.theme.PlugPlayMobileTheme
import com.plugplay.plugplaymobile.util.LocaleHelper // Імпорт
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // [ДОДАНО] Перевизначаємо attachBaseContext, щоб форсувати мову
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "en"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlugPlayMobileTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}