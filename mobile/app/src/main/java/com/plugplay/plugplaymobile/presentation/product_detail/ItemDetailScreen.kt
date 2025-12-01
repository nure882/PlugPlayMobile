package com.plugplay.plugplaymobile.presentation.product_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.R
import com.plugplay.plugplaymobile.presentation.cart.CartViewModel
import com.plugplay.plugplaymobile.presentation.cart.ShoppingCartDialog
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    navController: NavController,
    onNavigateToCheckout: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val cartItemsCount = cartState.cartItems.sumOf { it.quantity }

    val item = state.item


    var isCartOpen by remember { mutableStateOf(false) }


    val isInCart = remember(cartState.cartItems, item) {
        if (item == null) return@remember false
        cartState.cartItems.any { it.productId == item.id }
    }


    ShoppingCartDialog(
        isOpen = isCartOpen,
        onClose = { isCartOpen = false },
        onNavigateToCheckout = {
            isCartOpen = false
            onNavigateToCheckout()
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Plug & Play",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Пошук */ }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Пошук")
                    }
                    IconButton(onClick = { /* TODO: Профіль */ }) {
                        Icon(Icons.Outlined.Person, contentDescription = "Профіль")
                    }
                    IconButton(onClick = { isCartOpen = true }) {
                        BadgedBox(
                            badge = {
                                if (cartItemsCount > 0) {
                                    Badge(
                                        modifier = Modifier.offset(x = (-6).dp, y = 4.dp),
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text(cartItemsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Корзина")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            when {
                state.isLoading -> {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                state.error != null -> {
                    item {
                        Text(
                            text = state.error.toString(),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
                item != null -> {
                    item {
                        ImagePager(item.imageUrls)
                    }

                    item {
                        Column(
                            Modifier
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            TitleAndPrice(item)
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    item {
                        ActionButtons(
                            item = item,
                            isInCart = isInCart,
                            onAddToCart = {
                                cartViewModel.addToCart(item.id, 1)
                            },
                            onBuyClick = {
                                cartViewModel.addToCart(item.id, 1)
                                isCartOpen = true
                            }
                        )
                    }

                    item {
                        InfoSection()
                    }

                    item {
                        DescriptionSection(item)
                    }
                }
            }
        }
    }
}


@Composable
fun ActionButtons(
    item: Item,
    isInCart: Boolean,
    onAddToCart: () -> Unit,
    onBuyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
    ) {
        // Buy Button
        Button(
            onClick = onBuyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = item.isAvailable,
            // [ОНОВЛЕНО КОЛІР КНОПКИ BUY]
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)) // Синій відтінок
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // [ДОДАНО ІКОНКУ КОШИКА]
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = "Buy",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                // [ЗМІНЕНО ТЕКСТ КНОПКИ]
                Text("Buy", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))

        // Add to Cart Button (змінюється на "Already in cart")
        OutlinedButton(
            onClick = onAddToCart,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = !isInCart && item.isAvailable,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isInCart) Color(0xFFF4F7F8) else Color.White,
                contentColor = if (isInCart) Color.Gray else MaterialTheme.colorScheme.onSurface,
                disabledContentColor = Color.Gray
            ),
            border = BorderStroke(
                1.dp,
                if (isInCart) Color(0xFFE0E0E0) else Color.Gray.copy(alpha = 0.5f)
            )
        ) {
            // [ЗМІНЕНО ТЕКСТ КНОПКИ]
            Text(if (isInCart) "Already in cart" else "Add to cart", fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun ImagePager(imageUrls: List<String>) {
    val mainImageUrl = imageUrls.firstOrNull()
        ?: "https://example.com/placeholder.jpg"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
        ) {
            AsyncImage(
                model = mainImageUrl,
                contentDescription = "Зображення товару",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_foreground)
            )

            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "В обране", tint = Color.White)
            }
        }
    }
}


@Composable
fun TitleAndPrice(item: Item) {
    val formattedNumber = remember(item.price) {
        val format = NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        format.format(item.price) + " ₴"
    }

    Text(
        text = item.name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (item.reviewCount > 0) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
            Text(
                text = " ${String.format("%.1f", item.averageRating)} (${item.reviewCount} відгук${if (item.reviewCount != 1) "и" else ""})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        } else {
            Text(
                text = "No feedback",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Наявність", tint = Color.Green, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = if (item.isAvailable) "In stock" else "Not in stock",
            color = Color.Green,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = formattedNumber,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


@Composable
fun InfoSection() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Delivery and warranty", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        InfoRow(Icons.Outlined.LocalShipping, "Fast delivery", "Delivery to Kyiv on the next day")
        Divider(Modifier.padding(vertical = 8.dp))

        InfoRow(Icons.Outlined.Shield, "1 year warranty", "Official manufacturer warranty")
        Divider(Modifier.padding(vertical = 8.dp))

        InfoRow(Icons.Outlined.Replay, "Return within 14 days", "Ability to return the product")
        Divider(Modifier.padding(vertical = 8.dp))

        InfoRow(Icons.Outlined.Archive, "Safe packaging", "Reliable protection during delivery")
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun DescriptionSection(item: Item) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}