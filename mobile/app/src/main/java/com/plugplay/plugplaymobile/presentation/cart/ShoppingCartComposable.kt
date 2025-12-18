package com.plugplay.plugplaymobile.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete // [НОВИЙ ІМПОРТ]
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.domain.model.CartItem
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ShoppingCartDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    if (!isOpen) return

    val state by viewModel.state.collectAsState()
    val cartItems = state.cartItems

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cart", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Закрити")
                    }
                }
                HorizontalDivider()

                // Content
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        cartItems.isEmpty() -> EmptyCartPlaceholder(Modifier.align(Alignment.Center))
                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(cartItems, key = { it.id }) { item ->
                                    CartItemRow(
                                        item = item,
                                        onQuantityChange = { newQty ->
                                            viewModel.updateQuantity(item.id, newQty)
                                        },
                                        onDelete = { viewModel.deleteItem(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer (Total and Actions)
                if (cartItems.isNotEmpty()) {
                    CartFooter(
                        subtotal = state.subtotal,
                        onClearCart = viewModel::clearCart,
                        onNavigateToCheckout = onNavigateToCheckout
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    // [ОНОВЛЕНО] Використовуємо форматування для числа з "₴"
    val formattedUnitPrice = remember(item.unitPrice) {
        val format = NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        "Price: ${format.format(item.unitPrice)} ₴" // <-- ДОДАНО " ₴"
    }

    val formattedTotal = remember(item.total) {
        val format = NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        "Total: ${format.format(item.total)} ₴" // <-- ДОДАНО " ₴"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Image (Left)
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F0)),
            contentScale = ContentScale.Crop
        )

        // 2. Content Column (Text Info + Quantity Controls + Delete)
        Column(modifier = Modifier.weight(1f)) {

            // A. Top Row: Name and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Name (Left side of content row)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formattedUnitPrice, // <-- ВИКОРИСТАННЯ НОВОГО ФОРМАТУ
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                    Text(
                        text = formattedTotal, // <-- ВИКОРИСТАННЯ НОВОГО ФОРМАТУ
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Delete Button (Far right of content row)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Видалити", tint = Color.Gray)
                }
            }

            Spacer(Modifier.height(12.dp)) // Збільшений відступ між інфо та контролами

            // B. Bottom Row: Quantity Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Обмежуємо ширину, щоб не займати весь простір
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                IconButton(
                    onClick = { onQuantityChange(item.quantity - 1) },
                    enabled = item.quantity > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Зменшити", Modifier.size(20.dp))
                }
                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Збільшити", Modifier.size(20.dp))
                }
            }
        }
    }
}

// [ОНОВЛЕНО] Структура CartFooter
@Composable
fun CartFooter(
    subtotal: Double,
    onClearCart: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    // [ОНОВЛЕНО] Використовуємо форматування для числа з "₴"
    val formattedSubtotal = remember(subtotal) {
        val format = NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        format.format(subtotal) + " ₴" // <-- ДОДАНО " ₴"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F7F8)) // Light gray background
            .padding(16.dp)
    ) {
        // 1. Total Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = formattedSubtotal,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 2. Buttons Column (Симетрично одна під одною)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // A. Order Now Button
            Button(
                onClick = onNavigateToCheckout,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Order now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // B. Clear Cart Button (ОНОВЛЕНО: Тепер це OutlinedButton з іконкою)
            OutlinedButton(
                onClick = onClearCart,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), // Світло-червоний фон
                    contentColor = MaterialTheme.colorScheme.error // Червоний текст
                ),
                border = null // Прибираємо рамку
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Clear cart icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Clear cart", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EmptyCartPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your cart is empty", style = MaterialTheme.typography.titleLarge)
        Text("Add products to checkout", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}