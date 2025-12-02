package com.plugplay.plugplaymobile.presentation.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.usecase.PlaceOrderUseCase
import com.plugplay.plugplaymobile.domain.usecase.GetCartItemsUseCase
import com.plugplay.plugplaymobile.presentation.cart.CartViewModel
import kotlinx.coroutines.flow.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.util.Locale

// --- СТАН І VIEWMODEL ---

data class CheckoutState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val isPlacingOrder: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null
)

data class DeliveryOption(val method: DeliveryMethod, val title: String, val subtitle: String, val icon: ImageVector)
data class PaymentOption(val method: PaymentMethod, val title: String, val subtitle: String, val icon: ImageVector)

data class CustomerOrderData(
    val address: UserAddress,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getCartItemsUseCase: GetCartItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutState(isLoading = true))
    private val _isPlacingOrder = MutableStateFlow(false)

    val state: StateFlow<CheckoutState> = combine(
        authRepository.getAuthStatus(),
        _isPlacingOrder,
        _uiState
    ) { isLoggedIn, isPlacing, uiState ->
        val profile = if (isLoggedIn && uiState.profile == null) {
            authRepository.getProfile().getOrNull()
        } else {
            uiState.profile
        }

        uiState.copy(
            isLoggedIn = isLoggedIn,
            isLoading = false,
            isPlacingOrder = isPlacing,
            profile = profile
        )
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CheckoutState(isLoading = true)
        )


    fun loadProfileIfNecessary() {
        if (state.value.isLoggedIn && state.value.profile == null) {
            viewModelScope.launch {
                authRepository.getProfile()
                    .onSuccess { profile ->
                        _uiState.update { it.copy(profile = profile) }
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
        }
    }

    // Оновлена сигнатура: тепер передаємо функцію, яка має викликатися після розміщення замовлення
    fun placeOrder(
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod,
        address: UserAddress,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        onOrderPlacedSuccessfully: (PaymentMethod) -> Unit
    ) {
        if (_isPlacingOrder.value) return

        _isPlacingOrder.update { true }
        _uiState.update { it.copy(error = null) }

        viewModelScope.launch {
            val userId = authRepository.getUserId().first()
            val cartItems = getCartItemsUseCase(userId).first()
            val totalPrice = cartItems.sumOf { it.total }

            if (cartItems.isEmpty()) {
                _uiState.update { it.copy(error = "Cart is empty.") }
                _isPlacingOrder.update { false }
                return@launch
            }

            placeOrderUseCase(
                userId = userId,
                cartItems = cartItems,
                totalPrice = totalPrice,
                deliveryMethod = deliveryMethod,
                paymentMethod = paymentMethod,
                address = address,
                customerName = customerName,
                customerEmail = customerEmail,
                customerPhone = customerPhone
            )
                .onSuccess { orderId ->
                    _isPlacingOrder.update { false }
                    // ВИКЛИК КОЛБЕКУ З МЕТОДОМ ОПЛАТИ
                    onOrderPlacedSuccessfully(paymentMethod)
                }
                .onFailure { error ->
                    _isPlacingOrder.update { false }
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}


// Delivery Options
val deliveryOptions = listOf(
    DeliveryOption(DeliveryMethod.Courier, "Courier", "Delivery 1-2 days", Icons.Outlined.LocalShipping),
    DeliveryOption(DeliveryMethod.Post, "Post", "Delivery 3-5 days", Icons.Outlined.Inventory),
    DeliveryOption(DeliveryMethod.Premium, "Premium Delivery", "Same day delivery", Icons.Outlined.FlashOn),
    DeliveryOption(DeliveryMethod.Pickup, "Pickup", "Collect from store", Icons.Outlined.Store)
)

// Payment Options (GooglePay mock added)
val paymentOptions = listOf(
    PaymentOption(PaymentMethod.Card, "Card", "Pay online with card", Icons.Outlined.CreditCard),
    PaymentOption(PaymentMethod.GooglePay, "Google Pay (Test)", "Secure payment via Google", Icons.Outlined.Paid),
    PaymentOption(PaymentMethod.CashAfterDelivery, "Cash after delivery", "Pay when you receive", Icons.Outlined.AttachMoney)
)

// Helper function
fun Double.formatPrice(): String {
    val format = NumberFormat.getNumberInstance(Locale("uk", "UA")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return format.format(this)
}

// NEW OrderTotalCard
@Composable
fun OrderTotalCard(subtotal: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal:", color = Color.Gray)
                Text(subtotal.formatPrice() + " ₴")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Delivery:", color = Color.Gray)
                Text("0.00 ₴")
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", fontWeight = FontWeight.Bold)
                Text(subtotal.formatPrice() + " ₴", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// NEW Payment Simulation Dialog
@Composable
fun PaymentSimulationDialog(
    isOpen: Boolean,
    onPaymentSuccess: () -> Unit,
    onClose: () -> Unit,
    total: Double,
    paymentMethod: PaymentMethod
) {
    if (!isOpen) return

    var isLoading by remember { mutableStateOf(true) }
    var paymentSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        isLoading = false
        // Імітуємо успіх оплати
        paymentSuccess = true
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Connecting to Payment Gateway...", style = MaterialTheme.typography.titleMedium)
                } else if (paymentSuccess) {
                    Icon(
                        Icons.Outlined.CheckCircleOutline,
                        contentDescription = "Success",
                        tint = Color.Green,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("Payment Successful!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Amount paid: ${total.formatPrice()} ₴ via ${paymentMethod.name}", color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onPaymentSuccess,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Continue to Order Confirmation")
                    }
                } else {
                    // Це не повинно відбутися в симуляції, але як заглушка
                    Text("Payment failed.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderConfirmed: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val checkoutState by viewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val isLoggedIn = checkoutState.isLoggedIn

    var selectedDelivery by remember { mutableStateOf<DeliveryMethod>(DeliveryMethod.Courier) }
    var selectedPayment by remember { mutableStateOf<PaymentMethod>(PaymentMethod.Card) }

    // Стан для модального вікна оплати
    var showPaymentModal by remember { mutableStateOf(false) }
    // Зберігаємо обраний метод для передачі в модальне вікно
    var paymentMethodForModal by remember { mutableStateOf<PaymentMethod?>(null) }


    val guestFirstName = remember { mutableStateOf("") }
    val guestLastName = remember { mutableStateOf("") }
    val guestPhone = remember { mutableStateOf("") }
    val guestEmail = remember { mutableStateOf("") }
    val guestCity = remember { mutableStateOf("") }
    val guestStreet = remember { mutableStateOf("") }
    val guestHouse = remember { mutableStateOf("") }
    val guestApartment = remember { mutableStateOf("") }

    val selectedAddressFromProfile = remember { mutableStateOf<UserAddress?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProfileIfNecessary()
    }

    LaunchedEffect(checkoutState.profile) {
        val addresses = checkoutState.profile?.addresses.orEmpty().filter { it.street.isNotBlank() && it.city.isNotBlank() }
        selectedAddressFromProfile.value = addresses.firstOrNull()
    }


    val canPlaceOrder = remember(checkoutState.isPlacingOrder, cartState.cartItems, isLoggedIn, selectedAddressFromProfile.value, guestFirstName.value, guestCity.value) {
        if (checkoutState.isPlacingOrder || cartState.cartItems.isEmpty()) return@remember false

        if (isLoggedIn) {
            selectedAddressFromProfile.value != null
        } else {
            guestFirstName.value.isNotBlank() && guestLastName.value.isNotBlank() &&
                    guestPhone.value.isNotBlank() && guestEmail.value.isNotBlank() &&
                    guestCity.value.isNotBlank() && guestStreet.value.isNotBlank() &&
                    guestHouse.value.isNotBlank()
        }
    }

    val customerData = remember(isLoggedIn, checkoutState.profile, selectedAddressFromProfile.value, guestFirstName.value, guestCity.value, guestPhone.value, guestEmail.value) {
        if (isLoggedIn) {
            val profile = checkoutState.profile
            val address = selectedAddressFromProfile.value
            if (profile != null && address != null) {
                CustomerOrderData(
                    address = address,
                    customerName = "${profile.firstName} ${profile.lastName}",
                    customerEmail = profile.email,
                    customerPhone = profile.phoneNumber
                )
            } else null
        } else {
            if (guestCity.value.isNotBlank() && guestStreet.value.isNotBlank() && guestHouse.value.isNotBlank()) {
                CustomerOrderData(
                    address = UserAddress(
                        id = null,
                        city = guestCity.value,
                        street = guestStreet.value,
                        house = guestHouse.value,
                        apartments = guestApartment.value.ifBlank { null }
                    ),
                    customerName = "${guestFirstName.value} ${guestLastName.value}",
                    customerEmail = guestEmail.value,
                    customerPhone = guestPhone.value
                )
            } else null
        }
    }

    // Діалог симуляції оплати
    paymentMethodForModal?.let { method ->
        PaymentSimulationDialog(
            isOpen = showPaymentModal,
            onPaymentSuccess = {
                showPaymentModal = false
                onOrderConfirmed()
            },
            onClose = {
                // Якщо користувач закрив діалог, він, ймовірно, скасував оплату.
                showPaymentModal = false
            },
            total = cartState.subtotal,
            paymentMethod = method
        )
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
                item {
                    if (isLoggedIn) {
                        ShippingInformationForm(
                            profile = checkoutState.profile,
                            selectedAddress = selectedAddressFromProfile.value,
                            onAddressSelect = { selectedAddressFromProfile.value = it }
                        )
                    } else {
                        AddressInputForm(
                            firstName = guestFirstName,
                            lastName = guestLastName,
                            phone = guestPhone,
                            email = guestEmail,
                            city = guestCity,
                            street = guestStreet,
                            house = guestHouse,
                            apartment = guestApartment
                        )
                    }
                }

                item {
                    Text(
                        "Delivery Type",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DeliveryOptions(
                        selected = selectedDelivery,
                        onSelect = { selectedDelivery = it.method },
                        options = deliveryOptions
                    )
                }

                item {
                    Text(
                        "Payment Method",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                    PaymentOptions(
                        selected = selectedPayment,
                        onSelect = { selectedPayment = it.method },
                        options = paymentOptions
                    )
                }

                item {
                    OrderTotalCard(cartState.subtotal)
                }

                item {
                    if (checkoutState.error != null) {
                        Text(checkoutState.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            customerData?.let { data ->
                                paymentMethodForModal = selectedPayment
                                viewModel.placeOrder(
                                    deliveryMethod = selectedDelivery,
                                    paymentMethod = selectedPayment,
                                    address = data.address,
                                    customerName = data.customerName,
                                    customerEmail = data.customerEmail,
                                    customerPhone = data.customerPhone,
                                    onOrderPlacedSuccessfully = { method ->
                                        when (method) {
                                            PaymentMethod.CashAfterDelivery -> onOrderConfirmed()
                                            PaymentMethod.Card, PaymentMethod.GooglePay -> showPaymentModal = true
                                            else -> onOrderConfirmed()
                                        }
                                    }
                                )
                            }
                        },
                        enabled = canPlaceOrder,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (checkoutState.isPlacingOrder) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Confirm Order (${cartState.subtotal.formatPrice()} ₴)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- ФОРМА АВТОРИЗОВАНОГО КОРИСТУВАЧА ---
@Composable
fun ShippingInformationForm(profile: UserProfile?, selectedAddress: UserAddress?, onAddressSelect: (UserAddress) -> Unit) {

    val addresses = remember(profile?.addresses) {
        profile?.addresses.orEmpty()
            .filter { it.street.isNotBlank() && it.city.isNotBlank() }
    }

    val addressOptions = remember(addresses) {
        addresses.map { address ->
            Pair(
                address,
                "${address.street}, ${address.house}${if (address.apartments.isNullOrBlank()) "" else ", apt ${address.apartments}"}, ${address.city}"
            )
        }
    }

    val selectedAddressPair = remember(selectedAddress) {
        addressOptions.find { it.first == selectedAddress } ?: addressOptions.firstOrNull()
    }

    var isExpanded by remember { mutableStateOf(false) }

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InputDisplayCard(
                label = "First name",
                value = profile?.firstName ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            InputDisplayCard(
                label = "Last name",
                value = profile?.lastName ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InputDisplayCard(
                label = "Email",
                value = profile?.email ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            InputDisplayCard(
                label = "Phone number",
                value = profile?.phoneNumber ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            "Address",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

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
            Text(
                selectedAddressPair?.second ?: "Select address or add one in profile",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = if (selectedAddressPair != null) Color.Black else Color.Gray
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
                addressOptions.forEach { (address, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onAddressSelect(address)
                            isExpanded = false
                        }
                    )
                }
            }
        }
        if (addresses.isEmpty()) {
            Text(
                "You have no addresses to select from. Please configure them in your profile.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

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
                .background(Color(0xFFF0F0F0))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(value, color = Color.Black)
        }
    }
}


// --- ФОРМА ГОСТЯ ---
@Composable
fun AddressInputForm(
    firstName: MutableState<String>,
    lastName: MutableState<String>,
    phone: MutableState<String>,
    email: MutableState<String>,
    city: MutableState<String>,
    street: MutableState<String>,
    house: MutableState<String>,
    apartment: MutableState<String>
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

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = firstName.value, onValueChange = { firstName.value = it }, label = { Text("First Name*") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = lastName.value, onValueChange = { lastName.value = it }, label = { Text("Last Name*") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email*") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            OutlinedTextField(
                value = phone.value,
                onValueChange = { phone.value = it.filter { c -> c.isDigit() || c == '+' } },
                label = { Text("Phone*") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = city.value, onValueChange = { city.value = it }, label = { Text("City*") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = street.value, onValueChange = { street.value = it }, label = { Text("Street*") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = house.value, onValueChange = { house.value = it }, label = { Text("House*") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = apartment.value, onValueChange = { apartment.value = it }, label = { Text("Apartment (optional)") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }
    }
}


// --- ДОПОМІЖНІ КОМПОНЕНТИ ---

@Composable
fun DeliveryOptions(
    selected: DeliveryMethod,
    onSelect: (DeliveryOption) -> Unit,
    options: List<DeliveryOption>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            SelectionCard(
                title = item.title,
                subtitle = item.subtitle,
                icon = item.icon,
                isSelected = item.method == selected,
                onClick = { onSelect(item) }
            )
        }
    }
}

@Composable
fun PaymentOptions(
    selected: PaymentMethod,
    onSelect: (PaymentOption) -> Unit,
    options: List<PaymentOption>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { item ->
            SelectionCard(
                title = item.title,
                subtitle = item.subtitle,
                icon = item.icon,
                isSelected = item.method == selected,
                onClick = { onSelect(item) }
            )
        }
    }
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
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = Color.Gray
                    )
                )

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}