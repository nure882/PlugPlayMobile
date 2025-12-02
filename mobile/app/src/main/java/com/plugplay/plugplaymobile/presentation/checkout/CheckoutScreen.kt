package com.plugplay.plugplaymobile.presentation.checkout

import androidx.compose.foundation.BorderStroke
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// --- СТАН І VIEWMODEL ---

data class CheckoutState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val profile: UserProfile? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val state: StateFlow<CheckoutState> = authRepository.getAuthStatus()
        .map { isLoggedIn ->
            val profile = if (isLoggedIn) {
                authRepository.getProfile().getOrNull()
            } else {
                null
            }
            CheckoutState(
                isLoggedIn = isLoggedIn,
                isLoading = false,
                profile = profile
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CheckoutState(isLoading = true)
        )
}

// --- МОДЕЛІ-ЗАГЛУШКИ ДЛЯ ДОСТАВКИ/ОПЛАТИ ---
sealed class DeliveryMethod(val title: String, val subtitle: String, val icon: ImageVector) {
    object Courier : DeliveryMethod("Courier", "Delivery 1-2 days", Icons.Outlined.LocalShipping)
    object Post : DeliveryMethod("Post", "Delivery 3-5 days", Icons.Outlined.Inventory)
    object Premium : DeliveryMethod("Premium Delivery", "Same day delivery", Icons.Outlined.FlashOn)
    object Pickup : DeliveryMethod("Pickup", "Collect from store", Icons.Outlined.Store)
}

sealed class PaymentMethod(val title: String, val subtitle: String, val icon: ImageVector) {
    object Card : PaymentMethod("Card", "Pay online with card", Icons.Outlined.CreditCard)
    object CashAfterDelivery : PaymentMethod("Cash after delivery", "Pay when you receive", Icons.Outlined.AttachMoney)
}

// --- ОСНОВНИЙ ЕКРАН ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderConfirmed: () -> Unit, // Цей колбек має відкривати OrderConfirmationScreen
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val checkoutState by viewModel.state.collectAsState()
    val isLoggedIn = checkoutState.isLoggedIn

    // --- STATE HOISTING (Стан полів винесено сюди для валідації) ---
    // Поля гостя
    var guestName by remember { mutableStateOf("") }
    var guestLastName by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }

    // Поля адреси (спільні для логіки)
    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }

    // Стан для вибору адреси (якщо залогінений)
    var selectedProfileAddress by remember { mutableStateOf("") }

    // Стан методів доставки/оплати
    var selectedDelivery by remember { mutableStateOf<DeliveryMethod>(DeliveryMethod.Courier) }
    var selectedPayment by remember { mutableStateOf<PaymentMethod>(PaymentMethod.Card) }

    // --- ЛОГІКА ВАЛІДАЦІЇ ---
    val isFormValid = remember(
        isLoggedIn,
        guestName, guestLastName, guestEmail, guestPhone,
        city, street, house,
        selectedProfileAddress
    ) {
        if (isLoggedIn) {
            // Якщо залогінений - перевіряємо, чи обрана адреса зі списку
            // (або додайте логіку, якщо user додає нову адресу)
            selectedProfileAddress.isNotBlank() && selectedProfileAddress != "Select address"
        } else {
            // Якщо гість - перевіряємо всі текстові поля
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
                // --- 0. INFO & ADDRESS FORM ---
                item {
                    if (isLoggedIn) {
                        ShippingInformationForm(
                            profile = checkoutState.profile,
                            onAddressSelected = { selectedProfileAddress = it }
                        )
                    } else {
                        // Передаємо змінні стану та callback-и для їх зміни
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

                // --- 1. DELIVERY TYPE ---
                item {
                    Text(
                        "Delivery Type",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DeliveryOptions(
                        selected = selectedDelivery,
                        onSelect = { selectedDelivery = it },
                        options = listOf(DeliveryMethod.Courier, DeliveryMethod.Post, DeliveryMethod.Premium)
                    )
                }

                // --- 2. PAYMENT METHOD ---
                item {
                    Text(
                        "Payment Method",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    PaymentOptions(
                        selected = selectedPayment,
                        onSelect = { selectedPayment = it },
                        options = listOf(PaymentMethod.Card, PaymentMethod.CashAfterDelivery)
                    )
                }

                // --- 3. CONFIRM BUTTON ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onOrderConfirmed() // Навігація на OrderConfirmationScreen
                            }
                        },
                        // Кнопка активна (enabled) тільки якщо форма валідна
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            // Синій, коли активно, сірий, коли ні
                            containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else Color.LightGray,
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text("Confirm Order", fontWeight = FontWeight.Bold)
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
    onAddressSelected: (String) -> Unit // Callback для передачі обраної адреси наверх
) {
    val addresses = remember(profile?.addresses) {
        profile?.addresses.orEmpty()
            .filter { it.street.isNotBlank() && it.city.isNotBlank() }
            .map { "${it.street}, ${it.house}, ${it.city}" }
    }

    val addressOptions = remember(addresses) {
        if (addresses.isEmpty()) listOf("Select address") else addresses
    }

    var isExpanded by remember { mutableStateOf(false) }
    var selectedAddress by remember { mutableStateOf(addressOptions.first()) }

    // Ініціалізуємо значення при першому запуску
    LaunchedEffect(selectedAddress) {
        onAddressSelected(selectedAddress)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Shipping Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InputDisplayCard("First name", profile?.firstName ?: "N/A", Modifier.weight(1f))
            InputDisplayCard("Last name", profile?.lastName ?: "N/A", Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InputDisplayCard("Email", profile?.email ?: "N/A", Modifier.weight(1f))
            InputDisplayCard("Phone number", profile?.phoneNumber ?: "N/A", Modifier.weight(1f))
        }

        Text("Address", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

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
            Text(selectedAddress, modifier = Modifier.padding(horizontal = 16.dp), color = Color.Black)
            Icon(Icons.Outlined.ArrowDropDown, null, Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                addressOptions.forEach { address ->
                    DropdownMenuItem(
                        text = { Text(address) },
                        onClick = {
                            selectedAddress = address
                            onAddressSelected(address) // Оновлюємо стан в CheckoutScreen
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}

// --- ФОРМА ГОСТЯ (ОНОВЛЕНА) ---
@Composable
fun AddressInputForm(
    // Вхідні параметри для State Hoisting
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
        Text(
            "Guest and Shipping Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. Ім'я та Прізвище
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = firstName, onValueChange = onFirstNameChange,
                label = { Text("First Name*") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = lastName, onValueChange = onLastNameChange,
                label = { Text("Last Name*") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 2. Email та Телефон
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                label = { Text("Email*") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = phone, onValueChange = onPhoneChange,
                label = { Text("Phone*") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Delivery Address", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        // 3. Місто та Вулиця
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = city, onValueChange = onCityChange,
                label = { Text("City*") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = street, onValueChange = onStreetChange,
                label = { Text("Street*") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 4. Будинок та Квартира
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = house, onValueChange = onHouseChange,
                label = { Text("House*") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = apartment, onValueChange = onApartmentChange,
                label = { Text("Apartment") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// --- ДОПОМІЖНІ КОМПОНЕНТИ (Без змін) ---

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
            DeliveryCard(item, item == selected) { onSelect(item) }
        }
    }
}

@Composable
fun <T : PaymentMethod> PaymentOptions(selected: T, onSelect: (T) -> Unit, options: List<T>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            PaymentCard(item, item == selected) { onSelect(item) }
        }
    }
}

@Composable
fun DeliveryCard(delivery: DeliveryMethod, isSelected: Boolean, onClick: () -> Unit) {
    SelectionCard(delivery.title, delivery.subtitle, delivery.icon, isSelected, onClick)
}

@Composable
fun PaymentCard(payment: PaymentMethod, isSelected: Boolean, onClick: () -> Unit) {
    SelectionCard(payment.title, payment.subtitle, payment.icon, isSelected, onClick)
}

@Composable
fun SelectionCard(title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().border(1.5.dp, borderColor, RoundedCornerShape(12.dp)).clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(0.7f), modifier = Modifier.size(24.dp))
        }
    }
}