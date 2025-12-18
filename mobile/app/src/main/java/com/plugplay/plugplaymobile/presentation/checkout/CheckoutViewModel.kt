package com.plugplay.plugplaymobile.presentation.checkout

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.data.model.LiqPayInitResponse
import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.usecase.ClearCartUseCase
import com.plugplay.plugplaymobile.domain.usecase.GetCartItemsUseCase
import com.plugplay.plugplaymobile.domain.usecase.PlaceOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutState(
    val cartItems: List<CartItem> = emptyList(),
    val availableAddresses: List<UserAddress> = emptyList(),
    val selectedAddress: UserAddress? = null,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CashAfterDelivery,
    val selectedDeliveryMethod: DeliveryMethod = DeliveryMethod.Courier,
    val isLoading: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val orderId: Int? = null,
    val error: String? = null,
    val liqPayData: LiqPayInitResponse? = null,
    val orderSuccess: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private val _currentUserId = mutableStateOf<Int?>(null)

    fun loadCart(userId: Int, addresses: List<UserAddress>) {
        if (_currentUserId.value == null) {
            _currentUserId.value = userId
        }
        _state.update { it.copy(availableAddresses = addresses, selectedAddress = addresses.firstOrNull(), isLoading = true) }

        viewModelScope.launch {
            getCartItemsUseCase(userId)
                .catch { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message) }
                }
                .collect { items ->
                    _state.update { it.copy(cartItems = items, isLoading = false) }
                }
        }
    }

    fun selectAddress(address: UserAddress) {
        _state.update { it.copy(selectedAddress = address) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _state.update { it.copy(selectedPaymentMethod = method) }
    }

    fun selectDeliveryMethod(method: DeliveryMethod) {
        _state.update { it.copy(selectedDeliveryMethod = method) }
    }

    fun resetOrderState() {
        _state.update { it.copy(orderSuccess = false, orderId = null, error = null) }
    }

    fun resetError() {
        _state.update { it.copy(error = null) }
    }

    fun placeOrder(
        guestName: String, guestLastName: String, guestEmail: String, guestPhone: String,
        city: String, street: String, house: String, apartment: String,
        deliveryMethod: DeliveryMethod, paymentMethod: PaymentMethod
    ) {
        placeOrder()
    }

    fun placeOrder() {
        val userId = _currentUserId.value
        val address = _state.value.selectedAddress
        val items = _state.value.cartItems
        val orderDescription = items.joinToString(", ") { "${it.name} x${it.quantity}" }
        val paymentMethod = _state.value.selectedPaymentMethod
        val deliveryMethod = _state.value.selectedDeliveryMethod

        if (userId == null || address == null || items.isEmpty()) {
            _state.update { it.copy(error = "Missing user, address, or cart items.") }
            return
        }

        _state.update { it.copy(isPlacingOrder = true, error = null) }

        viewModelScope.launch {
            placeOrderUseCase(
                userId = userId,
                address = address,
                cartItems = items,
                totalPrice = items.sumOf { it.total },
                paymentMethod = paymentMethod,
                deliveryMethod = deliveryMethod,
                customerName = "",
                customerEmail = "",
                customerPhone = "",
                description = orderDescription,
            ).onSuccess { result -> // [FIX] Тепер це PlaceOrderResult
                // Очищення кошика вже викликається в UseCase, але для UI оновлення можна залишити
                clearCartUseCase(userId)

                _state.update {
                    it.copy(
                        isPlacingOrder = false,
                        orderId = result.orderId, // [FIX] Беремо з об'єкта
                        orderSuccess = true,
                        liqPayData = result.paymentData, // [FIX] Зберігаємо дані LiqPay!
                        cartItems = emptyList()
                    )
                }
            }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isPlacingOrder = false,
                            error = throwable.message ?: "Failed to place order."
                        )
                    }
                }
        }
    }
}