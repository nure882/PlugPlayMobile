package com.plugplay.plugplaymobile.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.usecase.GetCartItemsUseCase
import com.plugplay.plugplaymobile.domain.usecase.PlaceOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod as DomainDeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod as DomainPaymentMethod

data class CheckoutState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val cartItems: List<CartItem> = emptyList(),
    val orderProcessing: Boolean = false,
    val orderError: String? = null,
    val orderSuccess: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getCartItemsUseCase: GetCartItemsUseCase
) : ViewModel() {

    private val _orderProcessingState = MutableStateFlow(false)
    private val _orderErrorState = MutableStateFlow<String?>(null)
    private val _orderSuccessState = MutableStateFlow(false)

    val state: StateFlow<CheckoutState> = combine(
        authRepository.getAuthStatus(),
        getCartItemsUseCase(null),
        _orderProcessingState,
        _orderErrorState,
        _orderSuccessState
    ) { isLoggedIn, cartItems, isProcessing, error, isSuccess ->
        val profile = if (isLoggedIn) {
            authRepository.getProfile().getOrNull()
        } else {
            null
        }
        CheckoutState(
            isLoggedIn = isLoggedIn,
            isLoading = false,
            profile = profile,
            cartItems = cartItems,
            orderProcessing = isProcessing,
            orderError = error,
            orderSuccess = isSuccess
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CheckoutState(isLoading = true)
    )

    fun placeOrder(
        guestName: String,
        guestLastName: String,
        guestEmail: String,
        guestPhone: String,
        city: String,
        street: String,
        house: String,
        apartment: String?,
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod
    ) {
        val currentState = state.value

        if (!currentState.isLoggedIn) {
            _orderErrorState.value = "You must be logged in to place an order."
            return
        }

        viewModelScope.launch {
            _orderProcessingState.value = true
            _orderErrorState.value = null

            try {
                val userId = authRepository.getUserId().first()
                val totalPrice = currentState.cartItems.sumOf { it.total }

                // [ВАЖНО] Пытаемся найти ID выбранного адреса в списке профиля
                // Сравниваем введенные данные с сохраненными адресами
                val savedAddress = currentState.profile?.addresses?.find {
                    it.city.equals(city, ignoreCase = true) &&
                            it.street.equals(street, ignoreCase = true) &&
                            it.house.equals(house, ignoreCase = true)
                }

                // Если адрес не найден среди сохраненных, у него не будет ID, и репозиторий выдаст ошибку.
                // В реальном приложении тут нужно вызывать createAddress, но пока используем то, что есть.
                val addressToSend = savedAddress ?: UserAddress(
                    id = null, // Это вызовет ошибку в репозитории, но это честно
                    city = city, street = street, house = house, apartments = apartment
                )

                // ... остальной код (placeOrderUseCase) ...
                placeOrderUseCase(
                    userId = userId,
                    cartItems = currentState.cartItems,
                    totalPrice = totalPrice,
                    deliveryMethod = when(deliveryMethod) {
                        DeliveryMethod.Courier -> DomainDeliveryMethod.Courier
                        DeliveryMethod.Post -> DomainDeliveryMethod.Post
                        DeliveryMethod.Premium -> DomainDeliveryMethod.Premium
                        DeliveryMethod.Pickup -> DomainDeliveryMethod.Pickup
                    },
                    paymentMethod = when(paymentMethod) {
                        PaymentMethod.Card -> DomainPaymentMethod.Card
                        PaymentMethod.CashAfterDelivery -> DomainPaymentMethod.CashAfterDelivery
                    },
                    address = addressToSend, // Передаем найденный адрес с ID
                    customerName = "${currentState.profile?.firstName} ${currentState.profile?.lastName}",
                    customerEmail = currentState.profile?.email ?: "",
                    customerPhone = currentState.profile?.phoneNumber ?: ""
                ).onSuccess {
                    _orderProcessingState.value = false
                    _orderSuccessState.value = true
                }.onFailure { e ->
                    _orderProcessingState.value = false
                    _orderErrorState.value = e.message
                }
            } catch (e: Exception) {
                _orderProcessingState.value = false
                _orderErrorState.value = e.message
            }
        }
    }

    fun resetOrderState() {
        _orderSuccessState.value = false
        _orderErrorState.value = null
    }
}