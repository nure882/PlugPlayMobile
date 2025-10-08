package com.plugplay.plugplaymobile.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // 👈 Нужный импорт!
import com.plugplay.plugplaymobile.presentation.auth.AuthViewModel

/**
 * Заглушка для иконки (имитация вилки)
 */
@Composable
private fun PlugIcon(color: Color) {
    // Используем простой текст в качестве заглушки для SVG-иконки
    Text(
        text = "🔌",
        fontSize = 32.sp,
        color = color,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/**
 * Компонент логотипа Plug&Play
 */
@Composable
private fun PlugPlayLogo() {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        PlugIcon(color = primaryColor)
        Spacer(Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                    append("Plug&")
                }
                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                    append("Play")
                }
            },
            fontSize = 32.sp,
        )
    }
}


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val loginEnabled = remember { derivedStateOf { email.value.isNotBlank() && password.value.isNotBlank() } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 💡 ЛОГОТИП СВЕРХУ
            PlugPlayLogo()

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Password Field (без логики видимости из-за конфликтов зависимостей)
                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Login Button
                    Button(
                        onClick = {
                            // Имитация успешного входа
                            if (loginEnabled.value) {
                                viewModel.login(email.value, password.value)
                                onLoginSuccess()
                            }
                        },
                        enabled = loginEnabled.value,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Sign In", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Разделитель или текст "или"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f).height(1.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Text(
                            text = " OR ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Divider(
                            modifier = Modifier.weight(1f).height(1.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Login Placeholder
                    OutlinedButton(
                        onClick = { /* TODO: Google Sign In logic */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = SolidColor(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        )
                    ) {
                        // Заглушка иконки Google
                        Text("G", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 8.dp))
                        Text("Continue with Google", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Link to Register
                    Text(
                        text = "Don't have an account? Sign up",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToRegister)
                    )
                }
            }
        }
    }
}
