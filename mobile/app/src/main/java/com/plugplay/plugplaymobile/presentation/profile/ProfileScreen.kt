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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.presentation.auth.AuthViewModel
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.usecase.GetProfileUseCase
import com.plugplay.plugplaymobile.domain.usecase.UpdateProfileUseCase
import androidx.lifecycle.ViewModel
import com.plugplay.plugplaymobile.R
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

// =========================================================================
// !!! МОДЕЛІ ДЛЯ ПРИКЛАДУ (В РЕАЛЬНОСТІ ВОНИ В DOMAIN ШАРІ) !!!
// =========================================================================

/**
 * Клас адреси користувача.
 * Усі поля, крім apartments, вважаються обов'язковими.
 */
data class UserAddress(
    val id: Int? = null, // ID для існуючої адреси. Null для нової.
    val city: String,
    val street: String,
    val house: String,
    val apartments: String? = null
)

/**
 * Оновлений UserProfile, що містить список адрес.
 * Припускаємо, що оригінальний UserProfile оновлено.
 */
// Вважаємо, що оригінальний UserProfile виглядав так:
// data class UserProfile(
//     val id: Int,
//     val firstName: String,
//     val lastName: String,
//     val phoneNumber: String,
//     val email: String,
//     val addresses: List<UserAddress> = emptyList() // <-- ДОДАНО ЦЕ ПОЛЕ
// )
// Якщо ваш оригінальний UserProfile не містить addresses, вам потрібно буде його оновити.

/**
 * Заглушка для репозиторію
 */
interface AuthRepository // Заглушка для коректної ін'єкції

// =========================================================================
// [STATE] (Залишається без змін)
// =========================================================================
data class ProfileState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

// =========================================================================
// [VIEWMODEL] (ОНОВЛЕНО ДЛЯ РОБОТИ З АДРЕСАМИ)
// =========================================================================
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    // Припускаємо, що UpdateProfileUseCase тепер приймає список адрес
    private val updateProfileUseCase: UpdateProfileUseCase,
    // Ін'єкція для отримання повного профілю, якщо потрібно,
    // хоча в цьому прикладі вона не використовується для прямого виклику
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

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
        addresses: List<UserAddress> // <-- ДОДАНО ЦЕЙ АРГУМЕНТ
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

        if (city.isBlank() || street.isBlank() || house.isBlank()) {
            _state.update { it.copy(error = "City, street, and house number are required.") }
            return
        }

        val newAddress = UserAddress(
            id = null,
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
            // Скидаємо паролі, якщо вони не міняються
            currentPassword = null,
            newPassword = null,
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
            // Скидаємо паролі, якщо вони не міняються
            currentPassword = null,
            newPassword = null,
            addresses = addressesToSend
        )
    }

    fun resetUpdateState() {
        _state.update { it.copy(updateSuccess = false, error = null) }
    }
}

// =========================================================================
// [COMPOSE COMPONENTS]
// =========================================================================

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

    val profile = profileState.profile // Локальна змінна для Smart Cast

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isLoggedIn) {
        profileViewModel.onAuthStatusChanged(isLoggedIn)
    }

    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val currentPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmNewPassword = remember { mutableStateOf("") }

    val openSection = remember { mutableStateOf("") }
    val passwordsMatch = remember { derivedStateOf { newPassword.value == confirmNewPassword.value } }

    fun resetFields() {
        profile?.let { p ->
            firstName.value = p.firstName
            lastName.value = p.lastName
            phone.value = p.phoneNumber
            email.value = p.email
        }
        currentPassword.value = ""
        newPassword.value = ""
        confirmNewPassword.value = ""
    }

    LaunchedEffect(profile) {
        if (profile != null) {
            resetFields()
            profileViewModel.resetUpdateState()
        }
    }

    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess) {
            // Показуємо Snackbar лише для загального оновлення профілю (не адреси)
            if (openSection.value == "Edit Credentials") {
                snackbarHostState.showSnackbar("Profile updated successfully!")
            } else if (openSection.value == "Delivery Addresses") {
                // Можна додати окреме повідомлення для адреси, якщо потрібно
                snackbarHostState.showSnackbar("Address list updated successfully!")
            }

            resetFields()
            profileViewModel.resetUpdateState()
        }
    }


    val isSaveEnabled = remember {
        derivedStateOf {
            val p = profile
            val hasProfileChanges = p != null && (
                    firstName.value != p.firstName ||
                            lastName.value != p.lastName ||
                            phone.value != p.phoneNumber ||
                            email.value != p.email
                    )

            val hasPasswordChanges = newPassword.value.isNotBlank() && passwordsMatch.value && currentPassword.value.isNotBlank()

            !profileState.isUpdating && (hasProfileChanges || hasPasswordChanges) &&
                    (!newPassword.value.isNotBlank() || (newPassword.value.length >= 8 && passwordsMatch.value && currentPassword.value.isNotBlank()))
        }
    }


    val isCancelLoading = remember { mutableStateOf(false) }

    fun onCancelClick() {
        isCancelLoading.value = true
        profileViewModel.viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            resetFields()
            isCancelLoading.value = false
        }
    }

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
                newPassword = newPass,
                // **ВАЖЛИВО:** Передаємо поточні адреси, щоб вони не були видалені
                addresses = profile?.addresses ?: emptyList()
            )
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F7F8),
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
        snackbarHost = { SnackbarHost(snackbarHostState) },

        bottomBar = {
            if (isLoggedIn) {
                AnimatedVisibility(
                    // Показуємо BottomBar лише для Edit Credentials
                    visible = openSection.value == "Edit Credentials",
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    BottomAppBar(containerColor = Color.White) {

                        val disableButtons = isCancelLoading.value || profileState.isUpdating

                        // CANCEL BUTTON
                        Button(
                            onClick = { onCancelClick() },
                            enabled = isSaveEnabled.value && !disableButtons,
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
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                NotLoggedInPlaceholder(onNavigateToLogin)
            }
        } else if (profileState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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

                // 1. My Account (Display Only)
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

                            Text("First Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = firstName.value.ifBlank { "—" },
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )

                            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                            Text("Last Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = lastName.value.ifBlank { "—" },
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }


                // 2. Contacts (Display Only)
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
                            Text("Phone", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(phone.value.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)

                            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                            Text("Email", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(email.value.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)
                        }
                    }
                }


                // 3. Delivery Addresses (ОНОВЛЕНО)
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
                        if (profile != null) {
                            AddressList(
                                addresses = profile.addresses,
                                onDeleteAddress = profileViewModel::deleteAddress
                            )
                        }

                        // Форма додавання адреси
                        AddAddressForm(
                            viewModel = profileViewModel,
                            onAddressAdded = {
                                // Закриваємо секцію після успішного додавання
                                openSection.value = ""
                            }
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
                            text = "Тут буде історія ваших замовлень.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                // 5. Edit Credentials (Редагування)
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

                // --- Соціальні мережі ---
                item {
                    Spacer(Modifier.height(24.dp))
                    SocialAccountsCard()
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}


// =========================================================================
// [КОМПОНЕНТИ АДРЕСИ]
// =========================================================================

/**
 * Компонент для форми додавання нової адреси.
 */
@Composable
fun AddAddressForm(
    viewModel: ProfileViewModel = hiltViewModel(),
    onAddressAdded: () -> Unit
) {
    val city = remember { mutableStateOf("") }
    val street = remember { mutableStateOf("") }
    val house = remember { mutableStateOf("") }
    val apartment = remember { mutableStateOf("") }

    val profileState by viewModel.state.collectAsState()
    val lastUpdateSuccess by rememberUpdatedState(profileState.updateSuccess)

    // Обробка успішного додавання адреси
    LaunchedEffect(lastUpdateSuccess) {
        if (lastUpdateSuccess && profileState.error == null) {
            // Очищення полів форми
            city.value = ""
            street.value = ""
            house.value = ""
            apartment.value = ""

            onAddressAdded() // Виклик callback для закриття секції
            viewModel.resetUpdateState()
        }
    }

    val isAddButtonEnabled = remember {
        derivedStateOf {
            city.value.isNotBlank() && street.value.isNotBlank() && house.value.isNotBlank()
        }
    }

    val isUpdating = profileState.isUpdating

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Add New Address",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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

        Button(
            onClick = {
                viewModel.addAddress(
                    city = city.value,
                    street = street.value,
                    house = house.value,
                    apartment = apartment.value.ifBlank { null }
                )
            },
            enabled = isAddButtonEnabled.value && !isUpdating,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
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

/**
 * Список збережених адрес.
 */
@Composable
fun AddressList(
    addresses: List<UserAddress>,
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

/**
 * Картка однієї адреси з кнопкою видалення.
 */
@Composable
fun SavedAddressCard(
    address: UserAddress,
    onDeleteClick: (Int) -> Unit,
    isUpdating: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(end = 40.dp) // Зменшуємо простір для тексту
            ) {
                // Основний рядок: Street, House, Apartment
                Text(
                    text = "${address.street}, ${address.house}${if (address.apartments.isNullOrBlank()) "" else ", apt ${address.apartments}"}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                // Другий рядок: City
                Text(
                    text = address.city,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // КНОПКА ВИДАЛЕННЯ
            val addressId = address.id
            if (addressId != null) {
                IconButton(
                    onClick = {
                        onDeleteClick(addressId)
                    },
                    enabled = !isUpdating, // Деактивуємо під час будь-якого оновлення
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Видалити адресу",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


// =========================================================================
// [ІНШІ КОМПОНЕНТИ] (Залишаються без змін)
// =========================================================================

/**
 * Вміст для секції "My Account" (Тепер Edit Credentials)
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
            // Припускаємо, що R.drawable.google_g_logo існує
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
            .padding(horizontal = 24.dp),
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