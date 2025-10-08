package com.plugplay.plugplaymobile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument // НОВИЙ ІМПОРТ
import androidx.navigation.NavType // НОВИЙ ІМПОРТ
import com.plugplay.plugplaymobile.presentation.auth.LoginScreen
import com.plugplay.plugplaymobile.presentation.auth.RegisterScreen
import com.plugplay.plugplaymobile.presentation.product_list.ProductListScreen
import com.plugplay.plugplaymobile.presentation.profile.ProfileScreen
import com.plugplay.plugplaymobile.presentation.product_detail.ItemDetailScreen // НОВИЙ ІМПОРТ (Для наступного етапу)

object Routes {
    const val PRODUCT_LIST = "product_list"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // [ЗМІНА]: Переименовано і змінено аргумент на {itemId} для консистентності з Item.kt
    const val ITEM_DETAIL = "detail_list/{itemId}"
}

/**
 * Вспомогательная функция для создания маршрута деталей товара с конкретным ID.
 */
fun createItemDetailRoute(itemId: String) = "detail_list/$itemId"

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.PRODUCT_LIST, // Починаємо з Каталогу
        modifier = modifier
    ) {
        // Екран Каталогу
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE) {
                        // Щоб уникнути створення дублікатів
                        launchSingleTop = true
                    }
                },
                // [ДОДАНО]: Нова функція для переходу на деталі товару
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(createItemDetailRoute(itemId))
                }
            )
        }

        // Екран Профілю (з умовним відображенням)
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToCatalog = {
                    navController.navigate(Routes.PRODUCT_LIST) {
                        // Щоб уникнути створення дублікатів
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        // Екран Входу
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Після успішного входу повертаємося на екран Профілю
                    navController.navigate(Routes.PROFILE) {
                        // Видаляємо всі екрани аутентифікації з бекстеку
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
                // Видалено onNavigateBack
            )
        }

        // Екран Реєстрації
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Після успішної реєстрації повертаємося на екран Профілю
                    navController.navigate(Routes.PROFILE) {
                        // Видаляємо всі екрани аутентифікації з бекстеку
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    // Просто повертаємося на екран входу
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                // Видалено onNavigateBack
            )
        }

        // [НОВИЙ КОМПОЗИТОР] Екран Деталей Товару
        composable(
            route = Routes.ITEM_DETAIL, // Шаблон маршрута detail_list/{itemId}
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType // Тип аргумента - строка
                }
            )
        ) { backStackEntry ->
            // Извлечение ID товара
            val itemId = backStackEntry.arguments?.getString("itemId")

            if (itemId != null) {
                ItemDetailScreen(
                    itemId = itemId,
                    navController = navController
                )
            } else {
                // Если ID отсутствует, возвращаемся в Каталог
                navController.popBackStack(Routes.PRODUCT_LIST, inclusive = false)
            }
        }
    }
}