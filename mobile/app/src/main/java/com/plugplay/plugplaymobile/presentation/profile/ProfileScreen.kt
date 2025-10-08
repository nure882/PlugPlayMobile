package com.plugplay.plugplaymobile.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToCatalog: () -> Unit,
    onNavigateToLogin: () -> Unit, // 💡 Callback для переходу на Login
    viewModel: AuthViewModel = hiltViewModel() // Використовуємо AuthViewModel для стану
) {
    // 💡 Спостерігаємо за станом логіну
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Профіль") }) },
        bottomBar = {
            BottomAppBar {
                // Кнопка Каталог
                IconButton(
                    onClick = onNavigateToCatalog,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.List, contentDescription = "Каталог")
                }

                // Кнопка Профіль (Поточний екран)
                IconButton(
                    onClick = { /* Вже на профілі */ },
                    enabled = false,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Профіль")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoggedIn) {
                // 💡 АВТОРИЗОВАНИЙ СТАН
                Text("Вітаємо в PlugPlay!", style = MaterialTheme.typography.headlineSmall)
                Text("ID Користувача: 42 (Mock)", modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.logout()
                        // Навігація не потрібна, оскільки стан isLoggedIn змінить UI
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Вийти")
                }
            } else {
                // 💡 НЕАВТОРИЗОВАНИЙ СТАН
                Text(
                    "Ви не авторизовані.",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Увійдіть, щоб переглянути свої замовлення та зберегти вподобання.",
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Увійти / Зареєструватися")
                }
            }
        }
    }
}
