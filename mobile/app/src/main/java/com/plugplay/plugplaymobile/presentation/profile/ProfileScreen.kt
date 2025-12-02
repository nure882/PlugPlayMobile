package com.plugplay.plugplaymobile.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.presentation.auth.AuthViewModel
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.usecase.GetProfileUseCase
import com.plugplay.plugplaymobile.domain.usecase.UpdateProfileUseCase
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class ProfileState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

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
                .onSuccess {
                    // Встановлюємо успіх і викликаємо перезавантаження
                    _state.update { it.copy(isUpdating = false, updateSuccess = true) }
                    loadProfile()
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

    fun updateAddresses(newAddresses: List<UserAddress>) {
        val currentProfile = state.value.profile ?: return
        updateProfile(
            firstName = currentProfile.firstName,
            lastName = currentProfile.lastName,
            phoneNumber = currentProfile.phoneNumber,
            email = currentProfile.email,
            addresses = newAddresses
        )
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
        updateAddresses(addressesToSend)
    }

    fun editAddress(addressId: Int?, city: String, street: String, house: String, apartment: String?) {
        val currentProfile = state.value.profile ?: return
        if (addressId == null) return

        val updatedAddresses = currentProfile.addresses.map { address ->
            if (address.id == addressId) {
                address.copy(
                    city = city,
                    street = street,
                    house = house,
                    apartments = apartment?.ifBlank { null }
                )
            } else {
                address
            }
        }
        updateAddresses(updatedAddresses)
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
        updateAddresses(addressesToSend)
    }

    fun resetUpdateState() {
        _state.update { it.copy(updateSuccess = false, error = null) }
    }
}

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

    val isEditingCredentials = remember { mutableStateOf(false) }
    val openSection = remember { mutableStateOf("") }

    LaunchedEffect(isLoggedIn) {
        profileViewModel.onAuthStatusChanged(isLoggedIn)
    }

    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess) {
            isEditingCredentials.value = false
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
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) },
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

                item {
                    ExpandableSection(
                        title = "My Details & Contacts",
                        subtitle = "Account information, email, and phone number",
                        isExpanded = openSection.value == "My Details",
                        onClick = {
                            openSection.value =
                                if (openSection.value == "My Details") "" else "My Details"
                        },
                        actionButton = {
                            if (!isEditingCredentials.value) {
                                TextButton(
                                    onClick = { isEditingCredentials.value = true },
                                    enabled = !profileState.isUpdating
                                ) {
                                    Text("Edit")
                                }
                            }
                        }
                    ) {
                        if (isEditingCredentials.value) {
                            EditCredentialsForm(
                                profile = profile,
                                onSave = { fn, ln, ph, em ->
                                    profileViewModel.updateProfile(fn, ln, ph, em)
                                },
                                onCancel = { isEditingCredentials.value = false },
                                isUpdating = profileState.isUpdating
                            )
                        } else {
                            DisplayCredentials(profile = profile)
                        }
                    }
                }

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
                            onDeleteAddress = profileViewModel::deleteAddress,
                            onEditAddress = profileViewModel::editAddress,
                        )

                        AddAddressForm(
                            viewModel = profileViewModel,
                            onAddressAdded = { openSection.value = "" }
                        )
                    }
                }

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

@Composable
fun DisplayCredentials(profile: UserProfile) {
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

        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

        Text("Phone", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(profile.phoneNumber.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)

        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

        Text("Email", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(profile.email.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)
    }
}

@Composable
fun EditCredentialsForm(
    profile: UserProfile,
    onSave: (firstName: String, lastName: String, phoneNumber: String, email: String) -> Unit,
    onCancel: () -> Unit,
    isUpdating: Boolean
) {
    val firstName = remember { mutableStateOf(profile.firstName) }
    val lastName = remember { mutableStateOf(profile.lastName) }
    val phone = remember { mutableStateOf(profile.phoneNumber) }
    val email = remember { mutableStateOf(profile.email) }

    val isSaveEnabled = remember {
        derivedStateOf {
            !isUpdating &&
                    firstName.value.isNotBlank() &&
                    lastName.value.isNotBlank() &&
                    phone.value.isNotBlank() &&
                    email.value.isNotBlank() &&
                    (firstName.value != profile.firstName ||
                            lastName.value != profile.lastName ||
                            phone.value != profile.phoneNumber ||
                            email.value != profile.email)
        }
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = firstName.value, onValueChange = { firstName.value = it },
                label = { Text("First Name") }, singleLine = true, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
            OutlinedTextField(
                value = lastName.value, onValueChange = { lastName.value = it },
                label = { Text("Last Name") }, singleLine = true, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
        }

        OutlinedTextField(
            value = phone.value, onValueChange = { phone.value = it },
            label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), enabled = !isUpdating
        )

        OutlinedTextField(
            value = email.value, onValueChange = { email.value = it },
            label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), enabled = !isUpdating
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onSave(firstName.value, lastName.value, phone.value, email.value) },
                enabled = isSaveEnabled.value,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Save Changes", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

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

@Composable
fun AddressList(
    addresses: List<UserAddress>,
    onDeleteAddress: (Int) -> Unit,
    onEditAddress: (addressId: Int?, city: String, street: String, house: String, apartment: String?) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val isUpdating = profileViewModel.state.collectAsState().value.isUpdating
    val addressToEdit = remember { mutableStateOf<UserAddress?>(null) }

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
                if (addressToEdit.value?.id == address.id) {
                    EditAddressForm(
                        address = address,
                        onSave = { city, street, house, apartment ->
                            onEditAddress(address.id, city, street, house, apartment)
                            addressToEdit.value = null
                        },
                        onCancel = { addressToEdit.value = null },
                        isUpdating = isUpdating
                    )
                } else {
                    SavedAddressCard(
                        address = address,
                        onDeleteClick = onDeleteAddress,
                        onEditClick = { addressToEdit.value = address },
                        isUpdating = isUpdating
                    )
                }
            }
        }
    }
}

@Composable
fun EditAddressForm(
    address: UserAddress,
    onSave: (city: String, street: String, house: String, apartment: String?) -> Unit,
    onCancel: () -> Unit,
    isUpdating: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEA)),
        border = BorderStroke(1.dp, Color(0xFFFFCC00))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Edit Address", fontWeight = FontWeight.Bold)

            val city = remember { mutableStateOf(address.city) }
            val street = remember { mutableStateOf(address.street) }
            val house = remember { mutableStateOf(address.house ?: "") }
            val apartment = remember { mutableStateOf(address.apartments ?: "") }

            OutlinedTextField(
                value = city.value, onValueChange = { city.value = it },
                label = { Text("City") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
            OutlinedTextField(
                value = street.value, onValueChange = { street.value = it },
                label = { Text("Street") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = house.value, onValueChange = { house.value = it },
                    label = { Text("House") }, singleLine = true, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), enabled = !isUpdating
                )
                OutlinedTextField(
                    value = apartment.value, onValueChange = { apartment.value = it },
                    label = { Text("Apt (optional)") }, singleLine = true, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), enabled = !isUpdating
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(40.dp),
                    enabled = !isUpdating
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onSave(city.value, street.value, house.value, apartment.value) },
                    enabled = !isUpdating && city.value.isNotBlank() && street.value.isNotBlank() && house.value.isNotBlank(),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}


@Composable
fun SavedAddressCard(
    address: UserAddress,
    onDeleteClick: (Int) -> Unit,
    onEditClick: () -> Unit,
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
                    .padding(end = 90.dp)
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

            Row(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                IconButton(
                    onClick = onEditClick,
                    enabled = !isUpdating,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редагувати адресу",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                val addressId = address.id
                if (addressId != null) {
                    IconButton(
                        onClick = { onDeleteClick(addressId) },
                        enabled = !isUpdating,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Видалити адресу",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ExpandableSection(
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    actionButton: @Composable (() -> Unit)? = null,
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
                actionButton?.invoke()
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