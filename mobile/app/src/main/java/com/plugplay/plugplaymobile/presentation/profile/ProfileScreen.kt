package com.plugplay.plugplaymobile.presentation.profile
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility // [НОВИЙ ІМПОРТ]
import androidx.compose.material.icons.filled.VisibilityOff // [НОВИЙ ІМПОРТ]
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.text.input.VisualTransformation // [НОВИЙ ІМПОРТ]
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.presentation.auth.AuthViewModel
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.usecase.GetProfileUseCase
import com.plugplay.plugplaymobile.domain.usecase.UpdateProfileUseCase
import androidx.lifecycle.ViewModel
import com.plugplay.plugplaymobile.R // [НОВИЙ ІМПОРТ]
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

// [STATE] (Залишається без змін)
data class ProfileState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

// [VIEWMODEL] (Залишається без змін)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init { }

    fun onAuthStatusChanged(isLoggedIn: Boolean) {
        if (isLoggedIn && _state.value.profile == null && !_state.value.isLoading) {
            loadProfile()
        } else if (!isLoggedIn) {
            _state.value = ProfileState()
        }
    }

    fun loadProfile() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getProfileUseCase()
                .onSuccess { profile ->
                    _state.update { it.copy(profile = profile, isLoading = false) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Помилка завантаження профілю.") }
                }
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String? = null,
        newPassword: String? = null
    ) {
        _state.update { it.copy(isUpdating = true, error = null, updateSuccess = false) }
        viewModelScope.launch {
            updateProfileUseCase(firstName, lastName, phoneNumber, email, currentPassword, newPassword)
                .onSuccess { updatedProfile ->
                    _state.update {
                        it.copy(
                            profile = updatedProfile,
                            isUpdating = false,
                            updateSuccess = true
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isUpdating = false,
                            error = throwable.message ?: "Помилка оновлення профілю."
                        )
                    }
                }
        }
    }

    fun resetUpdateState() {
        _state.update { it.copy(updateSuccess = false, error = null) }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToCatalog: () -> Unit, // Назад
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val profileState by profileViewModel.state.collectAsState()

    LaunchedEffect(isLoggedIn) {
        profileViewModel.onAuthStatusChanged(isLoggedIn)
    }

    // --- [НОВА ЛОГІКА] ---
    // Поля тепер завжди редаговані, їх стан зберігається тут
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val currentPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmNewPassword = remember { mutableStateOf("") }

    // Стан для секцій, "My Account" закрыта за замовчуванням
    val openSection = remember { mutableStateOf("") }

    // Функція для скидання полів до оригінальних значень
    fun resetFields() {
        profileState.profile?.let { profile ->
            firstName.value = profile.firstName
            lastName.value = profile.lastName
            phone.value = profile.phoneNumber
            email.value = profile.email
        }
        currentPassword.value = ""
        newPassword.value = ""
        confirmNewPassword.value = ""
        profileViewModel.resetUpdateState() // Скидаємо помилки
    }

    // Ініціалізація та скидання полів при зміні профілю
    LaunchedEffect(profileState.profile) {
        resetFields()
    }

    // Обробка успішного оновлення (скидаємо поля паролів)
    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess) {
            currentPassword.value = ""
            newPassword.value = ""
            confirmNewPassword.value = ""
            profileViewModel.resetUpdateState()
        }
    }

    val passwordsMatch = remember { derivedStateOf { newPassword.value == confirmNewPassword.value } }

    // [НОВА ЛОГІКА] Кнопка "Save Changes" активна, якщо є зміни
    val isSaveEnabled = remember {
        derivedStateOf {
            val profile = profileState.profile
            val hasChanges = profile != null && (
                    firstName.value != profile.firstName ||
                            lastName.value != profile.lastName ||
                            phone.value != profile.phoneNumber ||
                            email.value != profile.email ||
                            newPassword.value.isNotBlank()
                    )

            !profileState.isUpdating && hasChanges &&
                    (!newPassword.value.isNotBlank() || (newPassword.value.length >= 8 && passwordsMatch.value && currentPassword.value.isNotBlank()))
        }
    }


    // [НОВА ЛОГІКА] Обробник для "Cancel"
    val isCancelLoading = remember { mutableStateOf(false) }

    // Обработчик Cancel
    fun onCancelClick() {
        isCancelLoading.value = true

        profileViewModel.viewModelScope.launch {
            kotlinx.coroutines.delay(800) // для красоты анимации
            resetFields()
            isCancelLoading.value = false
        }
    }

    // [НОВА ЛОГІКА] Обробник для "Save Changes"
    fun onSaveClick() {
        if (isSaveEnabled.value) {
            val newPass = if (newPassword.value.isNotBlank() && currentPassword.value.isNotBlank() && passwordsMatch.value) newPassword.value else null
            val currentPass = if (newPass != null) currentPassword.value else null

            profileViewModel.updateProfile(
                firstName = firstName.value,
                lastName = lastName.value,
                phoneNumber = phone.value,
                email = email.value,
                currentPassword = currentPass,
                newPassword = newPass
            )
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F7F8), // Світло-сірий фон
        topBar = {
            TopAppBar(
                title = { Text("Personal Information") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToCatalog) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },

        // [НОВА ЛОГІКА] Нижня панель з кнопками
        bottomBar = {
            if (isLoggedIn) {
                AnimatedVisibility(
                    visible = openSection.value == "Edit Credentials",
                    enter = slideInVertically(
                        initialOffsetY = { it }  // старт снизу экрана
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it }  // уходит вниз
                    ) + fadeOut()
                ) {
                    BottomAppBar(containerColor = Color.White) {

                        val disableButtons = isCancelLoading.value || profileState.isUpdating

                        // CANCEL BUTTON
                        Button(
                            onClick = { onCancelClick() },
                            enabled = isSaveEnabled.value && !profileState.isUpdating && !isCancelLoading.value,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSaveEnabled.value) Color(0xFF2A1036) else Color(0xFFE0E0E0),
                                disabledContainerColor = Color(0xFFE0E0E0),
                                contentColor = if (isSaveEnabled.value) Color.White else Color.Gray,
                                disabledContentColor = Color.Gray
                            )
                        ) {
                            if (isCancelLoading.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.Gray,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Cancel", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        // SAVE BUTTON
                        Button(
                            onClick = { onSaveClick() },
                            enabled = isSaveEnabled.value && !disableButtons,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSaveEnabled.value) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                                disabledContainerColor = Color(0xFFE0E0E0),
                                contentColor = if (isSaveEnabled.value) Color.White else Color.Gray,
                                disabledContentColor = Color.Gray
                            )
                        ) {
                            if (profileState.isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.Gray,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save Changes", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }




    ) { padding ->
        // --- [НОВИЙ МАКЕТ] ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {

            // Якщо не залогінений
            if (!isLoggedIn) {
                item {
                    NotLoggedInPlaceholder(onNavigateToLogin)
                }
            }
            // Якщо помилка завантаження
            else if (profileState.isLoading) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            // Якщо все добре, показуємо секції
            else {
                item {
                    Text(
                        text = "Manage your personal details, delivery addresses, and account preferences",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // --- Секції Акордеону ---

                // 1. My Account
                item {
                    ExpandableSection(
                        title = "My Account",
                        subtitle = "Account information and login credentials",
                        isExpanded = openSection.value == "My Account",
                        onClick = {
                            openSection.value =
                                if (openSection.value == "My Account") "" else "My Account"
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            // First Name
                            Text("First Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = firstName.value.ifBlank { "—" },
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )

                            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                            // Last Name
                            Text("Last Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = lastName.value.ifBlank { "—" },
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )


                        }
                    }
                }



                // 2. Contacts
                item {
                    ExpandableSection(
                        title = "Contacts",
                        subtitle = "Email addresses and phone numbers",
                        isExpanded = openSection.value == "Contacts",
                        onClick = {
                            openSection.value =
                                if (openSection.value == "Contacts") "" else "Contacts"
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Phone display (not editable)
                            Text("Phone", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(phone.value.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)

                            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                            // Email display (not editable)
                            Text("Email", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(email.value.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)
                        }
                    }
                }


                // 3. Delivery Addresses (Заглушка)
                item {
                    ExpandableSection(
                        title = "Delivery Addresses",
                        subtitle = "Saved delivery addresses",
                        isExpanded = openSection.value == "Delivery Addresses",
                        onClick = {
                            openSection.value =
                                if (openSection.value == "Delivery Addresses") "" else "Delivery Addresses"
                        }
                    ) {
                        Text(
                            text = "Тут будуть налаштування персональних даних (стать, дата народження).",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                // 4. My Orders (Заглушка)
                item {
                    ExpandableSection(
                        title = "My orders",
                        subtitle = "Your order history",
                        isExpanded = openSection.value == "My Orders",
                        onClick = {
                            openSection.value =
                                if (openSection.value == "My Orders") "" else "My Orders"
                        }
                    ) {
                        Text(
                            text = "Тут будуть налаштування персональних даних (стать, дата народження).",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                // 5. Edit Credentials
                item {
                    ExpandableSection(
                        title = "Edit Credentials",
                        subtitle = "Edit account information and login credentials",
                        isExpanded = openSection.value == "Edit Credentials",
                        onClick = {
                            openSection.value =
                                if (openSection.value == "Edit Credentials") "" else "Edit Credentials"
                        }
                    ) {
                        // Контент для "My Account" (ваша стара логіка)
                        MyAccountSection(
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            email = email,
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            confirmNewPassword = confirmNewPassword,
                            passwordsMatch = passwordsMatch.value,
                            error = profileState.error,
                            onLogoutClick = { authViewModel.logout() }
                        )
                    }
                }

                // ... і т.д. для інших секцій (Delivery Addresses...)

                // --- Соціальні мережі ---
                item {
                    Spacer(Modifier.height(24.dp))
                    SocialAccountsCard()
                    Spacer(Modifier.height(24.dp)) // Додатковий відступ
                }
            }
        }
    }
}

// --- [НОВІ КОМПОНЕНТИ ДИЗАЙНУ] ---

/**
 * Компонент для секції, що розкривається (акордеон)
 */
@Composable
fun ExpandableSection(
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Згорнути" else "Розгорнути"
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Divider(color = Color(0xFFF0F0F0))
                    content()
                }
            }
        }
    }
}

/**
 * Вміст для секції "My Account"
 */
@Composable
fun MyAccountSection(
    firstName: MutableState<String>,
    lastName: MutableState<String>,
    phone: MutableState<String>,
    email: MutableState<String>,
    currentPassword: MutableState<String>,
    newPassword: MutableState<String>,
    confirmNewPassword: MutableState<String>,
    passwordsMatch: Boolean,
    error: String?,
    onLogoutClick: () -> Unit
) {
    var passVisible by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // [ЛОГІКА ПЕРЕНЕСЕНА СЮДИ]
        // Поля завжди редаговані
        OutlinedTextField(
            value = firstName.value,
            onValueChange = { firstName.value = it },
            label = { Text("Ім'я") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = lastName.value,
            onValueChange = { lastName.value = it },
            label = { Text("Прізвище") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone.value,
            onValueChange = { phone.value = it },
            label = { Text("Телефон") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Divider(Modifier.padding(vertical = 8.dp))

        // Зміна паролю
        Text("Зміна паролю", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        OutlinedTextField(
            value = currentPassword.value,
            onValueChange = { currentPassword.value = it },
            label = { Text("Поточний пароль") },
            singleLine = true,
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newPassword.value,
            onValueChange = { newPassword.value = it },
            label = { Text("Новий пароль (min 8)") },
            singleLine = true,
            visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPassVisible = !confirmPassVisible }) {
                    Icon(if (confirmPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = confirmNewPassword.value,
            onValueChange = { confirmNewPassword.value = it },
            label = { Text("Підтвердити новий пароль") },
            singleLine = true,
            visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = newPassword.value.isNotBlank() && !passwordsMatch,
            modifier = Modifier.fillMaxWidth()
        )
        if (newPassword.value.isNotBlank() && !passwordsMatch) {
            Text("Паролі не співпадають", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Divider(Modifier.padding(vertical = 8.dp))

        // Кнопка "Вийти"
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Text("Вийти з акаунту")
        }
    }
}

/**
 * Картка для підключення соціальних акаунтів
 */
@Composable
fun SocialAccountsCard() {
    Text(
        "Connect Social Accounts",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        text = "Connect your accounts to sync with social networks and log in to the site using Facebook, Google, or Apple",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // TODO: Використовуйте реальні іконки
            SocialRow(icon = R.drawable.ic_launcher_foreground, "Facebook", "Connect to social networks")
            Divider(color = Color(0xFFF0F0F0))
            SocialRow(icon = R.drawable.ic_launcher_foreground, "Google", "Sync contacts")
            Divider(color = Color(0xFFF0F0F0))
            SocialRow(icon = R.drawable.ic_launcher_foreground, "Apple", "Sync contacts")
        }
    }
}

@Composable
fun SocialRow(icon: Int, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(id = icon), contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}


/**
 * Заглушка, якщо користувач не увійшов
 */
@Composable
fun NotLoggedInPlaceholder(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 64.dp), // Додаємо відступи
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Ви не авторизовані.",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "Увійдіть, щоб керувати своїм профілем.",
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Увійти / Зареєструватися")
        }
    }
}