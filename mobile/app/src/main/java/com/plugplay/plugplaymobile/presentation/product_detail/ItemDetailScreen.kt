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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
    navController: NavController,
    viewModel: ItemDetailViewModel = hiltViewModel(),
    onNavigateToCheckout: () -> Unit,
    onNavigateToProfile: () -> Unit,
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
                title = { Text("Plug & Play") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }, actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Пошук")
                    }
                    IconButton(onClick = onNavigateToProfile) {
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
                })
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Text(
                        text = state.error.toString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                state.item != null -> {
                    ItemDetailContent(
                        item = state.item!!,
                        isInCart = isInCart,
                        onAddToCart = { cartViewModel.addToCart(state.item!!.id, 1) },
                        onBuyClick = {
                            cartViewModel.addToCart(state.item!!.id, 1)
                            onNavigateToCheckout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemDetailContent(
    item: Item,
    isInCart: Boolean,
    onAddToCart: () -> Unit,
    onBuyClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            ImagePager(item.imageUrls.firstOrNull() ?: "")
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
                onAddToCart = onAddToCart,
                onBuyClick = onBuyClick
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
        Button(
            onClick = onBuyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = item.isAvailable
        ) {
            Text("Купити", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))

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
            Text(if (isInCart) "Вже в корзині" else "Додати в корзину", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ImagePager(imageUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Зображення товару",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        IconButton(
            onClick = { }, modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "В обране")
        }
    }
}

@Composable
fun TitleAndPrice(item: Item) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("uk", "UA"))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Наявність",
                tint = Color.Green,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (item.isAvailable) "Є в наявності" else "Немає в наявності",
                color = Color.Green,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currencyFormat.format(item.price),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun VariantSelectors() {
    var selectedColor by remember { mutableStateOf("Чорний") }
    var selectedStorage by remember { mutableStateOf("256GB") }

    Column {
        Text("Колір: $selectedColor", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(
                        BorderStroke(
                            2.dp,
                            if (selectedColor == "Чорний") MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        CircleShape
                    )
                    .clickable { selectedColor = "Чорний" }
            )
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Blue)
                    .border(
                        BorderStroke(
                            2.dp,
                            if (selectedColor == "Синій") MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        CircleShape
                    )
                    .clickable { selectedColor = "Синій" }
            )
        }
    }

    Spacer(Modifier.height(24.dp))

    Column {
        Text("Пам'ять:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("256GB", "512GB", "1TB").forEach { storage ->
                OutlinedButton(
                    onClick = { selectedStorage = storage },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedStorage == storage) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedStorage == storage) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                    )
                ) {
                    Text(storage, fontWeight = if (selectedStorage == storage) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
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
        Text("Доставка и гарантія", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        InfoRow(Icons.Outlined.LocalShipping, "Быстрая доставка", "Відправка в день замовлення")
        Divider(Modifier.padding(vertical = 8.dp))
        InfoRow(Icons.Outlined.Shield, "Гарантія 2 роки", "Офіційна гарантія від виробника")
        Divider(Modifier.padding(vertical = 8.dp))
        InfoRow(Icons.Outlined.Replay, "Возврат 14 дней", "Возможность вернуть товар")
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
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
        Text(
            "Опис товару", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = item.description, style = MaterialTheme.typography.bodyMedium
        )
    }
}