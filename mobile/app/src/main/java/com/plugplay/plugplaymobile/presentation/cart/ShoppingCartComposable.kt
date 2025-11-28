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
                    Text("Корзина", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Закрити")
                    }
                }
                Divider()

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
    val format = NumberFormat.getCurrencyInstance(Locale("uk", "UA"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image Placeholder
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F0)),
            contentScale = ContentScale.Crop
        )

        // Info and Price
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ціна: ${format.format(item.unitPrice)}",
                color = MaterialTheme.colorScheme.error, // Red like in frontend
                fontSize = 12.sp
            )
            Text(
                text = "Сума: ${format.format(item.total)}",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Quantity Controls and Delete
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Видалити", tint = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
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
                    modifier = Modifier.width(20.dp),
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

@Composable
fun CartFooter(
    subtotal: Double,
    onClearCart: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale("uk", "UA"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F7F8)) // Light gray background
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Загальна сума", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = format.format(subtotal),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onNavigateToCheckout,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Оформити замовлення", fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onClearCart, modifier = Modifier.align(Alignment.End)) {
            Text("Очистити корзину", color = MaterialTheme.colorScheme.error)
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
        Text("Ваша корзина порожня", style = MaterialTheme.typography.titleLarge)
        Text("Додайте товари, щоб почати оформлення", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}