package com.plugplay.plugplaymobile.presentation.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.presentation.cart.CartViewModel

@Composable
fun OrderConfirmationScreen(
    onNavigateToCatalog: () -> Unit, // Для повернення на головний екран
    viewModel: CartViewModel = hiltViewModel() // [ЗМІНА] Отримуємо ViewModel для роботи з кошиком
) {
    // [ЗМІНА] Очищаємо кошик при першому відображенні цього екрана (успішне замовлення)
    LaunchedEffect(Unit) {
        viewModel.clearCart()
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
            // Іконка успіху
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
                text = "Your order has been successfully placed. You will receive a confirmation email shortly.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            Button(
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