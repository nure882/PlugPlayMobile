package com.plugplay.plugplaymobile.presentation.wishlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.presentation.product_list.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToItemDetail: (String) -> Unit,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Перезагрузка списка желаемого при входе на экран
    LaunchedEffect(Unit) {
        viewModel.loadWishlist()
    }

    // Состояние для хранения товара, который удаляем.
    // Если null — диалог скрыт. Если объект есть — диалог показывается.
    var productToRemove by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wishlist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color(0xFFF4F7F8)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.items.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Your wishlist is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.items) { product ->
                        ProductItem(
                            product = product,
                            isFavorite = true,
                            onClick = { onNavigateToItemDetail(product.id) },
                            // Вместо удаления сохраняем товар в переменную для диалога
                            onFavoriteClick = { productToRemove = product },
                            showFavoriteButton = true
                        )
                    }
                }
            }
        }

        // Диалог подтверждения
        if (productToRemove != null) {
            AlertDialog(
                onDismissRequest = { productToRemove = null },
                title = { Text(text = "Confirm action") },
                text = {
                    // Здесь подставляем имя товара. Убедитесь, что у Product есть поле name (или title)
                    Text(text = "Are you sure you want to remove ${productToRemove?.title} from your wishlist?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            productToRemove?.let {
                                viewModel.removeFromWishlist(it.id)
                            }
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
    }
}