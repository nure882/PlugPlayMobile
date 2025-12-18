package com.plugplay.plugplaymobile.presentation.product_list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plugplay.plugplaymobile.R
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.presentation.cart.CartViewModel
import com.plugplay.plugplaymobile.presentation.cart.ShoppingCartDialog
import kotlinx.coroutines.launch

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
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val cartItemsCount = cartState.cartItems.sumOf { it.quantity }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val categoryTree by viewModel.categoryTree.collectAsState()

    var isCartOpen by remember { mutableStateOf(false) }
    val isFilterModalVisible by viewModel.isFilterModalVisible.collectAsState()

    val currentSearchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    var productToRemove by remember { mutableStateOf<Product?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadWishlist()
    }

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

    // [НОВОЕ] Вычисляем имя текущей категории для заголовка
    val currentCategoryName = remember(currentCategoryId, categoryTree) {
        fun findName(nodes: List<CategoryNode>, id: Int): String? {
            for (node in nodes) {
                if (node.id == id) return node.name
                val childName = findName(node.children, id)
                if (childName != null) return childName
            }
            return null
        }
        currentCategoryId?.let { findName(categoryTree, it) }
    }

    ShoppingCartDialog(
        isOpen = isCartOpen,
        onClose = { isCartOpen = false },
        onNavigateToCheckout = {
            isCartOpen = false
            onNavigateToCheckout()
        }
    )

    FilterModal(
        isOpen = isFilterModalVisible,
        onClose = viewModel::toggleFilterModal,
        onApply = { min, max, sort, attrs ->
            viewModel.applyFilters(min, max, sort, attrs)
        }
    )

    if (productToRemove != null) {
        AlertDialog(
            onDismissRequest = { productToRemove = null },
            title = { Text(text = "Confirm action") },
            text = {
                Text(text = "Are you sure you want to remove ${productToRemove?.title} from your wishlist?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToRemove?.let { viewModel.toggleWishlist(it.id) }
                        productToRemove = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productToRemove = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "Categories",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp, start = 12.dp)
                    )
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        "All Products",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                selected = currentCategoryId == null,
                                onClick = {
                                    viewModel.setCategoryFilter(0) // Сброс
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(vertical = 4.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        items(categoryTree) { node ->
                            CategoryTreeItem(
                                node = node,
                                selectedId = currentCategoryId,
                                onCategoryClick = { id ->
                                    viewModel.setCategoryFilter(id)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) {
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
                                    onSearch = { viewModel.search(searchText.trim()) }
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
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Close search")
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
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
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
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    when (state) {
                        is ProductListState.Loading -> {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }

                        is ProductListState.Error -> {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Text(
                                    text = "Error: ${(state as ProductListState.Error).message}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        is ProductListState.Success -> {
                            val products = (state as ProductListState.Success).products
                            ProductGrid(
                                products = products,
                                currentCategoryName = currentCategoryName, // [НОВОЕ] Передаем имя
                                onClearCategory = {
                                    if (currentCategoryId != null) {
                                        viewModel.setCategoryFilter(currentCategoryId!!)
                                    }
                                },
                                modifier = Modifier,
                                onItemClick = onNavigateToItemDetail,
                                viewModel = viewModel,
                                onFilterClick = viewModel::toggleFilterModal,
                                onToggleWishlist = { product ->
                                    if (wishlistIds.contains(product.id)) {
                                        productToRemove = product
                                    } else {
                                        viewModel.toggleWishlist(product.id)
                                    }
                                },
                                wishlistIds = wishlistIds,
                                isLoggedIn = isLoggedIn
                            )
                        }

                        ProductListState.Idle -> { }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTreeItem(
    node: CategoryNode,
    selectedId: Int?,
    onCategoryClick: (Int) -> Unit,
    level: Int = 0
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasChildren = node.children.isNotEmpty()
    val isSelected = node.id == selectedId

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) primaryColor.copy(alpha = 0.1f)
                    else Color.Transparent
                )
                .clickable {
                    if (hasChildren && !isSelected) {
                        isExpanded = !isExpanded
                    } else {
                        onCategoryClick(node.id)
                    }
                }
                .padding(vertical = 12.dp, horizontal = 16.dp + (level * 16).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
            )
            if (hasChildren) {
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (isExpanded) {
            node.children.forEach { child ->
                CategoryTreeItem(
                    node = child,
                    selectedId = selectedId,
                    onCategoryClick = onCategoryClick,
                    level = level + 1
                )
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<Product>,
    currentCategoryName: String?, // [НОВОЕ]
    onClearCategory: () -> Unit, // [НОВОЕ]
    modifier: Modifier,
    onItemClick: (itemId: String) -> Unit,
    viewModel: ProductListViewModel,
    onFilterClick: () -> Unit,
    wishlistIds: Set<String>,
    onToggleWishlist: (Product) -> Unit,
    isLoggedIn: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // [НОВОЕ] Логика заголовка
        item(span = { GridItemSpan(maxLineSpan) }) {
            val title = currentCategoryName ?: "All Products"
            val onBack = if (currentCategoryName != null) onClearCategory else null

            SectionHeader(
                title = title,
                onBackClick = onBack, // Передаем обработчик
                showFilter = true,
                onFilterClick = onFilterClick
            )
        }

        if (products.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No products found",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Try adjusting your filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(products) { product ->
                ProductItem(
                    product = product,
                    onClick = { onItemClick(product.id) },
                    onFavoriteClick = { onToggleWishlist(product) },
                    isFavorite = wishlistIds.contains(product.id),
                    showFavoriteButton = isLoggedIn
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    showFavoriteButton: Boolean = true
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

                if (showFavoriteButton) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(
                                Color.White.copy(alpha = 0.7f),
                                CircleShape
                            )
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "В обране",
                            tint = if (isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
fun SectionHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    showFilter: Boolean = false,
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ЛЕВАЯ ЧАСТЬ (Стрелка + Заголовок)
        // Добавляем .weight(1f), чтобы этот блок занимал всё доступное место,
        // но оставлял место для кнопки справа.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(4.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1, // Ограничиваем одной строкой
                overflow = TextOverflow.Ellipsis // Добавляем троеточие в конце, если не влезает
            )
        }

        // ПРАВАЯ ЧАСТЬ (Кнопка фильтров)
        // Она не имеет веса, поэтому Android отрисует её полностью
        if (showFilter) {
            Spacer(Modifier.width(8.dp)) // Отступ от заголовка
            TextButton(onClick = onFilterClick) {
                Text("Filters")
            }
        }
    }
}

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

    var minPriceText by remember { mutableStateOf(currentMinPrice?.toString() ?: "") }
    var maxPriceText by remember { mutableStateOf(currentMaxPrice?.toString() ?: "") }
    var selectedSort by remember { mutableStateOf(currentSort) }
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
                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Text("Sort By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        SortOptionRadio("Price (Low to High)", "price-asc", selectedSort) { selectedSort = it }
                        SortOptionRadio("Price (High to Low)", "price-desc", selectedSort) { selectedSort = it }
                        SortOptionRadio("Newest", "newest", selectedSort) { selectedSort = it }
                    }

                    HorizontalDivider()

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

                    if (availableAttributes.isNotEmpty()) {
                        HorizontalDivider()
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
    selectedValues: Set<String>,
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
                group.options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onValueToggle(option.value) }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedValues.contains(option.value),
                            onCheckedChange = { onValueToggle(option.value) }
                        )
                        Text(
                            text = option.display,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}