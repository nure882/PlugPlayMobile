package com.plugplay.plugplaymobile.presentation.product_list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plugplay.plugplaymobile.R
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.presentation.cart.CartViewModel
import com.plugplay.plugplaymobile.presentation.cart.ShoppingCartDialog
import java.util.Locale

// --- ДАНІ-ЗАГЛУШКИ ДЛЯ ДИЗАЙНУ ---

// [ЗМІНЕНО] Додайте categoryId (Int) до моделі
data class CategoryItem(val name: String, val icon: Int, val categoryId: Int)

// [ЗМІНЕНО] Використовуйте реальні ID категорій:
val categoryItems = listOf(
    // ID 23 - Smartphones
    CategoryItem("Smartphones", R.drawable.smartphone_logo, 23),
    // ID 35 - Headphones & Earbuds
    CategoryItem("Headphones", R.drawable.headphones_logo, 35),
    // ID 2 - Laptops
    CategoryItem("Laptops", R.drawable.laptop_logo, 2),
    // ID 52 - Cameras & Photography
    CategoryItem("Cameras", R.drawable.camera_logo, 52),
)

// --- ОСНОВНИЙ ЕКРАН ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToItemDetail: (itemId: String) -> Unit,
    onNavigateToCheckout: () -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val cartItemsCount = cartState.cartItems.sumOf { it.quantity }

    // Стан для відображення діалогу кошика
    var isCartOpen by remember { mutableStateOf(false) }

    // [НОВЕ] Стан для відображення модального вікна фільтра
    val isFilterModalVisible by viewModel.isFilterModalVisible.collectAsState()

    // [НОВЕ] Отримуємо поточний ключ фільтра для AnimatedContent
    val currentFilterKey by viewModel.currentCategoryId.collectAsState()

    // [ДОДАНО] Діалог кошика
    ShoppingCartDialog(
        isOpen = isCartOpen,
        onClose = { isCartOpen = false },
        onNavigateToCheckout = {
            isCartOpen = false
            onNavigateToCheckout()
        }
    )

    // [ОНОВЛЕНО] Модальне вікно для фільтрів
    FilterModal(
        isOpen = isFilterModalVisible,
        onClose = viewModel::toggleFilterModal,
        onApply = { minPrice, maxPrice, isSortAscending -> // <--- ОНОВЛЕНО СИГНАТУРУ
            viewModel.applyFilters(minPrice, maxPrice, isSortAscending)
        }
    )

    Scaffold(
        topBar = {
            // [ОНОВЛЕНО TopAppBar]
            TopAppBar(
                title = {
                    // ПОЧАТОК ЗМІНИ: Заміна тексту на два графічні ресурси
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ресурс 1: Іконка "Plug" (вилка, logo.png)
                        Icon(
                            painter = painterResource(id = R.drawable.logo_plug), // <-- Використовуйте R.drawable.logo_plug
                            contentDescription = "Plug Logo Icon",
                            tint = MaterialTheme.colorScheme.primary, // Можна стилізувати кольором теми
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        // Ресурс 2: Текст "Plug & Play" (file (1).png)

                        // ПОЧАТОК ЗМІНИ: Повернення тексту "Plug & Play" та зміна кольору на чорний
                        Text(
                            "Plug & Play",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black // <-- ТУТ ЗМІНА
                        )
                    }
                    // КІНЕЦЬ ЗМІНИ
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

        // [НОВИЙ КОНТЕЙНЕР АНІМАЦІЇ]
        // AnimatedContent анімує перехід, коли змінюється currentFilterKey
        AnimatedContent(
            targetState = currentFilterKey, // Ключ, який запускає анімацію (ID категорії або null)
            transitionSpec = {
                // Плавний перехід: старий екран зникає, новий з'являється одночасно
                (fadeIn(animationSpec = tween(300))
                    .togetherWith(fadeOut(animationSpec = tween(300))))
            },
            label = "CategoryFilterFade"
        ) { targetCategory -> // targetCategory - це значення currentFilterKey

            // Вміст, який анімується
            Box(Modifier.fillMaxSize().padding(padding)) {

                // Обробка станів (Завантаження, Помилка)
                when (state) {
                    is ProductListState.Loading -> {
                        // Показуємо індикатор завантаження
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    is ProductListState.Error -> {
                        Box(Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                            Text(
                                text = "Помилка: ${(state as ProductListState.Error).message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    is ProductListState.Success -> {
                        // [НОВИЙ МАКЕТ] Використовуємо LazyVerticalGrid
                        ProductGrid(
                            products = (state as ProductListState.Success).products,
                            // Передаємо порожній модифікатор, оскільки всі відступи вже оброблені
                            modifier = Modifier,
                            onItemClick = onNavigateToItemDetail,
                            viewModel = viewModel,
                            // [НОВЕ] Передаємо обробник кліку для фільтра
                            onFilterClick = viewModel::toggleFilterModal
                        )
                    }
                    ProductListState.Idle -> { /* Нічого */ }
                }
            }
        }
    }
}

// --- НОВІ КОМПОНЕНТИ ДИЗАЙНУ ---
// (Оновлення ProductGrid та CategoryLazyRow)

@Composable
fun ProductGrid(
    products: List<Product>,
    modifier: Modifier,
    onItemClick: (itemId: String) -> Unit,
    viewModel: ProductListViewModel, // <--- ВИКОРИСТОВУЄМО VIEWMODEL
    // [НОВИЙ АРГУМЕНТ]
    onFilterClick: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 колонки
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 2. ЗАГОЛОВОК КАТЕГОРІЙ (на всю ширину)
        item(span = { GridItemSpan(2) }) {
            SectionHeader(title = "It would interest you")
        }

        // 3. РЯДОК КАТЕГОРІЙ (на всю ширину)
        item(span = { GridItemSpan(2) }) {
            CategoryLazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                viewModel = viewModel // <--- ПЕРЕДАЄМО VIEWMODEL
            )
        }

        // 4. ЗАГОЛОВОК "ДЛЯ ТЕБЕ" (на всю ширину)
        item(span = { GridItemSpan(2) }) {
            SectionHeader(
                title = "For you",
                showFilter = true,
                // [ЗМІНА] Передаємо обробник кліку
                onFilterClick = onFilterClick
            )
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
                    model = product.image, // <--- URL-адреса з моделі
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    // Використовуємо заглушку на випадок помилки Coil
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )

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
    modifier: Modifier = Modifier,
    // [ЗМІНА] Додаємо обробник кліку
    onFilterClick: () -> Unit = {}
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
            // [ЗМІНА] Використовуємо onFilterClick
            TextButton(onClick = onFilterClick) {
                Text("Filters")
            }
        }
    }
}

// [ЗМІНЕНО] CategoryLazyRow тепер приймає ViewModel
@Composable
fun CategoryLazyRow(modifier: Modifier = Modifier, viewModel: ProductListViewModel = hiltViewModel()) {
    val selectedCategory by viewModel.currentCategoryId.collectAsState() // Отримуємо поточний фільтр

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categoryItems) { item ->
            CategoryIconItem(
                item = item,
                isSelected = item.categoryId == selectedCategory, // Порівнюємо ID
                onClick = { viewModel.setCategoryFilter(item.categoryId) } // <--- ДОДАНО onClick
            )
        }
    }
}

// [НОВИЙ/ЗМІНЕНИЙ] CategoryIconItem тепер приймає isSelected та onClick
@Composable
fun CategoryIconItem(item: CategoryItem, isSelected: Boolean, onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick) // <--- ДОДАНО ОБРОБНИК КЛІКУ
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSelected) primaryColor.copy(alpha = 0.1f) // Якщо обрано
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .border(
                    width = 2.dp,
                    color = if (isSelected) primaryColor else Color.Transparent, // Рамка для обраного
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.icon),
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                tint = primaryColor
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold, // Жирний шрифт для обраного
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- НОВИЙ КОМПОНЕНТ: МОДАЛЬНЕ ВІКНО ФІЛЬТРІВ (ОНОВЛЕНО ДЛЯ ЦІНИ ТА СОРТУВАННЯ) ---

@Composable
fun FilterModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    // [ОНОВЛЕНО]: Тепер приймає опціональний Boolean для сортування (true=Asc, false=Desc, null=None)
    onApply: (minPrice: Double?, maxPrice: Double?, isSortAscending: Boolean?) -> Unit,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    if (!isOpen) return

    val currentMinPrice by viewModel.minPrice.collectAsState()
    val currentMaxPrice by viewModel.maxPrice.collectAsState()
    val currentSort by viewModel.isPriceSortAscending.collectAsState() // <--- НОВЕ: Стан сортування

    // Стан для полів введення ціни
    val minPriceText = remember { mutableStateOf(currentMinPrice?.toString() ?: "") }
    val maxPriceText = remember { mutableStateOf(currentMaxPrice?.toString() ?: "") }

    // Стан для перемикача сортування (true=Asc, false=Desc, null=No Sort)
    var isSortAscending by remember { mutableStateOf(currentSort) }

    LaunchedEffect(currentMinPrice, currentMaxPrice, currentSort) {
        minPriceText.value = currentMinPrice?.toString() ?: ""
        maxPriceText.value = currentMaxPrice?.toString() ?: ""
        isSortAscending = currentSort
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.8f)
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
                    Text("Product Filters", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Закрити")
                    }
                }
                Divider()

                // Content (Price Range UI & Sorting)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- 1. Фільтр за ціною ---
                    Text(
                        "Price Range",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = minPriceText.value,
                            onValueChange = { minPriceText.value = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("From (₴)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = maxPriceText.value,
                            onValueChange = { maxPriceText.value = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("To (₴)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // --- 2. Сортування ---
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Sort by Price",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Radio Button: No Sort / Ascending / Descending
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Radio Button: No Sort
                        FilterRadioButton(
                            label = "No Sort",
                            selected = isSortAscending == null,
                            onClick = { isSortAscending = null }
                        )

                        // Radio Button: Ascending
                        FilterRadioButton(
                            label = "Ascending",
                            selected = isSortAscending == true,
                            onClick = { isSortAscending = true }
                        )

                        // Radio Button: Descending
                        FilterRadioButton(
                            label = "Descending",
                            selected = isSortAscending == false,
                            onClick = { isSortAscending = false }
                        )
                    }
                }

                // Footer
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Парсинг значень та виклик onApply
                            val minPrice = minPriceText.value.toDoubleOrNull()
                            val maxPrice = maxPriceText.value.toDoubleOrNull()
                            // Передаємо всі три параметри
                            onApply(minPrice, maxPrice, isSortAscending)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Filters", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// [НОВИЙ ДОПОМІЖНИЙ КОМПОНЕНТ]
@Composable
fun RowScope.FilterRadioButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}