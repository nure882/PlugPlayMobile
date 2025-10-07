package com.plugplay.plugplaymobile.presentation.product_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    // 💡 Hilt автоматически предоставит ViewModel
    viewModel: ProductListViewModel = hiltViewModel()
) {
    // Наблюдение за StateFlow. При изменении состояния UI перерисовывается.
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Каталог товарів") }) }) { padding ->

        // В зависимости от текущего состояния, показываем разный UI
        when (state) {
            ProductListState.Loading -> LoadingView(Modifier.padding(padding))

            is ProductListState.Error -> ErrorView(
                (state as ProductListState.Error).message,
                onRetry = { viewModel.loadProducts() },
                modifier = Modifier.padding(padding)
            )

            is ProductListState.Success -> ProductListView(
                products = (state as ProductListState.Success).products,
                modifier = Modifier.padding(padding)
            )

            ProductListState.Empty -> EmptyView(Modifier.padding(padding))
        }
    }
}

// --- Вспомогательные Composable-функции (для краткости) ---

@Composable
fun LoadingView(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier) {
    Column(
        modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Помилка: $message", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Спробувати ще")
        }
    }
}

@Composable
fun ProductListView(products: List<Product>, modifier: Modifier) {
    // Применяем внешний модификатор (который содержит отступы от Scaffold) к LazyColumn
    LazyColumn(
        modifier = modifier // 💡 Применяем modifier (который содержит отступы Scaffold)
            .fillMaxSize()
            .padding(horizontal = 8.dp) // Дополнительные горизонтальные отступы
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
        //  // Здесь будет загрузка изображения
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(product.title, style = MaterialTheme.typography.titleMedium)
            Text(product.priceValue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun EmptyView(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Товарів немає. Спробуйте пізніше.")
    }
}