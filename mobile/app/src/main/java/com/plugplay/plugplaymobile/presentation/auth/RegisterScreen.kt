package com.plugplay.plugplaymobile.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.R

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    // [ВИПРАВЛЕНО] Перейменував змінну, щоб уникнути плутанини
    val phoneInput = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthResultState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    val passwordsMatch = remember {
        derivedStateOf { password.value == confirmPassword.value }
    }

    // [ВИПРАВЛЕНО] Тепер логіка активації кнопки перевіряє 'phoneInput'
    val registerEnabled = remember {
        derivedStateOf {
            firstName.value.isNotBlank() &&
                    lastName.value.isNotBlank() &&
                    phoneInput.value.isNotBlank() && // <-- ОСЬ ТУТ БУЛО ВИПРАВЛЕННЯ
                    email.value.isNotBlank() &&
                    password.value.length >= 8 &&
                    passwordsMatch.value &&
                    state !is AuthResultState.Loading // Також перевіряємо, що не йде завантаження
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF4F7F8)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Create your account",
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

                    // First Name & Last Name
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName.value,
                            onValueChange = { firstName.value = it },
                            label = { Text("First Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = lastName.value,
                            onValueChange = { lastName.value = it },
                            label = { Text("Last Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Phone
                    OutlinedTextField(
                        value = phoneInput.value, // <-- [ВИПРАВЛЕНО]
                        onValueChange = { phoneInput.value = it }, // <-- [ВИПРАВЛЕНО]
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Email
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Password
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
                                Icon(imageVector = image, contentDescription = null)
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword.value,
                        onValueChange = { confirmPassword.value = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = !passwordsMatch.value && confirmPassword.value.isNotEmpty(),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible.value = !confirmPasswordVisible.value }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        }
                    )
                    if (!passwordsMatch.value && confirmPassword.value.isNotEmpty()) {
                        Text(
                            text = "Паролі не співпадають",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    if (state is AuthResultState.Error) {
                        Text(
                            text = (state as AuthResultState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Sign Up Button
                    Button(
                        onClick = {
                            // [ВИПРАВЛЕНО] Передаємо phoneInput.value
                            viewModel.register(
                                firstName.value,
                                lastName.value,
                                phoneInput.value,
                                email.value,
                                password.value
                            )
                        },
                        enabled = registerEnabled.value, // <-- [ВИПРАВЛЕНО] Логіка тепер в самій змінній
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
                            Text("Sign Up", fontSize = 16.sp)
                        }
                    }

                    OrDivider()

                    GoogleSignInButton {
                        // TODO: Google Sign In logic
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            ClickableLoginText(onNavigateToLogin)

            Spacer(Modifier.height(32.dp))
        }
    }
}

// --- ДОПОМІЖНІ КОМПОНЕНТИ (для обох екранів) ---
// (Ці функції залишаються без змін з минулого разу)

@Composable
internal fun OrDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Text(
            text = " or ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
internal fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        // TODO: Додайте реальну іконку Google
        Text(
            "G",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text("Continue with Google", fontSize = 16.sp)
    }
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