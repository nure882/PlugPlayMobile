package com.plugplay.plugplaymobile.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plugplay.plugplaymobile.presentation.auth.LoginScreen
import com.plugplay.plugplaymobile.presentation.auth.RegisterScreen
import com.plugplay.plugplaymobile.presentation.product_list.ProductListScreen
import com.plugplay.plugplaymobile.presentation.profile.ProfileScreen

// 💡 Определяем маршруты (Routes)
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PRODUCT_LIST = "product_list"
    const val PROFILE = "profile" // 💡 НОВИЙ МАРШРУТ
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Начальный экран - Login
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        // 1. ЕКРАН ВХОДУ
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // При успішному вході переходимо в каталог і видаляємо стек (щоб не можна було повернутися)
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // 2. ЕКРАН РЕЄСТРАЦІЇ
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    // При успішній реєстрації переходимо в каталог
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Очищаємо весь стек до логіна
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 3. ГОЛОВНИЙ ЕКРАН (Каталог товарів)
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) } // 💡 ПЕРЕХІД НА ПРОФІЛЬ
            )
        }

        // 4. ЕКРАН ПРОФІЛЮ 💡 НОВИЙ COMPOSABLE
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToCatalog = { navController.navigate(Routes.PRODUCT_LIST) }, // 💡 ПЕРЕХІД У КАТАЛОГ
                onNavigateToProfile = { /* Вже на профілі */ }
            )
        }
    }
}
