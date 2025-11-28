package com.plugplay.plugplaymobile.presentation.product_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person // [НОВИЙ ІМПОРТ]
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plugplay.plugplaymobile.R // Потрібен для R.drawable...
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.presentation.cart.CartViewModel
import com.plugplay.plugplaymobile.presentation.cart.ShoppingCartDialog

// --- ДАНІ-ЗАГЛУШКИ ДЛЯ ДИЗАЙНУ ---

// Заглушка для іконок категорій
data class CategoryItem(val name: String, val icon: Int) // Використовуйте R.drawable.ic_...
val categoryItems = listOf(
    CategoryItem("Смартфони", R.drawable.ic_launcher_foreground), // TODO: Замініть на реальні іконки
    CategoryItem("Навушники", R.drawable.ic_launcher_foreground),
    CategoryItem("Ноутбуки", R.drawable.ic_launcher_foreground),
    CategoryItem("Годинники", R.drawable.ic_launcher_foreground),
    CategoryItem("Камери", R.drawable.ic_launcher_foreground),
)

// --- ОСНОВНИЙ ЕКРАН ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToItemDetail: (itemId: String) -> Unit,
    onNavigateToCheckout: () -> Unit, // [НОВИЙ АРГУМЕНТ]
    viewModel: ProductListViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel() // [НОВИЙ VIEWMODEL]
) {
    val state by viewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState() // [CART STATE]
    val cartItemsCount = cartState.cartItems.sumOf { it.quantity }

    // Стан для відображення діалогу кошика
    var isCartOpen by remember { mutableStateOf(false) }

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
                title = {
                    Text(
                        "Plug & Play",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Пошук */ }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Пошук")
                    }

                    // [ДОДАНО] Іконка профілю
                    IconButton(onClick = onNavigateToProfile) {
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
        },
        // [ВИДАЛЕНО] bottomBar
    ) { padding ->

        // Обробка станів (Завантаження, Помилка)
        when (state) {
            is ProductListState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding).wrapContentSize(Alignment.Center)) {
                    CircularProgressIndicator()
                }
            }
            is ProductListState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding).wrapContentSize(Alignment.Center)) {
                    Text(text = "Помилка: ${(state as ProductListState.Error).message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is ProductListState.Success -> {
                // [НОВИЙ МАКЕТ] Використовуємо LazyVerticalGrid
                ProductGrid(
                    products = (state as ProductListState.Success).products,
                    modifier = Modifier.padding(padding),
                    onItemClick = onNavigateToItemDetail
                )
            }
            ProductListState.Idle -> { /* Нічого */ }
        }
    }
}

// --- НОВІ КОМПОНЕНТИ ДИЗАЙНУ ---
// (Весь код нижче залишається без змін)

@Composable
fun ProductGrid(
    products: List<Product>,
    modifier: Modifier,
    onItemClick: (itemId: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 колонки
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. БАНЕР (на всю ширину)
        item(span = { GridItemSpan(2) }) {
            BannerCard(modifier = Modifier.padding(vertical = 8.dp))
        }

        // 2. ЗАГОЛОВОК КАТЕГОРІЙ (на всю ширину)
        item(span = { GridItemSpan(2) }) {
            SectionHeader(title = "Тебе зацікавить")
        }

        // 3. РЯДОК КАТЕГОРІЙ (на всю ширину)
        item(span = { GridItemSpan(2) }) {
            CategoryLazyRow(modifier = Modifier.padding(vertical = 8.dp))
        }

        // 4. ЗАГОЛОВОК "ДЛЯ ТЕБЕ" (на всю ширину)
        item(span = { GridItemSpan(2) }) {
            SectionHeader(title = "Для тебе", showFilter = true)
        }

        // 5. СПИСОК ТОВАРІВ
        items(products) { product ->
            ProductItem(
                product = product,
                onClick = { onItemClick(product.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    containerColor = Color(0xFF00A046)
                ) {
                    Text("NEW", color = Color.White, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = { /* TODO: Add to favorites */ },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = "В обране",
                        tint = Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = product.priceValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Рейтинг",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "4.8",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " (156)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun BannerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF512DA8))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Світло в кожну домівку",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "генератори, павербанки,\nзарядні станції",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Почати продавати", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    showFilter: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (showFilter) {
            TextButton(onClick = { /* TODO: Фільтри */ }) {
                Text("Фільтри")
            }
        }
    }
}

@Composable
fun CategoryLazyRow(modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categoryItems) { item ->
            CategoryIconItem(item)
        }
    }
}

@Composable
fun CategoryIconItem(item: CategoryItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.icon),
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}