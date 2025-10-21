package com.plugplay.plugplaymobile.presentation.auth

import androidx.compose.foundation.BorderStroke // [НОВИЙ ІМПОРТ]
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText // [НОВИЙ ІМПОРТ]
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.text.SpanStyle // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.text.buildAnnotatedString // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.text.input.VisualTransformation // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.R // 💡 Потрібен для R.drawable...

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthResultState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF4F7F8) // Світло-сірий фон
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    if (state is AuthResultState.Error) {
                        Text(
                            text = (state as AuthResultState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.login(email.value, password.value)
                        },
                        enabled = state !is AuthResultState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (state is AuthResultState.Loading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Sign In", fontSize = 16.sp)
                        }
                    }

                    OrDivider()

                    GoogleSignInButton {
                        // TODO: Google Sign In logic
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // [ВИПРАВЛЕНО] Тепер ця функція визначена нижче
            ClickableRegisterText(onNavigateToRegister)
        }
    }
}




@Composable
private fun ClickableRegisterText(onClick: () -> Unit) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.Gray)) {
            append("Don't have an account? ")
        }
        pushStringAnnotation(tag = "SignUp", annotation = "SignUp")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
            append("Sign Up")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "SignUp", start = offset, end = offset)
                .firstOrNull()?.let {
                    onClick()
                }
        }
    )
}

@Composable
private fun ClickableLoginText(onClick: () -> Unit) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.Gray)) {
            append("Already have an account? ")
        }
        pushStringAnnotation(tag = "SignIn", annotation = "SignIn")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
            append("Sign In")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "SignIn", start = offset, end = offset)
                .firstOrNull()?.let {
                    onClick()
                }
        }
    )
}