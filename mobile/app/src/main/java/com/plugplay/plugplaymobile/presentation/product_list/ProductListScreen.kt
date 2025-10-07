package com.plugplay.plugplaymobile.presentation.product_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // 💡 ДОДАНО: Вирішує 'Unresolved reference: Alignment'
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateToProfile: () -> Unit, // 💡 НОВИЙ CALLBACK
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Каталог товарів") }) },
        // 💡 ДОДАВАННЯ ПАНЕЛІ НАВІГАЦІЇ
        bottomBar = {
            BottomAppBar {
                // Кнопка Каталог (Поточний екран)
                IconButton(
                    onClick = { /* Вже на каталозі */ },
                    enabled = false, // Вимикаємо, оскільки ми вже тут
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.List, contentDescription = "Каталог")
                }

                // Кнопка Профіль
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Профіль")
                }
            }
        }
    ) { padding ->
        when (state) {
            is ProductListState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // Імітація завантаження
                    Text(text = "Завантаження...", modifier = Modifier.padding(16.dp))
                }
            }
            is ProductListState.Success -> {
                ProductListView(
                    products = (state as ProductListState.Success).products,
                    modifier = Modifier.padding(padding)
                )
            }
            is ProductListState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Text(text = "Помилка: ${(state as ProductListState.Error).message}", color = MaterialTheme.colorScheme.error)
                }
            }
            ProductListState.Idle -> { /* Нічого не робимо */ }
        }
    }
}

@Composable
fun ProductListView(products: List<Product>, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        items(products) { product ->
            ProductItem(product)
            Divider()
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Імітація зображення
        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(end = 12.dp)
        ) {
            Text("IMG", style = MaterialTheme.typography.bodyLarge)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(product.title, style = MaterialTheme.typography.titleMedium)
            Text(product.priceValue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
