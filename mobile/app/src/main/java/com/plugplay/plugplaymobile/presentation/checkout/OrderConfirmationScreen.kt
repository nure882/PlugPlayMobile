package com.plugplay.plugplaymobile.presentation.checkout

import android.app.Activity
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.presentation.cart.CartViewModel
import com.plugplay.plugplaymobile.presentation.payment.PaymentViewModel

// LiqPay SDK imports
import ua.privatbank.liqpay.LiqPay
import ua.privatbank.liqpay.base.EXTRA_LIQPAY_RESULT
import ua.privatbank.liqpay.base.LiqpayActivityResult
import ua.privatbank.liqpay.cardpay.InputData

@Composable
fun OrderConfirmationScreen(
    onNavigateToCatalog: () -> Unit,
    cartViewModel: CartViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel
) {
    val context = LocalContext.current
    val isPaymentLoading by paymentViewModel.isLoading.collectAsState()
    val shouldLaunchSdk by paymentViewModel.shouldLaunchSdk.collectAsState() // Слухаємо команду запуску
    val liqPayData = paymentViewModel.paymentData

    // 1. Створюємо лаунчер SDK
    val liqPayLauncher = rememberLauncherForActivityResult(
        contract = LiqPay.cardPay().createActivityResultContract()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val liqpayResult = intent?.getSerializableExtra(EXTRA_LIQPAY_RESULT) as? LiqpayActivityResult

            if (liqpayResult?.payment?.status == "success" || liqpayResult?.payment?.status == "sandbox") {
                Toast.makeText(context, "Payment Successful!", Toast.LENGTH_LONG).show()
                onNavigateToCatalog()
            } else {
                val errorMsg = liqpayResult?.exception?.message ?: liqpayResult?.payment?.errDescription ?: "Unknown error"
                Toast.makeText(context, "Payment Failed: $errorMsg", Toast.LENGTH_LONG).show()
            }
        } else {
            // Користувач закрив вікно оплати
            Toast.makeText(context, "Payment Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Функція для запуску
    fun launchPayment() {
        if (liqPayData != null) {
            try {
                val jsonBytes = Base64.decode(liqPayData.data, Base64.DEFAULT)
                val jsonString = String(jsonBytes)
                Log.e("LiqPay_Check", "ОТРИМАНІ ДАНІ: $jsonString")

                liqPayLauncher.launch(
                    InputData(
                        data = liqPayData.data,
                        signature = liqPayData.signature,
                    )
                )
                paymentViewModel.onSdkLaunched() // Скидаємо прапорець
            } catch (e: Exception) {
                Toast.makeText(context, "SDK Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 3. Автоматичний запуск, якщо ViewModel дала команду (через startPayment в навігації)
    LaunchedEffect(shouldLaunchSdk) {
        if (shouldLaunchSdk) {
            launchPayment()
        }
    }

    // Очищення кошика
    LaunchedEffect(Unit) {
        cartViewModel.clearCart()
    }

    Scaffold(
        containerColor = Color(0xFFF4F7F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Order Confirmed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Order successfully placed!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your order #${paymentViewModel.currentOrderId ?: ""} has been created.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            if (liqPayData != null) {
                Button(
                    onClick = { launchPayment() }, // Ручний запуск
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = !isPaymentLoading
                ) {
                    Text("Pay Now via LiqPay", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Payment details sent to email.", color = Color.Gray)
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToCatalog,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Continue Shopping", fontWeight = FontWeight.Bold)
            }
        }
    }
}