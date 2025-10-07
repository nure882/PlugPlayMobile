package com.plugplay.plugplaymobile.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Реакция на успешную регистрацию
    LaunchedEffect(state) {
        if (state is AuthResultState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
        if (state is AuthResultState.Error) {
            // Сброс состояния после показа ошибки
            // viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Реєстрація") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Кнопка "назад"
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Створіть свій обліковий запис", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(32.dp))

            // Поле Ім'я
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ваше ім'я") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

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

            // Кнопка Регистрации
            Button(
                onClick = { viewModel.register(name, email, password) },
                enabled = state != AuthResultState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state == AuthResultState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Зареєструватися")
                }
            }

            // Сообщения об ошибке
            if (state is AuthResultState.Error) {
                Text(
                    text = (state as AuthResultState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
