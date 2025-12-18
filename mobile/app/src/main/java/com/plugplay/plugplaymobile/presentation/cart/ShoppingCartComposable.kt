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
import androidx.compose.ui.text.style.TextAlign
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
        BoxWithConstraints {
            val isWide = maxWidth > 600.dp

            Card(
                modifier = Modifier
                    .fillMaxHeight(if (isWide) 0.8f else 0.9f)
                    .widthIn(min = 320.dp, max = 1100.dp)
                    .fillMaxWidth(if (isWide) 0.9f else 1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Shopping Cart",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    if (isWide && cartItems.isNotEmpty()) {
                        // --- ДЛЯ ШИРОКИХ ЭКРАНОВ: ДВЕ ПАНЕЛИ ---
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Левая часть: Список товаров
                            Box(modifier = Modifier.weight(1f)) {
                                LazyColumn(
                                    contentPadding = PaddingValues(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(cartItems, key = { it.id }) { item ->
                                        CartItemRow(
                                            item = item,
                                            isWide = true,
                                            onQuantityChange = { viewModel.updateQuantity(item.id, it) },
                                            onDelete = { viewModel.deleteItem(item.id) }
                                        )
                                    }
                                }
                            }

                            // Правая часть: Итоговая панель (Side Summary)
                            Column(
                                modifier = Modifier
                                    .width(320.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFF8FAFB))
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Items (${cartItems.sumOf { it.quantity }})", color = Color.Gray)
                                    // Форматирование цены вынесено в утилиту или делается по месту
                                    Text(formatPrice(state.subtotal), fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(Modifier.weight(1f))

                                Text("Total Amount", color = Color.Gray, fontSize = 14.sp)
                                Text(
                                    text = formatPrice(state.subtotal),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Button(
                                    onClick = onNavigateToCheckout,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Order now", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = viewModel::clearCart,
                                    modifier = Modifier.fillMaxWidth(),
                                    border = null,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Clear Cart")
                                }
                            }
                        }
                    } else {
                        // --- МОБИЛЬНЫЙ ВИД ИЛИ ПУСТАЯ КОРЗИНА ---
                        Box(modifier = Modifier.weight(1f)) {
                            if (cartItems.isEmpty()) {
                                EmptyCartPlaceholder(Modifier.align(Alignment.Center))
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(cartItems, key = { it.id }) { item ->
                                        CartItemRow(
                                            item = item,
                                            isWide = false,
                                            onQuantityChange = { viewModel.updateQuantity(item.id, it) },
                                            onDelete = { viewModel.deleteItem(item.id) }
                                        )
                                    }
                                }
                            }
                        }

                        if (cartItems.isNotEmpty()) {
                            CartFooter(
                                subtotal = state.subtotal,
                                isWide = false,
                                onClearCart = viewModel::clearCart,
                                onNavigateToCheckout = onNavigateToCheckout
                            )
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательная функция для форматирования цены (чтобы код был чище)
private fun formatPrice(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${format.format(amount)} ₴"
}

@Composable
fun CartItemRow(
    item: CartItem,
    isWide: Boolean,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val format = remember {
        NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val formattedUnitPrice = "${format.format(item.unitPrice)} ₴"
    val formattedTotal = "${format.format(item.total)} ₴"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            modifier = Modifier
                .size(if (isWide) 100.dp else 80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F0)),
            contentScale = ContentScale.Crop
        )

        if (isWide) {
            // Широкая перекомпоновка: все в одну линию
            Text(
                text = item.name,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                QuantitySelector(
                    quantity = item.quantity,
                    onQuantityChange = onQuantityChange,
                    modifier = Modifier.width(120.dp)
                )
                Text(text = "Price: $formattedUnitPrice", fontSize = 12.sp, color = Color.Gray)
            }

            Text(
                text = formattedTotal,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(100.dp),
                textAlign = TextAlign.End
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        } else {
            // Мобильная перекомпоновка (оставляем вертикальную структуру)
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(item.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
                Text(text = "Price: $formattedUnitPrice", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                Text(text = "Total: $formattedTotal", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                QuantitySelector(quantity = item.quantity, onQuantityChange = onQuantityChange)
            }
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    ) {
        IconButton(onClick = { onQuantityChange(quantity - 1) }, enabled = quantity > 1) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", Modifier.size(20.dp))
        }
        Text(text = quantity.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        IconButton(onClick = { onQuantityChange(quantity + 1) }) {
            Icon(Icons.Default.Add, contentDescription = "Increase", Modifier.size(20.dp))
        }
    }
}

@Composable
fun CartFooter(
    subtotal: Double,
    isWide: Boolean,
    onClearCart: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val format = remember {
        NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
    val formattedSubtotal = "${format.format(subtotal)} ₴"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F7F8))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = formattedSubtotal,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(16.dp))

        if (isWide) {
            // На широком экране кнопки в ряд
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onClearCart,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Clear cart")
                }
                Button(
                    onClick = onNavigateToCheckout,
                    modifier = Modifier.weight(2f).height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Order now", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // На мобилках кнопки друг под другом
            Button(onClick = onNavigateToCheckout, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Order now", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onClearCart, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Clear cart")
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