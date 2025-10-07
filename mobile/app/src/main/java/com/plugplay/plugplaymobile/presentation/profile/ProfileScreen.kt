package com.plugplay.plugplaymobile.presentation.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(
    onNavigateToCatalog: () -> Unit, // Callback для перехода в Каталог
    onNavigateToProfile: () -> Unit // Callback для перехода в Профиль (текущий экран)
) {
    Scaffold(
        bottomBar = {
            BottomAppBar {
                // Кнопка Каталог
                IconButton(
                    onClick = onNavigateToCatalog,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.List, contentDescription = "Каталог")
                }

                // Кнопка Профиль (Текущий экран)
                IconButton(
                    onClick = onNavigateToProfile,
                    enabled = false, // Отключаем, так как мы уже здесь
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Профіль")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Екран Профілю (У розробці)")
        }
    }
}
