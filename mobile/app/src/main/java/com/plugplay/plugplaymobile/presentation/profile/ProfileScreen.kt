package com.plugplay.plugplaymobile.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.presentation.auth.AuthViewModel
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.model.Order
import com.plugplay.plugplaymobile.domain.model.OrderItem
import com.plugplay.plugplaymobile.domain.model.OrderStatus
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.PaymentStatus
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.format.FormatStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToCatalog: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val profileState by profileViewModel.state.collectAsState()

    val profile = profileState.profile
    val orders = profileState.orders
    var localProfile by remember(profile) { mutableStateOf(profile) }
    LaunchedEffect(profile) {
        localProfile = profile
    }

    val isEditingCredentials = remember { mutableStateOf(false) }
    val openSection = remember { mutableStateOf("") }

    // --- ЛОГІКА: Закриваємо форму редагування після успішного збереження ---
    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess) {
            // Якщо була відкрита форма редагування профілю - закриваємо її
            if (isEditingCredentials.value) {
                isEditingCredentials.value = false
                profileViewModel.resetUpdateState()
                profileViewModel.onAuthStatusChanged(true) // Оновлюємо дані
            }
        }
    }
    // ---------------------------------------------------------------------------

    LaunchedEffect(isLoggedIn) {
        profileViewModel.onAuthStatusChanged(isLoggedIn)
    }

    Scaffold(
        containerColor = Color(0xFFF4F7F8),
        topBar = {
            TopAppBar(
                title = { Text("Personal Information") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToCatalog) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
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
                            // Кнопка Edit відображається тільки якщо ми НЕ в режимі редагування
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
                        // Внутри LazyColumn -> ExpandableSection
                        if (isEditingCredentials.value) {
                            EditCredentialsForm(
                                profile = localProfile ?: profile!!, // Берем данные из локальной копии
                                onSave = { fn, ln, ph, em ->
                                    // 1. Мгновенно обновляем локальное состояние
                                    localProfile = localProfile?.copy(
                                        firstName = fn,
                                        lastName = ln,
                                        phoneNumber = ph,
                                        email = em
                                    )
                                    // 2. Закрываем форму редактирования сразу
                                    isEditingCredentials.value = false

                                    // 3. Отправляем реальный запрос в ViewModel
                                    profileViewModel.updateProfile(fn, ln, ph, em)
                                },
                                onCancel = { isEditingCredentials.value = false },
                                isUpdating = profileState.isUpdating
                            )
                        } else {
                            // Здесь тоже используем localProfile, чтобы данные изменились мгновенно
                            DisplayCredentials(profile = localProfile ?: profile!!)
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
                            addresses = localProfile?.addresses ?: emptyList(),
                            onDeleteAddress = profileViewModel::deleteAddress,
                            onEditAddress = profileViewModel::editAddress,
                            onUpdateLocalList = { newList ->
                                // Мгновенно подменяем адреса в локальном профиле
                                localProfile = localProfile?.copy(addresses = newList)
                            }
                        )

                        AddAddressForm(
                            viewModel = profileViewModel,
                            onAddressAdded = { newAddress ->
                                // Мгновенно добавляем новый адрес в локальный список
                                localProfile = localProfile?.let { lp ->
                                    lp.copy(addresses = lp.addresses + newAddress)
                                }
                            }
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
                        if (profileState.isOrdersLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            OrderHistoryList(
                                orders = orders,
                                addresses = profile.addresses,
                                onCancelOrder = profileViewModel::cancelOrder,
                                isCancelling = profileState.isUpdating
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(onClick = onNavigateToWishlist),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("My Wishlist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
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
                        Text("Logout", fontWeight = FontWeight.SemiBold)
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

        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))

        Text("Last Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(
            text = profile.lastName.ifBlank { "—" },
            fontSize = 16.sp,
            color = Color.DarkGray
        )

        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))

        Text("Phone", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(profile.phoneNumber.ifBlank { "—" }, fontSize = 15.sp, color = Color.DarkGray)

        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))

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

    // Регулярное выражение для формата +380YYXXXXXXX
    val phoneRegex = remember { Regex("^\\+380\\d{9}$") }

    val isPhoneValid = remember {
        derivedStateOf { phoneRegex.matches(phone.value) }
    }

    val isSaveEnabled = remember {
        derivedStateOf {
            !isUpdating &&
                    firstName.value.isNotBlank() &&
                    lastName.value.isNotBlank() &&
                    isPhoneValid.value && // Проверка валидности телефона
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

        // Поле ввода телефона с валидацией
        OutlinedTextField(
            value = phone.value,
            onValueChange = { newValue ->
                // Разрешаем вводить только цифры и +, ограничиваем длину до 13 символов
                if (newValue.all { it.isDigit() || it == '+' } && newValue.length <= 13) {
                    phone.value = newValue
                }
            },
            label = { Text("Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isUpdating,
            isError = phone.value.isNotEmpty() && !isPhoneValid.value,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            )
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
    onAddressAdded: (com.plugplay.plugplaymobile.domain.model.UserAddress) -> Unit
) {
    val city = remember { mutableStateOf("") }
    val street = remember { mutableStateOf("") }
    val house = remember { mutableStateOf("") }
    val apartment = remember { mutableStateOf("") }

    val profileState by viewModel.state.collectAsState()
    val isUpdating = profileState.isUpdating

    val isAddButtonEnabled = remember {
        derivedStateOf {
            city.value.isNotBlank() && street.value.isNotBlank() && house.value.isNotBlank()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Add New Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = city.value, onValueChange = { city.value = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isUpdating)
            OutlinedTextField(value = street.value, onValueChange = { street.value = it }, label = { Text("Street") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isUpdating)
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = house.value, onValueChange = { house.value = it }, label = { Text("House") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isUpdating)
            OutlinedTextField(value = apartment.value, onValueChange = { apartment.value = it }, label = { Text("Apartment (optional)") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isUpdating)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val tempAddress = com.plugplay.plugplaymobile.domain.model.UserAddress(
                    id = -1, // ВАЖНО: Ставим -1 вместо null, чтобы форма редактирования не открылась сразу
                    city = city.value,
                    street = street.value,
                    house = house.value,
                    apartments = apartment.value.ifBlank { null }
                )

                onAddressAdded(tempAddress)

                viewModel.addAddress(
                    city = city.value,
                    street = street.value,
                    house = house.value,
                    apartment = apartment.value.ifBlank { null }
                )

                city.value = ""; street.value = ""; house.value = ""; apartment.value = ""
            },
            enabled = isAddButtonEnabled.value && !isUpdating,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isUpdating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
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
    onUpdateLocalList: (List<UserAddress>) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val state by profileViewModel.state.collectAsState()
    val isUpdating = state.isUpdating
    val addressToEdit = remember { mutableStateOf<UserAddress?>(null) }

    // --- НОВИЙ СТАН: ID адреси для видалення ---
    val showDeleteConfirmation = remember { mutableStateOf<Int?>(null) }

    // --- ДІАЛОГ ПІДТВЕРДЖЕННЯ ---
    if (showDeleteConfirmation.value != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = null },
            title = { Text("Delete Address") },
            text = { Text("Are you sure you want to delete this address? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation.value?.let { id ->
                            // 1. ОПТИМІСТИЧНЕ ОНОВЛЕННЯ: видаляємо зі списку миттєво
                            val updatedList = addresses.filter { it.id != id }
                            onUpdateLocalList(updatedList)

                            // 2. Відправляємо реальний запит на сервер
                            onDeleteAddress(id)
                        }
                        // 3. Закриваємо діалог
                        showDeleteConfirmation.value = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation.value = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
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
                            val updatedList = addresses.map {
                                if (it.id == address.id) {
                                    it.copy(city = city, street = street, house = house, apartments = apartment)
                                } else it
                            }
                            onUpdateLocalList(updatedList)
                            addressToEdit.value = null
                            onEditAddress(address.id, city, street, house, apartment)
                        },
                        onCancel = { addressToEdit.value = null },
                        isUpdating = isUpdating
                    )
                } else {
                    SavedAddressCard(
                        address = address,
                        // ЗАМІНА: Замість прямого видалення — відкриваємо діалог
                        onDeleteClick = { id -> showDeleteConfirmation.value = id },
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
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = street.value, onValueChange = { street.value = it },
                label = { Text("Street") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = house.value, onValueChange = { house.value = it },
                    label = { Text("House") }, singleLine = true, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = apartment.value, onValueChange = { apartment.value = it },
                    label = { Text("Apt (optional)") }, singleLine = true, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        // Вызываем onSave: форма закроется мгновенно в родителе
                        onSave(city.value, street.value, house.value, apartment.value)
                    },
                    enabled = city.value.isNotBlank() && street.value.isNotBlank() && house.value.isNotBlank(),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text("Save")
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
                    HorizontalDivider(color = Color(0xFFF0F0F0))
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

@Composable
fun OrderHistoryList(
    orders: List<Order>,
    addresses: List<UserAddress>,
    onCancelOrder: (orderId: Int) -> Unit,
    isCancelling: Boolean
) {
    if (orders.isEmpty()) {
        Text(
            text = "You haven't placed any orders yet.",
            modifier = Modifier.padding(16.dp),
            color = Color.Gray
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            orders.sortedByDescending { it.orderDate }.forEach { order ->
                OrderHistoryCard(
                    order = order,
                    addresses = addresses,
                    onCancelOrder = onCancelOrder,
                    isCancelling = isCancelling
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryCard(
    order: Order,
    addresses: List<UserAddress>,
    onCancelOrder: (orderId: Int) -> Unit,
    isCancelling: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    fun formatHryvnia(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount) + " ₴"
    }

    val deliveryPrice = when (order.deliveryMethod) {
        DeliveryMethod.Courier -> 100.0
        DeliveryMethod.Post -> 80.0
        DeliveryMethod.Premium -> 150.0
        DeliveryMethod.Pickup -> 0.0
    }
    val deliveryLabel = when (order.deliveryMethod) {
        DeliveryMethod.Courier -> "Courier Delivery"
        DeliveryMethod.Post -> "Postal Service"
        DeliveryMethod.Premium -> "Premium Delivery"
        DeliveryMethod.Pickup -> "Store Pickup"
    }

    val totalWithShipping = order.totalAmount + deliveryPrice
    val canCancel = order.status == OrderStatus.Created || order.status == OrderStatus.Approved

    fun findAddress(addressId: Int): UserAddress? {
        return addresses.find { it.id == addressId }
    }

    val formattedAddress = remember(order.deliveryAddressId, addresses) {
        val address = findAddress(order.deliveryAddressId)
        address?.let {
            "${it.city}, ${it.street} ${it.house}${if (!it.apartments.isNullOrBlank()) ", apt ${it.apartments}" else ""}"
        } ?: "Address information not available"
    }

    val orderStatusDisplay = when(order.status) {
        OrderStatus.Created -> "Created"
        OrderStatus.Approved -> "Approved"
        OrderStatus.Collected -> "Collected"
        OrderStatus.Delivered -> "Delivered"
        OrderStatus.Cancelled -> "Cancelled"
    }

    val paymentStatusDisplay = when(order.paymentStatus) {
        PaymentStatus.Paid -> "Paid"
        PaymentStatus.Failed -> "Failed"
        PaymentStatus.TestPaid -> "Test paid"
        PaymentStatus.NotPaid -> "Not paid"
    }

    val orderDateFormatted = remember(order.orderDate) {
        try {
            val zonedDateTime = ZonedDateTime.parse(order.orderDate)
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale("uk", "UA")).format(zonedDateTime)
        } catch (e: Exception) {
            "Invalid date"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Order ID", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("#${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Date", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(orderDateFormatted, style = MaterialTheme.typography.bodyMedium)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        orderStatusDisplay,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when(order.status) {
                            OrderStatus.Created -> Color(0xFFF9A825)
                            OrderStatus.Approved -> Color(0xFF1976D2)
                            OrderStatus.Collected -> Color(0xFF388E3C)
                            OrderStatus.Delivered -> Color(0xFF9C27B0)
                            OrderStatus.Cancelled -> Color.Gray
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Total Amount", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(formatHryvnia(totalWithShipping), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrderInfoRow(Icons.Filled.Call, "Delivery Method", deliveryLabel)
                    OrderInfoRow(Icons.Filled.Info, "Payment Method", when(order.paymentMethod) {
                        PaymentMethod.Card -> "Card"
                        PaymentMethod.CashAfterDelivery -> "Cash after delivery"
                        PaymentMethod.GooglePay -> "Google Pay"
                    })
                    OrderInfoRow(Icons.Filled.Check, "Payment Status", paymentStatusDisplay)
                    OrderInfoRow(Icons.Filled.ShoppingCart, "Full Address", formattedAddress)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Order Items", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        order.orderItems.forEach { item ->
                            OrderItemDisplay(item = item, formatHryvnia = ::formatHryvnia)
                        }
                    }

                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        CostBreakdownRow("Subtotal", formatHryvnia(order.totalAmount), isTotal = false)
                        CostBreakdownRow("Shipment Cost ($deliveryLabel)", formatHryvnia(deliveryPrice), isTotal = false)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        CostBreakdownRow("Total", formatHryvnia(totalWithShipping), isTotal = true)
                    }

                    if (canCancel) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (!isCancelling) {
                                    onCancelOrder(order.id)
                                }
                            },
                            enabled = !isCancelling,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        ) {
                            if (isCancelling) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Clear, contentDescription = "Cancel", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Cancel Order", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemDisplay(item: OrderItem, formatHryvnia: (Double) -> String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                "Price: ${formatHryvnia(item.price)}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Qty: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
            Text(
                formatHryvnia(item.price * item.quantity),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun OrderInfoRow(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
fun CostBreakdownRow(label: String, amount: String, isTotal: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTotal) 16.sp else 14.sp,
            color = if (isTotal) Color.Black else Color.Gray
        )
        Text(
            amount,
            fontWeight = if (isTotal) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = if (isTotal) 16.sp else 14.sp,
            color = Color.Black
        )
    }
}