package com.plugplay.plugplaymobile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.plugplay.plugplaymobile.presentation.auth.LoginScreen
import com.plugplay.plugplaymobile.presentation.auth.RegisterScreen
import com.plugplay.plugplaymobile.presentation.product_list.ProductListScreen
import com.plugplay.plugplaymobile.presentation.profile.ProfileScreen
import com.plugplay.plugplaymobile.presentation.product_detail.ItemDetailScreen
import com.plugplay.plugplaymobile.presentation.checkout.CheckoutScreen
import com.plugplay.plugplaymobile.presentation.checkout.CheckoutViewModel
import com.plugplay.plugplaymobile.presentation.checkout.OrderConfirmationScreen
import com.plugplay.plugplaymobile.presentation.payment.PaymentViewModel
import com.plugplay.plugplaymobile.presentation.wishlist.WishlistScreen

object Routes {
    const val PRODUCT_LIST = "product_list"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ITEM_DETAIL = "detail_list/{itemId}"
    const val CHECKOUT = "checkout"
    const val WISHLIST = "wishlist"

    // Маршрут приймає orderId і прапорець startPayment (true/false)
    const val ORDER_CONFIRMATION = "order_confirmation/{orderId}/{startPayment}"

    fun createOrderConfirmationRoute(orderId: Int, startPayment: Boolean) =
        "order_confirmation/$orderId/$startPayment"
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

        // Profile Screen
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToCatalog = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                onNavigateToWishlist = { navController.navigate(Routes.WISHLIST) }
            )
        }

        // Wishlist Screen
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
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Register Screen
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
                    onNavigateToProfile = {
                        navController.navigate(Routes.PROFILE)
                    }
                )
            } else {
                navController.popBackStack(Routes.PRODUCT_LIST, inclusive = false)
            }
        }

        // --- CHECKOUT SCREEN ---
        composable(Routes.CHECKOUT) {
            val checkoutViewModel: CheckoutViewModel = hiltViewModel()

            CheckoutScreen(
                viewModel = checkoutViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderConfirmed = {
                    val orderId = checkoutViewModel.lastOrderId
                    val shouldStartPayment = checkoutViewModel.shouldStartPayment

                    if (orderId != null) {
                        // Переходимо на екран підтвердження, передаючи прапорець оплати
                        navController.navigate(Routes.createOrderConfirmationRoute(orderId, shouldStartPayment)) {
                            popUpTo(Routes.CHECKOUT) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        // --- ORDER CONFIRMATION SCREEN ---
        composable(
            route = Routes.ORDER_CONFIRMATION,
            arguments = listOf(
                navArgument("orderId") { type = NavType.IntType },
                navArgument("startPayment") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId")
            val startPayment = backStackEntry.arguments?.getBoolean("startPayment") ?: false

            val paymentViewModel: PaymentViewModel = hiltViewModel()

            // Передаємо дані у ViewModel
            LaunchedEffect(orderId) {
                paymentViewModel.currentOrderId = orderId
            }

            // АВТО-ЗАПУСК ОПЛАТИ: Якщо startPayment == true, запускаємо процес одразу
            LaunchedEffect(startPayment) {
                if (startPayment) {
                    paymentViewModel.payForOrder()
                }
            }

            OrderConfirmationScreen(
                onNavigateToCatalog = {
                    navController.popBackStack(Routes.PRODUCT_LIST, inclusive = false)
                },
                paymentViewModel = paymentViewModel
            )
        }
    }
}