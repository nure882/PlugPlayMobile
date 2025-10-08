package com.plugplay.plugplaymobile.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plugplay.plugplaymobile.presentation.auth.LoginScreen
import com.plugplay.plugplaymobile.presentation.auth.RegisterScreen
import com.plugplay.plugplaymobile.presentation.product_list.ProductListScreen
import com.plugplay.plugplaymobile.presentation.profile.ProfileScreen

// 💡 Визначені маршрути
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PRODUCT_LIST = "product_list"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 💡 ПОЧАТКОВИЙ ЕКРАН - КАТАЛОГ ТОВАРІВ
    NavHost(navController = navController, startDestination = Routes.PRODUCT_LIST) {

        // 1. ЕКРАН ВХОДУ
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Після успішного входу повертаємося на екран профілю
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Очищаємо стек від аутентифікації
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
                    // Після успішної реєстрації повертаємося на екран профілю
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Очищаємо стек (якщо перейшли з логіну)
                        popUpTo(Routes.REGISTER) { inclusive = true } // Видаляємо екран реєстрації
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 3. ЕКРАН КАТАЛОГУ
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        // 4. ЕКРАН ПРОФІЛЮ
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToCatalog = { navController.navigate(Routes.PRODUCT_LIST) },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) } // Перехід на вхід
            )
        }
    }
}
