package com.plugplay.plugplaymobile.presentation.product_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource // НОВИЙ ІМПОРТ
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage // НОВИЙ ІМПОРТ
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.R // НОВИЙ ІМПОРТ: для доступу до ресурсів (R.drawable.placeholder_image)
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
            TopAppBar(
                title = { Text(text = state.item?.name ?: "Деталі товару") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                    state.item?.let { item ->
                        ItemDetailContent(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemDetailContent(item: Item) {
    val scrollState = rememberScrollState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("uk", "UA"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Зображення з Coil
        AsyncImage(
            model = item.imageUrl,
            contentDescription = "Зображення товару ${item.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
            // [ВИПРАВЛЕНО] Використовуємо коректне посилання на R.drawable
            error = painterResource(id = R.drawable.ic_launcher_foreground) // Замініть на ваш реальний ID заглушки (наприклад, placeholder_image)
        )

        Spacer(Modifier.height(16.dp))

        // Назва та бренд
        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Бренд: ${item.brand} | Категорія: ${item.category}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        // Ціна
        Text(
            text = currencyFormat.format(item.price),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        // Кнопка купівлі
        Button(
            onClick = { /* TODO: Логіка додавання до кошика */ },
            enabled = item.isAvailable,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(if (item.isAvailable) "Додати до кошика" else "Немає в наявності")
        }

        Spacer(Modifier.height(24.dp))

        // Опис
        Text(
            text = "Детальний опис",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(64.dp))
    }
}
