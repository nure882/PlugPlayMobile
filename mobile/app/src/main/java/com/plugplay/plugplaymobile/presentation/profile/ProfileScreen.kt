package com.plugplay.plugplaymobile.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import java.time.format.FormatStyle
import java.util.Locale

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
    val snackbarHostState = remember { SnackbarHostState() }

    val profile = profileState.profile
    val orders = profileState.orders

    val isEditingCredentials = remember { mutableStateOf(false) }
    val openSection = remember { mutableStateOf("") }

    // Обработка ошибок через Snackbar
    LaunchedEffect(profileState.error) {
        profileState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            profileViewModel.resetUpdateState() // Сброс ошибки после показа
        }
    }

    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess) {
            isEditingCredentials.value = false
            profileViewModel.resetUpdateState()
        }
    }

    LaunchedEffect(isLoggedIn) {
        profileViewModel.onAuthStatusChanged(isLoggedIn)
    }

    Scaffold(
        containerColor = Color(0xFFF4F7F8),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Personal Information") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToCatalog) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
    ) { padding ->
        if (!isLoggedIn) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                NotLoggedInPlaceholder(onNavigateToLogin)
            }
        } else if (profileState.isLoading && profile == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (profile != null) {
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
                            if (!isEditingCredentials.value && openSection.value == "My Details") {
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
                            profileViewModel = profileViewModel
                        )

                        AddAddressForm(
                            viewModel = profileViewModel,
                            onAddressAdded = { /* Можно добавить логику скрытия формы, если нужно */ }
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)) // Добавил границу для стиля
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("My Wishlist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
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

// --- НЕДОСТАЮЩИЕ КОМПОНЕНТЫ (ИСПРАВЛЕНИЕ ОШИБОК) ---

@Composable
fun ExpandableSection(
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    actionButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f, label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(), // Анимация изменения размера
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color(0xFFE0E0E0))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                if (actionButton != null) {
                    actionButton()
                }

                IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        modifier = Modifier.rotate(rotationState),
                        tint = Color.Gray
                    )
                }
            }

            if (isExpanded) {
                Divider(color = Color(0xFFF0F0F0))
                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun NotLoggedInPlaceholder(onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "You are not logged in",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Log in to view your profile, orders and saved addresses.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Login / Register", fontWeight = FontWeight.Bold)
        }
    }
}

// --- ОСТАЛЬНЫЕ КОМПОНЕНТЫ (Без изменений или с мелкими правками импортов) ---

@Composable
fun AddAddressForm(
    viewModel: ProfileViewModel,
    onAddressAdded: () -> Unit
) {
    val city = remember { mutableStateOf("") }
    val street = remember { mutableStateOf("") }
    val house = remember { mutableStateOf("") }
    val apartment = remember { mutableStateOf("") }

    val profileState by viewModel.state.collectAsState()

    LaunchedEffect(profileState.updateSuccess) {
        if (profileState.updateSuccess && profileState.error == null) {
            // Очищаем только если поля были заполнены (чтобы не сбрасывать при других апдейтах)
            if (city.value.isNotEmpty()) {
                city.value = ""
                street.value = ""
                house.value = ""
                apartment.value = ""
                onAddressAdded()
            }
        }
    }

    val isAddButtonEnabled = remember {
        derivedStateOf {
            city.value.isNotBlank() && street.value.isNotBlank() && house.value.isNotBlank()
        }
    }

    val isUpdating = profileState.isUpdating

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text(
            "Add New Address",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = house.value, onValueChange = { house.value = it },
                label = { Text("House") }, singleLine = true, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp), enabled = !isUpdating
            )
            OutlinedTextField(
                value = apartment.value, onValueChange = { apartment.value = it },
                label = { Text("Apt (opt)") }, singleLine = true, modifier = Modifier.weight(1f),
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
    profileViewModel: ProfileViewModel
) {
    val state by profileViewModel.state.collectAsState()
    val isUpdating = state.isUpdating
    val addressToEdit = remember { mutableStateOf<UserAddress?>(null) }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess && addressToEdit.value != null) {
            addressToEdit.value = null
        }
    }

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
                        },
                        onCancel = { addressToEdit.value = null },
                        isUpdating = isUpdating
                    )
                } else {
                    SavedAddressCard(
                        address = address,
                        onDeleteClick = { id -> onDeleteAddress(id) },
                        onEditClick = { addressToEdit.value = address },
                        isUpdating = isUpdating
                    )
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
        CredentialRow("First Name", profile.firstName)
        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))
        CredentialRow("Last Name", profile.lastName)
        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))
        CredentialRow("Phone", profile.phoneNumber)
        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))
        CredentialRow("Email", profile.email)
    }
}

@Composable
fun CredentialRow(label: String, value: String) {
    Column {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value.ifBlank { "—" },
            fontSize = 16.sp,
            color = Color.Black
        )
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
                    label = { Text("Apt (opt)") }, singleLine = true, modifier = Modifier.weight(1f),
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
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                val addressId = address.id
                if (addressId != null) {
                    IconButton(
                        onClick = { onDeleteClick(addressId) },
                        enabled = !isUpdating,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
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

    val orderStatusColor = when(order.status) {
        OrderStatus.Created -> Color(0xFFF9A825)
        OrderStatus.Approved -> Color(0xFF1976D2)
        OrderStatus.Collected -> Color(0xFF388E3C)
        OrderStatus.Delivered -> Color(0xFF9C27B0)
        OrderStatus.Cancelled -> Color.Gray
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
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.getDefault()).format(zonedDateTime)
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
                    Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(orderDateFormatted, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = orderStatusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, orderStatusColor)
                    ) {
                        Text(
                            text = orderStatusDisplay,
                            style = MaterialTheme.typography.labelSmall,
                            color = orderStatusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(formatHryvnia(totalWithShipping), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Expanded Content
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Divider(color = Color(0xFFF0F0F0))
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrderInfoRow(Icons.Filled.Call, "Delivery", deliveryLabel)
                        OrderInfoRow(Icons.Filled.Info, "Payment", when(order.paymentMethod) {
                            PaymentMethod.Card -> "Card"
                            PaymentMethod.CashAfterDelivery -> "Cash on Delivery"
                            PaymentMethod.GooglePay -> "Google Pay"
                        })
                        OrderInfoRow(Icons.Filled.Check, "Status", paymentStatusDisplay)
                        if (order.deliveryMethod != DeliveryMethod.Pickup) {
                            OrderInfoRow(Icons.Filled.ShoppingCart, "Address", formattedAddress)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("Items", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            order.orderItems.forEach { item ->
                                OrderItemDisplay(item = item, formatHryvnia = ::formatHryvnia)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        CostBreakdownRow("Subtotal", formatHryvnia(order.totalAmount), isTotal = false)
                        CostBreakdownRow("Delivery", formatHryvnia(deliveryPrice), isTotal = false)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        CostBreakdownRow("Total", formatHryvnia(totalWithShipping), isTotal = true)

                        if (canCancel) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { if (!isCancelling) onCancelOrder(order.id) },
                                enabled = !isCancelling,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                if (isCancelling) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(20.dp))
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
}

@Composable
fun OrderItemDisplay(item: OrderItem, formatHryvnia: (Double) -> String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${formatHryvnia(item.price)} x ${item.quantity}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            formatHryvnia(item.price * item.quantity),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun OrderInfoRow(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, color = Color.Black)
        }
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