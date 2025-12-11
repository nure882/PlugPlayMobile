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
import com.plugplay.plugplaymobile.presentation.checkout.CheckoutScreen
import com.plugplay.plugplaymobile.presentation.checkout.OrderConfirmationScreen
import com.plugplay.plugplaymobile.presentation.wishlist.WishlistScreen

object Routes {
    const val PRODUCT_LIST = "product_list"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ITEM_DETAIL = "detail_list/{itemId}"
    const val CHECKOUT = "checkout"
    const val ORDER_CONFIRMATION = "order_confirmation"
    const val WISHLIST = "wishlist"
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
                onNavigateToCheckout = {
                    navController.navigate(Routes.CHECKOUT)
                }
            )
        }

        // Profile Screen (без змін)
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToCatalog = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                onNavigateToWishlist = { navController.navigate(Routes.WISHLIST) } // <--- NEW
            )
        }

        // Wishlist Screen Route
        composable(Routes.WISHLIST) {
            WishlistScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(createItemDetailRoute(itemId))
                }
            )
        }

        // Login Screen
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
                },
                onNavigateBack = { // <--- ВИКОРИСТАННЯ: popBackStack для повернення на Register
                    navController.popBackStack()
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
                    onNavigateToCheckout = {
                        navController.navigate(Routes.CHECKOUT)
                    },
                    onNavigateToProfile = { // <--- ДОДАНО: передаємо функцію для навігації на екран профілю
                        navController.navigate(Routes.PROFILE)
                    }
                )
            } else {
                navController.popBackStack(Routes.PRODUCT_LIST, inclusive = false)
            }
        }

        // Checkout Screen
        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderConfirmed = {
                    navController.navigate(Routes.ORDER_CONFIRMATION) {
                        // Видаляємо Checkout з бек-стеку
                        popUpTo(Routes.CHECKOUT) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Order Confirmation Screen
        composable(Routes.ORDER_CONFIRMATION) {
            OrderConfirmationScreen(
                onNavigateToCatalog = {
                    navController.popBackStack(Routes.PRODUCT_LIST, inclusive = false)
                }
            )
        }
    }
}