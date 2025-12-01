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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility // [НОВИЙ ІМПОРТ]
import androidx.compose.material.icons.filled.VisibilityOff // [НОВИЙ ІМПОРТ]
import androidx.compose.material.icons.filled.Delete // <--- НОВИЙ ІМПОРТ
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
import androidx.compose.ui.text.style.TextAlign // ВИПРАВЛЕННЯ: Додано імпорт TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.presentation.auth.AuthViewModel
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.usecase.GetProfileUseCase
import com.plugplay.plugplaymobile.domain.usecase.UpdateProfileUseCase
import androidx.lifecycle.ViewModel
import com.plugplay.plugplaymobile.R // [НОВИЙ ІМПОРТ]
import com.plugplay.plugplaymobile.domain.model.UserAddress // <--- НОВИЙ ІМПОРТ
import com.plugplay.plugplaymobile.domain.repository.AuthRepository // <--- НОВИЙ ІМПОРТ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

// [STATE]
data class ProfileState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

// [VIEWMODEL]
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val authRepository: AuthRepository // <--- ДОДАНО ДЛЯ ОТРИМАННЯ ПОВНОГО ПРОФІЛЮ
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

    // [ОНОВЛЕНО] Сигнатура updateProfile: тепер приймає адреси
    fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String? = null,
        newPassword: String? = null,
        addresses: List<UserAddress> = emptyList() // <--- НОВИЙ АРГУМЕНТ
    ) {
        _state.update { it.copy(isUpdating = true, error = null, updateSuccess = false) }
        viewModelScope.launch {
            updateProfileUseCase(firstName, lastName, phoneNumber, email, currentPassword, newPassword, addresses)
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

    // [НОВА ФУНКЦІЯ] Для додавання адреси
    fun addAddress(city: String, street: String, house: String, apartment: String?) {
        val currentProfile = state.value.profile
        if (currentProfile == null) {
            _state.update { it.copy(error = "User profile not loaded. Cannot add address.") }
            return
        }

        // Перевірка на заповненість обов'язкових полів
        if (city.isBlank() || street.isBlank() || house.isBlank()) {
            _state.update { it.copy(error = "City, street, and house number are required.") }
            return
        }

        val newAddress = UserAddress(
            id = null, // null для нової адреси - критично для бекенду
            city = city,
            street = street,
            house = house,
            apartments = apartment?.ifBlank { null }
        )

        // Збираємо новий список адрес: старі + нова
        val addressesToSend = currentProfile.addresses + listOf(newAddress)

        // Викликаємо оновлений загальний метод updateProfile з новим списком адрес,
        // зберігаючи інші поля профілю без змін
        updateProfile(
            firstName = currentProfile.firstName,
            lastName = currentProfile.lastName,
            phoneNumber = currentProfile.phoneNumber,
            email = currentProfile.email,
            addresses = addressesToSend
        )
    }

    // [ОНОВЛЕНА ФУНКЦІЯ] Для видалення адреси
    fun deleteAddress(addressId: Int) {
        val currentProfile = state.value.profile
        if (currentProfile == null) {
            _state.update { it.copy(error = "User profile not loaded. Cannot delete address.") }
            return
        }

        // Перевіряємо, чи є ID у адреси, якщо ні, то ігноруємо
        if (currentProfile.addresses.none { it.id == addressId }) {
            _state.update { it.copy(error = "Address with ID $addressId not found.") }
            return
        }

        val addressesToSend = currentProfile.addresses.filter { it.id != addressId }

        // Викликаємо оновлений загальний метод updateProfile з новим (відфільтрованим) списком адрес
        updateProfile(
            firstName = currentProfile.firstName,
            lastName = currentProfile.lastName,
            phoneNumber = currentProfile.phoneNumber,
            email = currentProfile.email,
            addresses = addressesToSend
        )
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

    // [FIX]: Витягуємо profile в локальну змінну для безпечного Smart Cast
    val profile = profileState.profile

    // ДОДАНО: Стан для Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isLoggedIn) {
        profileViewModel.onAuthStatusChanged(isLoggedIn)
    }

    // --- [НОВА ЛОГІКА] ---
    // Поля тепер завжди редаговані, їх стан зберігається тут
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    // УДАЛЕНО: val currentPassword = remember { mutableStateOf("") }
    // УДАЛЕНО: val newPassword = remember { mutableStateOf("") }
    // УДАЛЕНО: val confirmNewPassword = remember { mutableStateOf("") }

    // Стан для секцій, "My Account" закрыта за замовчуванням
    val openSection = remember { mutableStateOf("") }

    // Функція для скидання полів до оригінальних значень
    fun resetFields() {
        profile?.let { p ->
            firstName.value = p.firstName
            lastName.value = p.lastName
            phone.value = p.phoneNumber
            email.value = p.email
        }
        // УДАЛЕНО: currentPassword.value = ""
        // УДАЛЕНО: newPassword.value = ""
        // УДАЛЕНО: confirmNewPassword.value = ""
        profileViewModel.resetUpdateState() // Скидаємо помилки
    }

    // Ініціалізація та скидання полів при зміні профілю
    LaunchedEffect(profile) { // Використовуємо локальну змінну profile
        resetFields()
    }

    // ДОДАНО: LaunchedEffect для відображення Snackbar та скидання полів
    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully!")

            // Викликаємо resetFields() після успішного оновлення,
            // щоб поля вводу отримали нові значення з profileState.profile
            // Примітка: для додавання/видалення адреси ми також хочемо закрити Delivery Addresses
            if (openSection.value != "Delivery Addresses") {
                profileViewModel.resetUpdateState()
                resetFields()
            }
        }
    }


    // [НОВА ЛОГІКА] Кнопка "Save Changes" активна, якщо є зміни
    val isSaveEnabled = remember {
        derivedStateOf {
            val p = profile
            val hasChanges = p != null && (
                    firstName.value != p.firstName ||
                            lastName.value != p.lastName ||
                            phone.value != p.phoneNumber ||
                            email.value != p.email // <--- ВИПРАВЛЕННЯ: Тепер враховує email
                    )

            !profileState.isUpdating && hasChanges
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
            // Пароли всегда null, так как удалены из UI
            profileViewModel.updateProfile(
                firstName = firstName.value,
                lastName = lastName.value,
                phoneNumber = phone.value,
                email = email.value,
                currentPassword = null,
                newPassword = null,
                // Передаємо поточні адреси, щоб бекенд їх не видалив
                addresses = profile?.addresses ?: emptyList()
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
        // ДОДАНО: SnackbarHost
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // --- ЗМІНА СТРУКТУРИ: Умовний рендеринг для центрування ---

        // 1. Якщо не залогінений: абсолютне центрування
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding), // Враховуємо відступ TopAppBar
                contentAlignment = Alignment.Center
            ) {
                NotLoggedInPlaceholder(onNavigateToLogin)
            }
        }
        // 2. Якщо залогінений, але йде завантаження: абсолютне центрування
        else if (profileState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // 3. Якщо залогінений і успіх: прокручуваний контент
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            ) {
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


                // 3. Delivery Addresses (Виправлено)
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
                        // [НОВИЙ ВМІСТ] Спочатку список адрес, потім форма додавання
                        if (profile != null) { // Використовуємо локальну змінну profile
                            // --- ОНОВЛЕННЯ: Передаємо callback для видалення ---
                            AddressList(
                                addresses = profile.addresses,
                                onDeleteAddress = profileViewModel::deleteAddress
                            )
                        }

                        // --- ОНОВЛЕНИЙ ВИКЛИК ---
                        AddAddressForm(
                            viewModel = profileViewModel,
                            onAddressAdded = {
                                // Закрити секцію після успішного додавання адреси
                                openSection.value = ""
                            }
                        ) // Форма додавання
                        // --- КІНЕЦЬ ОНОВЛЕНОГО ВИКЛИКУ ---
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
                            text = "Тут буде історія ваших замовлень.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                // 5. Edit Credentials
                item {
                    ExpandableSection(
                        title = "Edit Credentials",
                        subtitle = "Edit account information", // Описание стало проще
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
                            error = profileState.error
                            // УДАЛЕНО: все параметры, связанные с паролями и onLogoutClick
                        )

                        // НОВЫЙ БЛОК: кнопки Save/Cancel (если нужно)
                        // Если вы хотите, чтобы кнопки Save/Cancel были внутри секции:
                        if (openSection.value == "Edit Credentials") {
                            SaveCancelButtons(
                                isSaveEnabled = isSaveEnabled.value,
                                isUpdating = profileState.isUpdating,
                                isCancelLoading = isCancelLoading.value,
                                onSaveClick = ::onSaveClick,
                                onCancelClick = ::onCancelClick
                            )
                        }
                    }
                }

                // ... і т.т. для інших секцій (Delivery Addresses...)

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

// --- НОВИЙ КОМПОНЕНТ ФОРМИ АДРЕСИ (згідно скріншоту) ---
@Composable
fun AddAddressForm(
    viewModel: ProfileViewModel = hiltViewModel(),
    // --- ЗМІНА ТУТ: Додано callback для закриття секції ---
    onAddressAdded: () -> Unit
) {
    // Стан для полів
    val city = remember { mutableStateOf("") }
    val street = remember { mutableStateOf("") }
    val house = remember { mutableStateOf("") }
    val apartment = remember { mutableStateOf("") }

    // Стан ViewModel
    val profileState by viewModel.state.collectAsState()

    // --- ДОДАНО LaunchedEffect для обробки успіху ---
    val lastUpdateSuccess by rememberUpdatedState(profileState.updateSuccess)

    LaunchedEffect(lastUpdateSuccess) {
        if (lastUpdateSuccess) {
            // Очищення полів форми
            city.value = ""
            street.value = ""
            house.value = ""
            apartment.value = ""

            // Виклик callback для закриття секції
            onAddressAdded()

            // Скидаємо прапорець, щоб уникнути повторного спрацьовування
            viewModel.resetUpdateState()
        }
    }
    // --- КІНЕЦЬ LaunchedEffect ---

    // Логіка активації кнопки
    val isAddButtonEnabled = remember {
        derivedStateOf {
            city.value.isNotBlank() && street.value.isNotBlank() && house.value.isNotBlank()
        }
    }

    // Перевірка, чи йде оновлення
    val isUpdating = profileState.isUpdating

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Заголовок
        Text(
            "Add New Address",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Рядок 1: City & Street
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = city.value,
                onValueChange = { city.value = it },
                label = { Text("City") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating
            )
            OutlinedTextField(
                value = street.value,
                onValueChange = { street.value = it },
                label = { Text("Street") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating
            )
        }

        Spacer(Modifier.height(16.dp))

        // Рядок 2: House & Apartment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = house.value,
                onValueChange = { house.value = it },
                label = { Text("House") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating
            )
            OutlinedTextField(
                value = apartment.value,
                onValueChange = { apartment.value = it },
                label = { Text("Apartment (optional)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating
            )
        }

        Spacer(Modifier.height(24.dp))

        // Кнопка "Add Address"
        OutlinedButton(
            onClick = {
                viewModel.addAddress(
                    city = city.value,
                    street = street.value,
                    house = house.value,
                    apartment = apartment.value.ifBlank { null }
                )
                // ВИДАЛЕНО локальне очищення полів
            },
            enabled = isAddButtonEnabled.value && !isUpdating, // <-- ДОДАНО ПЕРЕВІРКУ
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Address", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


// --- [НОВЫЙ КОМПОНЕНТ ДЛЯ КНОПОК] ---
@Composable
fun SaveCancelButtons(
    isSaveEnabled: Boolean,
    isUpdating: Boolean,
    isCancelLoading: Boolean,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White), // Фон кнопок
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val disableButtons = isCancelLoading || isUpdating
        val colorScheme = MaterialTheme.colorScheme

        // CANCEL BUTTON
        Button(
            onClick = onCancelClick,
            enabled = isSaveEnabled && !disableButtons,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSaveEnabled) Color(0xFF2A1036) else Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFE0E0E0),
                contentColor = if (isSaveEnabled) Color.White else Color.Gray,
                disabledContentColor = Color.Gray
            )
        ) {
            if (isCancelLoading) {
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
            onClick = onSaveClick,
            enabled = isSaveEnabled && !disableButtons,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSaveEnabled) colorScheme.primary else Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFE0E0E0),
                contentColor = if (isSaveEnabled) Color.White else Color.Gray,
                disabledContentColor = Color.Gray
            )
        ) {
            if (isUpdating) {
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
// --- [КОМПОНЕНТЫ ДИЗАЙНА БЕЗ ИЗМЕНЕНИЙ В СИГНАТУРЕ] ---

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
    error: String?
) {
    // Удалены passVisible и confirmPassVisible

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // [ОСТАВЛЕНО] Поля завжди редаговані
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

        // УДАЛЕНО: Divider, Text("Зміна паролю"), поля паролей и кнопка "Вийти з акаунту"

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
        text = "Connect your accounts to sync with social networks and log in to the site using Google",
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

            SocialRow(icon = R.drawable.google_g_logo, "Google", "Sync contacts")
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
            .fillMaxWidth()
            .padding(horizontal = 24.dp), // Залишаємо горизонтальний відступ для тексту
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "You are not authorized",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            "Sign in to manage your profile",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Log in / Register")
        }
    }
}

// --- ОНОВЛЕНИЙ КОМПОНЕНТ: Список збережених адрес ---
@Composable
fun AddressList(
    addresses: List<UserAddress>,
    // --- ЗМІНА: Отримуємо callback для видалення ---
    onDeleteAddress: (Int) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val isUpdating = profileViewModel.state.collectAsState().value.isUpdating

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (addresses.isEmpty()) {
            Text(
                "You have no saved addresses yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            addresses.forEach { address ->
                SavedAddressCard(
                    address = address,
                    onDeleteClick = onDeleteAddress,
                    isUpdating = isUpdating
                )
            }
        }
    }
}

// --- ОНОВЛЕНИЙ КОМПОНЕНТ: Картка однієї адреси з кнопкою видалення ---
@Composable
fun SavedAddressCard(
    address: UserAddress,
    onDeleteClick: (Int) -> Unit,
    isUpdating: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        // Use Box to position the delete button in the top right corner
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    // Зменшуємо простір для тексту, щоб звільнити місце для кнопки
                    .padding(end = 40.dp)
            ) {
                // Основний рядок: Street, House, Apartment
                Text(
                    text = "${address.street}, ${address.house}${if (address.apartments.isNullOrBlank()) "" else ", apt ${address.apartments}"}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                // Другий рядок: City (та інша додаткова інформація, наприклад, країна/індекс)
                Text(
                    text = address.city,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // --- КНОПКА ВИДАЛЕННЯ ---
            // ID має бути присутнім, щоб мати можливість видалити адресу
            val addressId = address.id
            if (addressId != null) {
                IconButton(
                    onClick = {
                        // Викликаємо функцію видалення з ID, який буде відправлено на бекенд
                        onDeleteClick(addressId)
                    },
                    // Деактивуємо кнопку під час оновлення (додавання/видалення іншої адреси)
                    enabled = !isUpdating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete, // Іконка видалення
                        contentDescription = "Видалити адресу",
                        tint = MaterialTheme.colorScheme.error // Червоний колір
                    )
                }
            }
            // --- КІНЕЦЬ КНОПКИ ВИДАЛЕННЯ ---
        }
    }
}