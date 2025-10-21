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
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    navController: NavController,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            // [НОВИЙ TopAppBar] З іконками, як на головному екрані
            TopAppBar(
                title = { Text("Plug & Play") }, // Або state.item?.name
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
                    IconButton(onClick = { /* TODO: Корзина */ }) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Корзина")
                    }
                }
            )
        }
    ) { innerPadding ->
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
                    // [НОВИЙ МАКЕТ] Використовуємо LazyColumn
                    ItemDetailContent(item = state.item!!)
                }
            }
        }
    }
}

@Composable
fun ItemDetailContent(item: Item) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)), // Світло-сірий фон, як на макеті
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- Секція 1: Зображення ---
        item {
            ImagePager(item.imageUrl)
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
            ActionButtons(item)
        }

        // --- Секція 4: Доставка та Гарантія ---
        item {
            InfoSection() // Заглушка
        }

        // --- Секція 5: Опис ---
        item {
            DescriptionSection(item)
        }
    }
}

// [НОВИЙ КОМПОНЕНТ] Заглушка для пейджера зображень
@Composable
fun ImagePager(imageUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // TODO: Замініть на HorizontalPager з Accompanist або Foundation 1.6+
        AsyncImage(
            model = imageUrl,
            contentDescription = "Зображення товару",
            modifier = Modifier.fillMaxSize(),
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

// [НОВИЙ КОМПОНЕНТ] Назва, рейтинг, ціна
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

    // Рейтинг та Код товару
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
        Text(" 4.7 (1044 відгуки)", style = MaterialTheme.typography.bodySmall, color = Color.Gray) // Заглушка
        Spacer(Modifier.width(8.dp))
        Text("|", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.width(8.dp))
        Text("Код: ${item.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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

    // Ціна
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = currencyFormat.format(item.price), // ₴33,999
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))

        // "Стара" ціна
        Text(
            text = currencyFormat.format(item.price * 1.1), // Заглушка
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            textDecoration = TextDecoration.LineThrough
        )
        Spacer(Modifier.width(8.dp))

        // Знижка
        Badge(containerColor = MaterialTheme.colorScheme.error) {
            Text("-10%", fontWeight = FontWeight.Bold) // Заглушка
        }
    }
}

// [НОВИЙ КОМПОНЕНТ] Заглушка для вибору варіантів
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

// [НОВИЙ КОМПОНЕНТ] Кнопки "Купити" / "В корзину"
@Composable
fun ActionButtons(item: Item) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
    ) {
        Button(
            onClick = { /* TODO: Buy Logic */ },
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
            onClick = { /* TODO: Add to Cart Logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = item.isAvailable
        ) {
            Text("Додати в корзину", fontWeight = FontWeight.Bold)
        }
    }
}

// [НОВИЙ КОМПОНЕНТ] Заглушка для інфо-секції
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

// [НОВИЙ КОМПОНЕНТ] Секція опису
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