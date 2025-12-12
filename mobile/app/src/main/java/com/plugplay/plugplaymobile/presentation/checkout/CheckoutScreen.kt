package com.plugplay.plugplaymobile.presentation.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.model.UserProfile
// [ВАЖЛИВО] Імпортуємо доменні моделі з аліасами, щоб не плутати з UI моделями
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod as DomainDeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod as DomainPaymentMethod

// --- МОДЕЛІ ДЛЯ UI (Залишаємось без змін, вони потрібні для верстки) ---
sealed class DeliveryMethod(val title: String, val subtitle: String, val icon: ImageVector) {
    object Courier : DeliveryMethod("Courier", "Delivery 1-2 days", Icons.Outlined.LocalShipping)
    object Post : DeliveryMethod("Post", "Delivery 3-5 days", Icons.Outlined.Inventory)
    object Premium : DeliveryMethod("Premium Delivery", "Same day delivery", Icons.Outlined.FlashOn)
    object Pickup : DeliveryMethod("Pickup", "Collect from store", Icons.Outlined.Store)
}

sealed class PaymentMethod(val title: String, val subtitle: String, val icon: ImageVector) {
    object Card : PaymentMethod("Card", "Pay online with card (LiqPay)", Icons.Outlined.CreditCard)
    object CashAfterDelivery : PaymentMethod("Cash after delivery", "Pay when you receive", Icons.Outlined.AttachMoney)
}

// --- ОСНОВНИЙ ЕКРАН ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderConfirmed: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val checkoutState by viewModel.state.collectAsState()
    val isLoggedIn = checkoutState.isLoggedIn
    val snackbarHostState = remember { SnackbarHostState() }

    // Слухаємо успішне замовлення
    LaunchedEffect(checkoutState.orderSuccess) {
        if (checkoutState.orderSuccess) {
            onOrderConfirmed()
            viewModel.resetOrderState()
        }
    }

    // Слухаємо помилки
    LaunchedEffect(checkoutState.orderError) {
        checkoutState.orderError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.resetOrderState()
        }
    }

    // --- STATE HOISTING ---
    var guestName by remember { mutableStateOf("") }
    var guestLastName by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }

    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }

    var selectedDelivery by remember { mutableStateOf<DeliveryMethod>(DeliveryMethod.Courier) }
    var selectedPayment by remember { mutableStateOf<PaymentMethod>(PaymentMethod.Card) }

    // Логіка автозаповнення адреси для залогіненого юзера
    val onAddressSelected: (UserAddress) -> Unit = { address ->
        city = address.city
        street = address.street
        house = address.house ?: ""
        apartment = address.apartments ?: ""
    }

    val isFormValid = remember(
        isLoggedIn,
        guestName, guestLastName, guestEmail, guestPhone,
        city, street, house
    ) {
        if (isLoggedIn) {
            city.isNotBlank() && street.isNotBlank() && house.isNotBlank()
        } else {
            guestName.isNotBlank() &&
                    guestLastName.isNotBlank() &&
                    guestEmail.isNotBlank() &&
                    guestPhone.isNotBlank() &&
                    city.isNotBlank() &&
                    street.isNotBlank() &&
                    house.isNotBlank()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (checkoutState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF4F7F8)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. INFO FORM
                item {
                    if (isLoggedIn) {
                        ShippingInformationForm(
                            profile = checkoutState.profile,
                            onAddressSelected = onAddressSelected
                        )
                    } else {
                        AddressInputForm(
                            firstName = guestName, onFirstNameChange = { guestName = it },
                            lastName = guestLastName, onLastNameChange = { guestLastName = it },
                            email = guestEmail, onEmailChange = { guestEmail = it },
                            phone = guestPhone, onPhoneChange = { guestPhone = it },
                            city = city, onCityChange = { city = it },
                            street = street, onStreetChange = { street = it },
                            house = house, onHouseChange = { house = it },
                            apartment = apartment, onApartmentChange = { apartment = it }
                        )
                    }
                }

                // 2. DELIVERY TYPE
                item {
                    Text("Delivery Type", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    DeliveryOptions(
                        selected = selectedDelivery,
                        onSelect = { selectedDelivery = it },
                        options = listOf(DeliveryMethod.Courier, DeliveryMethod.Post, DeliveryMethod.Premium)
                    )
                }

                // 3. PAYMENT METHOD
                item {
                    Text("Payment Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    PaymentOptions(
                        selected = selectedPayment,
                        onSelect = { selectedPayment = it },
                        options = listOf(PaymentMethod.Card, PaymentMethod.CashAfterDelivery)
                    )
                }

                // 4. BUTTON
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (isFormValid) {
                                // [FIX] Маппінг UI моделей в Domain моделі
                                val domainDelivery = when (selectedDelivery) {
                                    DeliveryMethod.Courier -> DomainDeliveryMethod.Courier
                                    DeliveryMethod.Post -> DomainDeliveryMethod.Post
                                    DeliveryMethod.Premium -> DomainDeliveryMethod.Premium
                                    DeliveryMethod.Pickup -> DomainDeliveryMethod.Pickup
                                }

                                val domainPayment = when (selectedPayment) {
                                    PaymentMethod.Card -> DomainPaymentMethod.Card
                                    PaymentMethod.CashAfterDelivery -> DomainPaymentMethod.CashAfterDelivery
                                }

                                viewModel.placeOrder(
                                    guestName, guestLastName, guestEmail, guestPhone,
                                    city, street, house, apartment,
                                    domainDelivery, // Передаємо сконвертоване значення
                                    domainPayment   // Передаємо сконвертоване значення
                                )
                            }
                        },
                        enabled = isFormValid && !checkoutState.orderProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else Color.LightGray
                        )
                    ) {
                        if (checkoutState.orderProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Confirm Order", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- ФОРМА АВТОРИЗОВАНОГО КОРИСТУВАЧА ---
@Composable
fun ShippingInformationForm(
    profile: UserProfile?,
    onAddressSelected: (UserAddress) -> Unit
) {
    val userAddresses = profile?.addresses.orEmpty()
    var isExpanded by remember { mutableStateOf(false) }
    var selectedLabel by remember { mutableStateOf("Select address or enter below") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Contact Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InputDisplayCard("Name", "${profile?.firstName} ${profile?.lastName}", Modifier.weight(1f))
            InputDisplayCard("Phone", profile?.phoneNumber ?: "N/A", Modifier.weight(1f))
        }

        Text("Saved Addresses", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .clickable(onClick = { isExpanded = true })
                .background(Color(0xFFF0F0F0)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(selectedLabel, modifier = Modifier.padding(horizontal = 16.dp), color = Color.Black)
            Icon(Icons.Outlined.ArrowDropDown, null, Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                if (userAddresses.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No saved addresses", color = Color.Gray) },
                        onClick = { isExpanded = false }
                    )
                } else {
                    userAddresses.forEach { address ->
                        val label = "${address.city}, ${address.street} ${address.house}"
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedLabel = label
                                onAddressSelected(address)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- ФОРМА ГОСТЯ ---
@Composable
fun AddressInputForm(
    firstName: String, onFirstNameChange: (String) -> Unit,
    lastName: String, onLastNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    city: String, onCityChange: (String) -> Unit,
    street: String, onStreetChange: (String) -> Unit,
    house: String, onHouseChange: (String) -> Unit,
    apartment: String, onApartmentChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Guest Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = firstName, onValueChange = onFirstNameChange, label = { Text("First Name*") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = lastName, onValueChange = onLastNameChange, label = { Text("Last Name*") }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email*") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Phone*") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        }
        Text("Delivery Address", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = city, onValueChange = onCityChange, label = { Text("City*") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = street, onValueChange = onStreetChange, label = { Text("Street*") }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = house, onValueChange = onHouseChange, label = { Text("House*") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = apartment, onValueChange = onApartmentChange, label = { Text("Apt") }, modifier = Modifier.weight(1f))
        }
    }
}

// --- ДОПОМІЖНІ КОМПОНЕНТИ ---

@Composable
fun InputDisplayCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F0F0))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(value, color = Color.Black, maxLines = 1)
        }
    }
}

@Composable
fun <T : DeliveryMethod> DeliveryOptions(selected: T, onSelect: (T) -> Unit, options: List<T>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            SelectionCard(item.title, item.subtitle, item.icon, item == selected) { onSelect(item) }
        }
    }
}

@Composable
fun <T : PaymentMethod> PaymentOptions(selected: T, onSelect: (T) -> Unit, options: List<T>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            SelectionCard(item.title, item.subtitle, item.icon, item == selected) { onSelect(item) }
        }
    }
}

@Composable
fun SelectionCard(title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary.copy(0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}