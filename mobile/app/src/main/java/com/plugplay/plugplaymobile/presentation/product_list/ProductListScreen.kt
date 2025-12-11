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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plugplay.plugplaymobile.R
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
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

    var isCartOpen by remember { mutableStateOf(false) }
    val isFilterModalVisible by viewModel.isFilterModalVisible.collectAsState()

    // Состояния для поиска
    val currentSearchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(currentSearchQuery) {
        searchText = currentSearchQuery
        if (currentSearchQuery.isNotEmpty()) {
            isSearchActive = true
        }
    }

    val currentCategoryId by viewModel.currentCategoryId.collectAsState()
    val animationKey = remember(currentCategoryId, currentSearchQuery) {
        currentCategoryId?.toString() ?: currentSearchQuery
    }

    ShoppingCartDialog(
        isOpen = isCartOpen,
        onClose = { isCartOpen = false },
        onNavigateToCheckout = {
            isCartOpen = false
            onNavigateToCheckout()
        }
    )

    // [ИСПРАВЛЕНО] Теперь передаем 4 параметра: min, max, sort, attributes
    FilterModal(
        isOpen = isFilterModalVisible,
        onClose = viewModel::toggleFilterModal,
        onApply = { min, max, sort, attrs ->
            viewModel.applyFilters(min, max, sort, attrs)
        }
    )

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("Search products...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.search(searchText.trim())
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchText = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Close search")
                        }
                    },
                    actions = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear text")
                            }
                        }
                        IconButton(onClick = { viewModel.search(searchText.trim()) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.logo_plug),
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Plug & Play", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Outlined.Person, contentDescription = "Profile")
                        }
                        IconButton(onClick = { isCartOpen = true }) {
                            BadgedBox(
                                badge = {
                                    if (cartItemsCount > 0) {
                                        Badge(
                                            modifier = Modifier.offset(x = (-6).dp, y = 4.dp),
                                            containerColor = MaterialTheme.colorScheme.error
                                        ) { Text(cartItemsCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart")
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = animationKey,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300))
                    .togetherWith(fadeOut(animationSpec = tween(300))))
            },
            label = "ContentFade"
        ) { _ ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (state) {
                    is ProductListState.Loading -> {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    is ProductListState.Error -> {
                        Box(Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                            Text(
                                text = "Error: ${(state as ProductListState.Error).message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    is ProductListState.Success -> {
                        val products = (state as ProductListState.Success).products
                        if (products.isEmpty() && currentSearchQuery.isNotEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No products found", style = MaterialTheme.typography.titleLarge)
                                Text("Try adjusting your search", color = Color.Gray)
                            }
                        } else {
                            ProductGrid(
                                products = products,
                                modifier = Modifier,
                                onItemClick = onNavigateToItemDetail,
                                viewModel = viewModel,
                                onFilterClick = viewModel::toggleFilterModal
                            )
                        }
                    }
                    ProductListState.Idle -> { /* Пустое состояние */ }
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
    onApply: (minPrice: Double?, maxPrice: Double?, sortOption: String, attributes: Map<Int, Set<String>>) -> Unit,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    if (!isOpen) return

    val currentMinPrice by viewModel.currentMinPrice.collectAsState()
    val currentMaxPrice by viewModel.currentMaxPrice.collectAsState()
    val currentSort by viewModel.currentSortOption.collectAsState()
    val availableAttributes by viewModel.availableAttributeGroups.collectAsState()
    val initialSelectedAttributes by viewModel.selectedAttributes.collectAsState()

    // Локальное состояние формы
    var minPriceText by remember { mutableStateOf(currentMinPrice?.toString() ?: "") }
    var maxPriceText by remember { mutableStateOf(currentMaxPrice?.toString() ?: "") }
    var selectedSort by remember { mutableStateOf(currentSort) }

    // Состояние выбранных атрибутов (копия для редактирования)
    // Map<GroupId, Set<Value>>
    var selectedAttrs by remember { mutableStateOf(initialSelectedAttributes) }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    Text("Filters & Sort", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Divider()

                // Content Scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Sorting
                    Column {
                        Text("Sort By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        SortOptionRadio("Price (Low to High)", "price-asc", selectedSort) { selectedSort = it }
                        SortOptionRadio("Price (High to Low)", "price-desc", selectedSort) { selectedSort = it }
                        SortOptionRadio("Newest", "newest", selectedSort) { selectedSort = it }
                    }

                    Divider()

                    // 2. Price Range
                    Column {
                        Text("Price Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = minPriceText,
                                onValueChange = { minPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Min (₴)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = maxPriceText,
                                onValueChange = { maxPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Max (₴)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    // 3. Dynamic Attributes
                    if (availableAttributes.isNotEmpty()) {
                        Divider()
                        Text("Characteristics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        availableAttributes.forEach { group ->
                            AttributeFilterGroup(
                                group = group,
                                selectedValues = selectedAttrs[group.id] ?: emptySet(),
                                onValueToggle = { value ->
                                    val currentSet = selectedAttrs[group.id] ?: emptySet()
                                    val newSet = if (currentSet.contains(value)) {
                                        currentSet - value
                                    } else {
                                        currentSet + value
                                    }

                                    // Обновляем Map
                                    selectedAttrs = if (newSet.isEmpty()) {
                                        selectedAttrs - group.id
                                    } else {
                                        selectedAttrs + (group.id to newSet)
                                    }
                                }
                            )
                        }
                    }
                }

                // Footer Buttons
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val min = minPriceText.toDoubleOrNull()
                            val max = maxPriceText.toDoubleOrNull()
                            onApply(min, max, selectedSort, selectedAttrs)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Show Results", fontWeight = FontWeight.Bold)
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

@Composable
fun SortOptionRadio(label: String, value: String, selectedValue: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(value) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = (value == selectedValue),
            onClick = { onSelect(value) },
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AttributeFilterGroup(
    group: AttributeGroup,
    selectedValues: Set<String>, // Здесь хранятся RAW значения ("16", "true")
    onValueToggle: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(group.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                // [ИЗМЕНЕНО] Перебираем options
                group.options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onValueToggle(option.value) } // Переключаем RAW значение
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedValues.contains(option.value), // Проверяем по RAW
                            onCheckedChange = { onValueToggle(option.value) }
                        )
                        Text(
                            text = option.display, // [ВАЖНО] Показываем красивое значение с юнитами
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        Divider(color = Color(0xFFF0F0F0))
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