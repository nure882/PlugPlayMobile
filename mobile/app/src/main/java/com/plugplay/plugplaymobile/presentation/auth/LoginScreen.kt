package com.plugplay.plugplaymobile.presentation.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock // Иконка-заглушка Google
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit, // Callback для перехода на главный экран
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // 💡 Наблюдение за состоянием ViewModel
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Эффект, который реагирует на изменение состояния
    LaunchedEffect(state) {
        if (state is AuthResultState.Success) {
            onLoginSuccess() // Вызываем навигацию
            viewModel.resetState()
        }
        if (state is AuthResultState.Error) {
            // Сброс состояния после показа ошибки, чтобы можно было попробовать снова
            // viewModel.resetState()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Вхід до PlugPlay", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))

            // Поле Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Поле Пароль
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // Кнопка Входа
            Button(
                onClick = { viewModel.login(email, password) },
                enabled = state != AuthResultState.Loading, // Блокируем во время загрузки
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state == AuthResultState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Увійти")
                }
            }

            // Разделитель и кнопка Google
            Spacer(Modifier.height(32.dp))
            Divider(Modifier.fillMaxWidth(0.8f))
            Spacer(Modifier.height(32.dp))

            // 💡 Кнопка "Логин через Google" (Заглушка)
            OutlinedButton(
                onClick = { /* TODO: Логика Google Sign-In */ },
                enabled = state != AuthResultState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = Color.Transparent
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(Color.Gray.copy(alpha = 0.5f))
                )
            ) {
                // Используем иконку Lock как заглушку, так как Google Icon ресурс отсутствует
                Icon(Icons.Default.Lock, contentDescription = "Google Icon", tint = Color.Red)
                Spacer(Modifier.width(8.dp))
                Text("Увійти через Google")
            }

            // Сообщения об ошибке
            if (state is AuthResultState.Error) {
                Text(
                    text = (state as AuthResultState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Ссылка на регистрацию
            TextButton(onClick = onNavigateToRegister) {
                Text("Немає облікового запису? Зареєструватися")
            }
        }
    }
}
