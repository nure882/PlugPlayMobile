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
import com.plugplay.plugplaymobile.data.model.LiqPayInitResponse
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val PRODUCT_LIST = "product_list"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ITEM_DETAIL = "detail_list/{itemId}"
    const val CHECKOUT = "checkout"
    const val WISHLIST = "wishlist"

    // [FIX] Додаємо необов'язкові параметри data та signature
    const val ORDER_CONFIRMATION = "order_confirmation/{orderId}/{startPayment}?data={data}&signature={signature}"

    fun createOrderConfirmationRoute(
        orderId: Int,
        startPayment: Boolean,
        data: String? = null,
        signature: String? = null
    ): String {
        val base = "order_confirmation/$orderId/$startPayment"
        return if (data != null && signature != null) {
            // [FIX] Кодуємо параметри, оскільки це Base64 і може містити спецсимволи
            val encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8.toString())
            val encodedSig = URLEncoder.encode(signature, StandardCharsets.UTF_8.toString())
            "$base?data=$encodedData&signature=$encodedSig"
        } else {
            base
        }
    }
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
        // ... (Інші маршрути без змін) ...
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) { launchSingleTop = true } },
                onNavigateToItemDetail = { itemId -> navController.navigate(createItemDetailRoute(itemId)) },
                onNavigateToCheckout = { navController.navigate(Routes.CHECKOUT) }
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToCatalog = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                onNavigateToWishlist = { navController.navigate(Routes.WISHLIST) }
            )
        }
        composable(Routes.WISHLIST) {
            WishlistScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemDetail = { itemId -> navController.navigate(createItemDetailRoute(itemId)) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
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
        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            if (itemId != null) {
                ItemDetailScreen(
                    itemId = itemId,
                    navController = navController,
                    onNavigateToCheckout = { navController.navigate(Routes.CHECKOUT) },
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
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
                onNavigateBack = { navController.popBackStack() },
                onOrderConfirmed = {
                    // [FIX] Отримуємо orderId та paymentData зі стану
                    val state = checkoutViewModel.state.value
                    val orderId = state.orderId
                    val isCardPayment = state.selectedPaymentMethod == PaymentMethod.Card
                    val liqPayData = state.liqPayData

                    if (orderId != null) {
                        // [FIX] Передаємо дані LiqPay у маршрут, щоб не робити зайвий запит (і уникнути 404)
                        val route = Routes.createOrderConfirmationRoute(
                            orderId,
                            isCardPayment,
                            liqPayData?.data,
                            liqPayData?.signature
                        )

                        navController.navigate(route) {
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
                navArgument("startPayment") { type = NavType.BoolType },
                // [FIX] Приймаємо аргументи
                navArgument("data") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("signature") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId")
            val startPayment = backStackEntry.arguments?.getBoolean("startPayment") ?: false
            val data = backStackEntry.arguments?.getString("data")
            val signature = backStackEntry.arguments?.getString("signature")

            val paymentViewModel: PaymentViewModel = hiltViewModel()

            LaunchedEffect(orderId, data, signature) {
                paymentViewModel.currentOrderId = orderId
                // [FIX] Якщо дані передані, встановлюємо їх у ViewModel.
                // Це запобігає виклику initPayment, який дає 404.
                if (data != null && signature != null) {
                    paymentViewModel.paymentData = LiqPayInitResponse(data, signature)
                }
            }

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