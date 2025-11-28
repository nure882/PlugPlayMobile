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
import com.plugplay.plugplaymobile.presentation.checkout.CheckoutScreen // [НОВИЙ ІМПОРТ]

object Routes {
    const val PRODUCT_LIST = "product_list"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ITEM_DETAIL = "detail_list/{itemId}"
    const val CHECKOUT = "checkout" // <-- НОВИЙ МАРШРУТ
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
        // Product List Screen
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE) {
                        launchSingleTop = true
                    }
                },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(createItemDetailRoute(itemId))
                },
                onNavigateToCheckout = { // [НОВИЙ КОЛБЕК]
                    navController.navigate(Routes.CHECKOUT)
                }
            )
        }

        // Profile Screen (без змін)
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

        // Login Screen (без змін)
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
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

        // Register Screen (без змін)
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Item Detail Screen
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
                    navController = navController,
                    onNavigateToCheckout = { // [НОВИЙ КОЛБЕК]
                        navController.navigate(Routes.CHECKOUT)
                    }
                )
            } else {
                navController.popBackStack(Routes.PRODUCT_LIST, inclusive = false)
            }
        }

        // [НОВИЙ КОМПОЗАБЛ] Checkout Screen
        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}