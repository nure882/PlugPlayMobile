package com.plugplay.plugplaymobile.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plugplay.plugplaymobile.presentation.auth.LoginScreen
import com.plugplay.plugplaymobile.presentation.auth.RegisterScreen // Импорт нового экрана
import com.plugplay.plugplaymobile.presentation.product_list.ProductListScreen

// 💡 Определяем маршруты (Routes)
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PRODUCT_LIST = "product_list"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Начальный экран - Login
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        // 1. ЭКРАН ВХОДА
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // При успешном входе переходим в каталог и удаляем стек (чтобы нельзя было вернуться)
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // 2. ЭКРАН РЕГИСТРАЦИИ
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    // При успешной регистрации переходим в каталог
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // Очищаем весь стек до логина
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 3. ГЛАВНЫЙ ЭКРАН (Каталог товаров)
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen()
        }
    }
}
