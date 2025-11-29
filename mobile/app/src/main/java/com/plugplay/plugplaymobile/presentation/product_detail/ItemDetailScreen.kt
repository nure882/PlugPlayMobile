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
import com.plugplay.plugplaymobile.presentation.cart.CartViewModel // [НОВИЙ ІМПОРТ]
import com.plugplay.plugplaymobile.presentation.cart.ShoppingCartDialog // [НОВИЙ ІМПОРТ]
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    navController: NavController,
    onNavigateToCheckout: () -> Unit, // [НОВИЙ АРГУМЕНТ]
    viewModel: ItemDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel() // [НОВИЙ VIEWMODEL]
) {
    val state by viewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState() // [CART STATE]
    val cartItemsCount = cartState.cartItems.sumOf { it.quantity } // Кількість товарів

    val item = state.item

    // Стан для відображення діалогу кошика
    var isCartOpen by remember { mutableStateOf(false) }

    // Перевіряємо, чи є товар уже в кошику (як у frontend/src/pages/ProductDetail.tsx)
    val isInCart = remember(cartState.cartItems, item) {
        if (item == null) return@remember false
        cartState.cartItems.any { it.productId == item.id }
    }

    // [ДОДАНО] Діалог кошика
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
            // [ОНОВЛЕНО TopAppBar]
            TopAppBar(
                title = { Text("Plug & Play") },
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
                    // [ОНОВЛЕНО] Кнопка корзини з лічильником
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
        // ВИПРАВЛЕНО: Використовуємо лише LazyColumn для заповнення всього доступного простору
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White), // Встановлюємо загальний БІЛИЙ фон
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
                    // --- Секція 1: Зображення ---
                    item {
                        ImagePager(item.imageUrls)
                    }

                    // --- Секція 2: Назва, ціна, варіанти ---
                    item {
                        Column(
                            Modifier
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            TitleAndPrice(item)
                            Spacer(Modifier.height(24.dp))
                            VariantSelectors() // Заглушка для кольору та пам'яті
                        }
                    }

                    // --- Секція 3: Кнопки ---
                    item {
                        // [ОНОВЛЕНО] Викликаємо оновлений ActionButtons
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

                    // --- Секція 4: Доставка та Гарантія ---
                    item {
                        InfoSection()
                    }

                    // --- Секція 5: Опис ---
                    item {
                        DescriptionSection(item)
                    }
                }
            }
        }
    }
}

// [УСУНЕНО] ItemDetailContent, його логіка перенесена в ItemDetailScreen


// [НОВИЙ КОМПОНЕНТ] Кнопки "Купити" / "В корзину"
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
            enabled = item.isAvailable
        ) {
            Text("Купити", fontWeight = FontWeight.Bold)
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
            Text(if (isInCart) "Вже в корзині" else "Додати в корзину", fontWeight = FontWeight.Bold)
        }
    }
}

// ВИПРАВЛЕНО: Усунення сірої області
@Composable
fun ImagePager(imageUrls: List<String>) {
    // В якості заглушки для дизайну використовуємо перше зображення
    val mainImageUrl = imageUrls.firstOrNull()
        ?: "https://example.com/placeholder.jpg"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Color.White), // Гарантуємо білий фон
        contentAlignment = Alignment.Center
    ) {
        // TODO: Після цього кроку рекомендується впровадити HorizontalPager
        AsyncImage(
            model = mainImageUrl, // <--- URL-адреса
            contentDescription = "Зображення товару",
            // ВИПРАВЛЕНО: Використовуємо .fillMaxWidth() для того, щоб зображення займало всю ширину
            // Це повинно усунути проблему з білим простором, який проступає
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        // "Like" кнопка
        IconButton(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "В обране")
        }

        // TODO: Додайте Thumbs (1, 2, 3, 4) внизу
    }
}

// ВИПРАВЛЕНО: TitleAndPrice без старої ціни та знижки, без зайвих відступів
@Composable
fun TitleAndPrice(item: Item) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("uk", "UA"))

    // Назва товару
    Text(
        text = item.name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(8.dp))

    // [ОНОВЛЕНО] Рейтинг (динамічний) та прибрано Код товару
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Якщо є відгуки, показуємо рейтинг
        if (item.reviewCount > 0) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
            Text(
                // Форматуємо середній рейтинг до одного знаку після коми
                text = " ${String.format("%.1f", item.averageRating)} (${item.reviewCount} відгук${if (item.reviewCount != 1) "и" else ""})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        } else {
            // Якщо немає відгуків
            Text(
                text = "Немає відгуків",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // [ВИДАЛЕНО] Код товару та статичні відгуки
    }

    Spacer(Modifier.height(16.dp))

    // Наявність
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Наявність", tint = Color.Green, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = if (item.isAvailable) "Є в наявності" else "Немає в наявності",
            color = Color.Green,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(Modifier.height(16.dp))

    // Ціна (ТІЛЬКИ АКТУАЛЬНА ЦІНА)
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = currencyFormat.format(item.price), // 41 999,58 грн
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun VariantSelectors() {
    var selectedColor by remember { mutableStateOf("Чорний") }
    var selectedStorage by remember { mutableStateOf("256GB") }

    // Колір
    Column {
        Text("Колір: $selectedColor", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Заглушка для вибору кольору 1
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
            // Заглушка для вибору кольору 2
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

    // Пам'ять
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
        Text("Опис товару", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}