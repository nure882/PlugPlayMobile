package com.plugplay.plugplaymobile.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.usecase.AddToCartUseCase
import com.plugplay.plugplaymobile.domain.usecase.ClearCartUseCase
import com.plugplay.plugplaymobile.domain.usecase.DeleteCartItemUseCase
import com.plugplay.plugplaymobile.domain.usecase.GetCartItemsUseCase
import com.plugplay.plugplaymobile.domain.usecase.UpdateCartItemQuantityUseCase
import com.plugplay.plugplaymobile.presentation.auth.AuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartState(
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val updateQuantityUseCase: UpdateCartItemQuantityUseCase,
    private val deleteCartItemUseCase: DeleteCartItemUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loadingState = MutableStateFlow(false)

    // Получаем ID пользователя из AuthRepository
    private val userIdFlow = authRepository.getUserId()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, null)

    // Комбинируем поток товаров в корзине с состоянием загрузки
    val state: StateFlow<CartState> = combine(
        userIdFlow,
        getCartItemsUseCase(null),
        _loadingState // <--- [ВИПРАВЛЕННЯ] Включаємо потік _loadingState сюди
    ) { userId, items, isMutating -> // <--- [ВИПРАВЛЕННЯ] Отримуємо нове значення isMutating
        val subtotal = items.sumOf { it.total }
        CartState(
            cartItems = items.sortedBy { it.id },
            subtotal = subtotal,
            isLoading = isMutating, // <--- [ВИПРАВЛЕННЯ] Використовуємо реактивне значення isMutating
            error = null
        )
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        CartState(isLoading = false)
    )

    fun addToCart(productId: String, quantity: Int) {
        viewModelScope.launch {
            _loadingState.update { true }
            addToCartUseCase(userIdFlow.value, productId, quantity)
                .onFailure { error ->
                    println("ERROR: Add to cart failed: ${error.message}")
                }
            _loadingState.update { false }
        }
    }

    fun updateQuantity(cartItemId: Long, newQuantity: Int) {
        if (newQuantity < 1) return
        viewModelScope.launch {
            _loadingState.update { true }
            updateQuantityUseCase(userIdFlow.value, cartItemId, newQuantity)
                .onFailure { error ->
                    println("ERROR: Update quantity failed: ${error.message}")
                }
            _loadingState.update { false }
        }
    }

    fun deleteItem(cartItemId: Long) {
        viewModelScope.launch {
            _loadingState.update { true }
            deleteCartItemUseCase(userIdFlow.value, cartItemId)
                .onFailure { error ->
                    println("ERROR: Delete item failed: ${error.message}")
                }
            _loadingState.update { false }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _loadingState.update { true }
            clearCartUseCase(userIdFlow.value)
                .onFailure { error ->
                    println("ERROR: Clear cart failed: ${error.message}")
                }
            _loadingState.update { false }
        }
    }
}