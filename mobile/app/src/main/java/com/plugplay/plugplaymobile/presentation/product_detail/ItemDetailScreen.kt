package com.plugplay.plugplaymobile.presentation.product_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.plugplay.plugplaymobile.R
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Review
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
    onNavigateToProfile: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val cartItemsCount = cartState.cartItems.sumOf { it.quantity }
    val item = state.item
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var isCartOpen by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

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

    if (showRemoveDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text(text = "Confirm action") },
            text = { Text(text = "Are you sure you want to remove ${item.name} from your wishlist?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleFavorite()
                        showRemoveDialog = false
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plug & Play", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = {
                            if (state.isFavorite) showRemoveDialog = true else viewModel.toggleFavorite()
                        }) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (state.isFavorite) Color.Red else Color.Black
                            )
                        }
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
                        ) { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart") }
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            val isWideScreen = maxWidth > 800.dp

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Text(
                        text = state.error.toString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                item != null -> {
                    if (isWideScreen) {
                        // ПК/Планшетный вид: Две колонки
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            // Левая колонка: Изображение
                            Column(modifier = Modifier.weight(1.2f)) {
                                ImagePager(item.imageUrls)
                            }

                            // Правая колонка: Инфо и действия (скроллится, если контента много)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                TitleAndPrice(item)
                                Spacer(Modifier.height(24.dp))
                                ActionButtons(
                                    item = item,
                                    isInCart = isInCart,
                                    onAddToCart = { cartViewModel.addToCart(item.id, 1) },
                                    onBuyClick = {
                                        cartViewModel.addToCart(item.id, 1)
                                        isCartOpen = true
                                    }
                                )
                                Spacer(Modifier.height(24.dp))
                                InfoSection()
                                Spacer(Modifier.height(24.dp))
                                DescriptionSection(item)
                                if (state.attributes.isNotEmpty()) {
                                    ProductAttributesSection(attributes = state.attributes)
                                }
                                ProductReviewsSection(reviews = item.reviews)
                            }
                        }
                    } else {
                        // Мобильный вид: Обычный LazyColumn
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item { ImagePager(item.imageUrls) }
                            item {
                                Column(Modifier.padding(16.dp)) {
                                    TitleAndPrice(item)
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                            item {
                                ActionButtons(
                                    item = item,
                                    isInCart = isInCart,
                                    onAddToCart = { cartViewModel.addToCart(item.id, 1) },
                                    onBuyClick = {
                                        cartViewModel.addToCart(item.id, 1)
                                        isCartOpen = true
                                    }
                                )
                            }
                            item { InfoSection() }
                            item { DescriptionSection(item) }
                            if (state.attributes.isNotEmpty()) {
                                item { ProductAttributesSection(attributes = state.attributes) }
                            }
                            item { ProductReviewsSection(reviews = item.reviews) }
                        }
                    }
                }
            }
        }
    }
}

// [Components: ProductAttributesSection, ProductReviewsSection, ReviewItem, ActionButtons, ImagePager, TitleAndPrice, InfoSection, InfoRow, DescriptionSection - NO CHANGES NEEDED]
// Copy them from previous responses if needed.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductAttributesSection(attributes: List<AttributeGroup>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4F7F8), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                attributes.forEach { group ->
                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            group.options.forEach { option ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                    shadowElevation = 0.dp
                                ) {
                                    Text(
                                        text = option.display,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProductReviewsSection(reviews: List<Review>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Reviews (${reviews.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (reviews.isEmpty()) {
            Text("No reviews yet.", color = Color.Gray)
        } else {
            reviews.forEach { review ->
                ReviewItem(review)
                if (review != reviews.last()) {
                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.userName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = review.userName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = review.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Row(modifier = Modifier.padding(vertical = 6.dp)) {
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (index < review.rating) Color(0xFFFFC107) else Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp)
            )
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
            enabled = item.isAvailable,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = "Buy",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Buy", fontWeight = FontWeight.Bold, color = Color.White)
            }
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
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isInCart) Color(0xFFE0E0E0) else Color.Gray.copy(alpha = 0.5f)
            )
        ) {
            Text(if (isInCart) "Already in cart" else "Add to cart", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ImagePager(imageUrls: List<String>) {
    val mainImageUrl = imageUrls.firstOrNull() ?: "https://example.com/placeholder.jpg"
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
                contentDescription = "Product Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_foreground)
            )
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

    // 1. [НОВОЕ] Категория (над названием)
    Text(
        text = item.category.uppercase(), // Или item.brand, если хотите бренд
        style = MaterialTheme.typography.labelMedium,
        color = Color.Gray,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(Modifier.height(4.dp))

    // Название товара
    Text(
        text = item.name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(8.dp))

    // 2. [НОВОЕ] Рейтинг 5 звезд
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Рисуем 5 звезд
        repeat(5) { index ->
            // Если индекс меньше рейтинга (округленного), рисуем полную, иначе пустую
            val starIcon = if (index < item.averageRating.toInt()) Icons.Filled.Star else Icons.Outlined.StarBorder

            Icon(
                imageVector = starIcon,
                contentDescription = null,
                tint = Color(0xFFFFC107), // Золотой цвет
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Количество отзывов текстом
        Text(
            text = "(${item.reviewCount} reviews)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }

    Spacer(Modifier.height(16.dp))

    // Блок наличия (из предыдущего шага)
    Row(verticalAlignment = Alignment.CenterVertically) {
        val isAvailable = item.stockQuantity > 0
        val statusColor = if (isAvailable) Color(0xFF4CAF50) else Color.Red
        val statusIcon = if (isAvailable) Icons.Default.CheckCircle else Icons.Outlined.Cancel
        val statusText = if (isAvailable) "In stock: ${item.stockQuantity} pcs" else "Out of stock"

        Icon(
            imageVector = statusIcon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = statusText,
            color = statusColor,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(Modifier.height(16.dp))

    // Цена
    Text(
        text = formattedNumber,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
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
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        InfoRow(Icons.Outlined.Shield, "1 year warranty", "Official manufacturer warranty")
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        InfoRow(Icons.Outlined.Replay, "Return within 14 days", "Ability to return the product")
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
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
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxSize())
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
        Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
    }
}