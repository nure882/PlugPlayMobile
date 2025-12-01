package com.plugplay.plugplaymobile.presentation.profile

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
 */
data class UserAddress(
    val id: Int? = null,
    val city: String,
    val street: String,
    val house: String,
    val apartments: String? = null
)

/**
 * Заглушка для репозиторію
 */
interface AuthRepository

interface GetProfileUseCase {
    suspend operator fun invoke(): Result<UserProfile>
}
interface UpdateProfileUseCase {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String? = null,
        newPassword: String? = null,
        addresses: List<UserAddress> = emptyList()
    ): Result<UserProfile>
}


// =========================================================================
// [STATE]
// =========================================================================
data class ProfileState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

// =========================================================================
// [VIEWMODEL]
// =========================================================================
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
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

    fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String? = null,
        newPassword: String? = null,
        addresses: List<UserAddress> = emptyList()
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

    fun addAddress(city: String, street: String, house: String, apartment: String?) {
        val currentProfile = state.value.profile ?: run {
            _state.update { it.copy(error = "User profile not loaded. Cannot add address.") }
            return
        }

        if (city.isBlank() || street.isBlank() || house.isBlank()) {
            _state.update { it.copy(error = "City, street, and house number are required.") }
            return
        }

        val newAddress = UserAddress(
            id = null, city = city, street = street, house = house, apartments = apartment?.ifBlank { null }
        )

        val addressesToSend = currentProfile.addresses + listOf(newAddress)

        updateProfile(
            firstName = currentProfile.firstName,
            lastName = currentProfile.lastName,
            phoneNumber = currentProfile.phoneNumber,
            email = currentProfile.email,
            addresses = addressesToSend
        )
    }

    fun deleteAddress(addressId: Int) {
        val currentProfile = state.value.profile ?: run {
            _state.update { it.copy(error = "User profile not loaded. Cannot delete address.") }
            return
        }

        if (currentProfile.addresses.none { it.id == addressId }) {
            _state.update { it.copy(error = "Address with ID $addressId not found.") }
            return
        }

        val addressesToSend = currentProfile.addresses.filter { it.id != addressId }

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

// =========================================================================
// [COMPOSE COMPONENTS]
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToCatalog: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val profileState by profileViewModel.state.collectAsState()

    val profile = profileState.profile

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isLoggedIn) {
        profileViewModel.onAuthStatusChanged(isLoggedIn)
    }

    // Редаговані поля (залишаємо для Edit Credentials, якщо ви вирішите його повернути)
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }

    // Прибираємо стани паролів, оскільки Edit Credentials видалено
    val currentPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmNewPassword = remember { mutableStateOf("") }
    val passwordsMatch = remember { derivedStateOf { newPassword.value == confirmNewPassword.value } }

    val openSection = remember { mutableStateOf("") }

    // Ініціалізація полів при завантаженні профілю
    LaunchedEffect(profile) {
        if (profile != null) {
            // Присвоюємо значення локальним станам
            firstName.value = profile.firstName
            lastName.value = profile.lastName
            phone.value = profile.phoneNumber
            email.value = profile.email
            profileViewModel.resetUpdateState()
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
        } else if (profileState.isLoading || profile == null) {
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
                                text = profile.firstName.ifBlank { "—" },
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )

                            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                            Text("Last Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = profile.lastName.ifBlank { "—" },
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
                            Text(profile.phoneNumber.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)

                            Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                            Text("Email", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(profile.email.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)
                        }
                    }
                }


                // 3. Delivery Addresses
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
                        AddressList(
                            addresses = profile.addresses,
                            onDeleteAddress = profileViewModel::deleteAddress
                        )

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

                // [НОВИЙ ЕЛЕМЕНТ] Кнопка виходу з акаунту
                item {
                    Spacer(Modifier.height(32.dp))
                    OutlinedButton(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Вийти з акаунту", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}


// =========================================================================
// [КОМПОНЕНТИ АДРЕСИ]
// =========================================================================

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

    LaunchedEffect(lastUpdateSuccess) {
        if (lastUpdateSuccess && profileState.error == null) {
            city.value = ""
            street.value = ""
            house.value = ""
            apartment.value = ""

            onAddressAdded()
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
                value = city.value, onValueChange = { city.value = it },
                label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
            OutlinedTextField(
                value = street.value, onValueChange = { street.value = it },
                label = { Text("Street") }, singleLine = true, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = house.value, onValueChange = { house.value = it },
                label = { Text("House") }, singleLine = true, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
            OutlinedTextField(
                value = apartment.value, onValueChange = { apartment.value = it },
                label = { Text("Apartment (optional)") }, singleLine = true, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.addAddress(
                    city = city.value, street = street.value, house = house.value, apartment = apartment.value.ifBlank { null }
                )
            },
            enabled = isAddButtonEnabled.value && !isUpdating,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp
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
                    .padding(end = 40.dp)
            ) {
                Text(
                    text = "${address.street}, ${address.house}${if (address.apartments.isNullOrBlank()) "" else ", apt ${address.apartments}"}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = address.city,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            val addressId = address.id
            if (addressId != null) {
                IconButton(
                    onClick = { onDeleteClick(addressId) },
                    enabled = !isUpdating,
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