package com.plugplay.plugplaymobile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.plugplay.plugplaymobile.presentation.auth.LoginScreen
import com.plugplay.plugplaymobile.presentation.auth.RegisterScreen
import com.plugplay.plugplaymobile.presentation.product_list.ProductListScreen
import com.plugplay.plugplaymobile.presentation.profile.ProfileScreen
import com.plugplay.plugplaymobile.presentation.product_detail.ItemDetailScreen

object Routes {
    const val PRODUCT_LIST = "product_list"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ITEM_DETAIL = "detail_list/{itemId}"
}

fun createItemDetailRoute(itemId: String) = "detail_list/$itemId"

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.PRODUCT_LIST,
        modifier = modifier
    ) {
        // ... (composable для PRODUCT_LIST, PROFILE залишаються без змін)
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE) {
                        launchSingleTop = true
                    }
                },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(createItemDetailRoute(itemId))
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToCatalog = {
                    navController.popBackStack()
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
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // Екран Реєстрації
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    // [ВИПРАВЛЕНО] Після успішної реєстрації ведемо на екран ЛОГІНУ
                    navController.navigate(Routes.LOGIN) {
                        // Видаляємо екран реєстрації з бекстеку
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                    // TODO: Тут можна додати SnackBar "Реєстрація успішна! Увійдіть."
                },
                onNavigateToLogin = {
                    // Повертаємося на екран входу
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ... (composable для ITEM_DETAIL залишається без змін)
        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")

            if (itemId != null) {
                ItemDetailScreen(
                    itemId = itemId,
                    navController = navController
                )
            } else {
                navController.popBackStack(Routes.PRODUCT_LIST, inclusive = false)
            }
        }
    }
}