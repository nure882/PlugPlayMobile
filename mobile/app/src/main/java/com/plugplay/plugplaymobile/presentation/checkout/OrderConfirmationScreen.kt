package com.plugplay.plugplaymobile.presentation.checkout

import android.content.Intent
import android.net.Uri
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

@Composable
fun OrderConfirmationScreen(
    onNavigateToCatalog: () -> Unit,
    cartViewModel: CartViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel // Отримуємо ViewModel через Hilt
) {
    val context = LocalContext.current
    val paymentUrl by paymentViewModel.paymentUrl.collectAsState()
    val isPaymentLoading by paymentViewModel.isLoading.collectAsState()

    // Слухаємо появу URL для оплати
    LaunchedEffect(paymentUrl) {
        paymentUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            paymentViewModel.onPaymentUrlOpened()
        }
    }

    // Очищаємо кошик при вході на екран
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
                text = "Your order has been successfully placed. You can pay now.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // Кнопка оплати LiqPay
            Button(
                onClick = { paymentViewModel.payForOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = !isPaymentLoading
            ) {
                if (isPaymentLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Pay Now via LiqPay", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToCatalog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Continue Shopping", fontWeight = FontWeight.Bold)
            }
        }
    }
}