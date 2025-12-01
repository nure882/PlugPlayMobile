package com.plugplay.plugplaymobile.presentation.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown // [ДОДАНО ІМПОРТ]
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.model.UserProfile
import kotlinx.coroutines.flow.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

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
                // Тут має бути виклик UseCase, який бере профіль,
                // але для імітації використовуємо заглушку, якщо profile null
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
    onOrderConfirmed: () -> Unit, // <--- ДОДАНО: новий колбек
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val checkoutState by viewModel.state.collectAsState()
    val isLoggedIn = checkoutState.isLoggedIn

    // Стан для обраних методів (перенесено всередину CheckoutScreen)
    var selectedDelivery by remember { mutableStateOf<DeliveryMethod>(DeliveryMethod.Courier) }
    var selectedPayment by remember { mutableStateOf<PaymentMethod>(PaymentMethod.Card) }

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
                // --- 0. SHIPPING INFORMATION / GUEST INFO (ГОЛОВНА ЛОГІКА) ---
                item {
                    if (isLoggedIn) {
                        ShippingInformationForm(checkoutState.profile)
                    } else {
                        // Якщо не авторизований - показуємо форму введення адреси для гостя
                        AddressInputForm()
                    }
                }

                // --- 1. DELIVERY TYPE SECTION ---
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

                // --- 2. PAYMENT METHOD SECTION ---
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

                // --- 3. CONFIRM BUTTON (Placeholder) ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        // [ОНОВЛЕНО] При натисканні імітуємо успішне оформлення замовлення та викликаємо навігацію
                        onClick = { onOrderConfirmed() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- ФОРМА АВТОРИЗОВАНОГО КОРИСТУВАЧА (ЗА СКРІНШОТОМ) ---
@Composable
fun ShippingInformationForm(profile: UserProfile?) {
    // Стан для Dropdown (вибору адреси)
    var isExpanded by remember { mutableStateOf(false) }
    // NOTE: Тут ми симулюємо, що адреси ще не налаштовані, тому адреса лише "Select address"
    val addresses = listOf("Select address")
    var selectedAddress by remember { mutableStateOf(addresses.first()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Shipping Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // --- 1. Name & Last Name ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InputDisplayCard(
                label = "First name",
                value = profile?.firstName ?: "skritiy po", // Mocked value
                modifier = Modifier.weight(1f)
            )
            InputDisplayCard(
                label = "Last name",
                value = profile?.lastName ?: "lol", // Mocked value
                modifier = Modifier.weight(1f)
            )
        }

        // --- 2. Email & Phone ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InputDisplayCard(
                label = "Email",
                value = profile?.email ?: "lolket@gmail.com", // Mocked value
                modifier = Modifier.weight(1f)
            )
            InputDisplayCard(
                label = "Phone number",
                value = profile?.phoneNumber ?: "deifn", // Mocked value
                modifier = Modifier.weight(1f)
            )
        }

        // --- 3. Address Dropdown (Вибір адреси) ---
        Text(
            "Address",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Використовуємо OutlinedTextField для імітації Dropdown, як на скріншоті.
        // NOTE: Це спрощена реалізація, яка відповідає візуальному макету image_6a164f.png
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .clickable { isExpanded = true }
                .background(Color(0xFFF0F0F0)), // Light Gray background for input fields
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                selectedAddress,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Black
            )
            Icon(
                Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
            )

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                addresses.forEach { address ->
                    DropdownMenuItem(
                        text = { Text(address) },
                        onClick = {
                            selectedAddress = address
                            isExpanded = false
                        }
                    )
                }
            }
        }
        // [ПОПЕРЕДЖЕННЯ ПРО АДРЕСУ]
        Text(
            "You have no addresses to select from. Please configure them in your profile.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Допоміжний компонент для відображення нередагованих полів
@Composable
fun InputDisplayCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F0F0)) // Light Gray background for input fields
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(value, color = Color.Black)
        }
    }
}


// --- ФОРМА ГОСТЯ (ІСНУЮЧА) ---
@Composable
fun AddressInputForm() {
    // Стан для полів
    val city = remember { mutableStateOf("") }
    val street = remember { mutableStateOf("") }
    val house = remember { mutableStateOf("") }
    val apartment = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Add New Address",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
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
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = street.value,
                onValueChange = { street.value = it },
                label = { Text("Street") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

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
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = apartment.value,
                onValueChange = { apartment.value = it },
                label = { Text("Apartment (optional)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Кнопка "Add Address"
        OutlinedButton(
            onClick = { /* TODO: Implement saving address */ },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Address", fontWeight = FontWeight.SemiBold)
        }
    }
}


// --- ІСНУЮЧІ ДОПОМІЖНІ КОМПОНЕНТИ ---

@Composable
fun <T : DeliveryMethod> DeliveryOptions(
    selected: T,
    onSelect: (T) -> Unit,
    options: List<T>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            DeliveryCard(
                delivery = item,
                isSelected = item == selected,
                onClick = { onSelect(item) }
            )
        }
    }
}

@Composable
fun <T : PaymentMethod> PaymentOptions(
    selected: T,
    onSelect: (T) -> Unit,
    options: List<T>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            PaymentCard(
                payment = item,
                isSelected = item == selected,
                onClick = { onSelect(item) }
            )
        }
    }
}


@Composable
fun DeliveryCard(
    delivery: DeliveryMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    SelectionCard(
        title = delivery.title,
        subtitle = delivery.subtitle,
        icon = delivery.icon,
        isSelected = isSelected,
        onClick = onClick
    )
}

@Composable
fun PaymentCard(
    payment: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // NOTE: Для методу оплати ми використовуємо іконку $ (AttachMoney) замість LocalShipping
    SelectionCard(
        title = payment.title,
        subtitle = payment.subtitle,
        icon = payment.icon,
        isSelected = isSelected,
        onClick = onClick
    )
}

@Composable
fun SelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Radio Button (Імітація)
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = Color.Gray
                    )
                )

                Spacer(Modifier.width(8.dp))

                // Content (Text)
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            // Icon (Right side, styled like the image)
            // Ми не можемо точно імітувати стилізовані іконки, тому використовуємо стандартні.
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}