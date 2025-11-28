package com.plugplay.plugplaymobile.data.local

import com.plugplay.plugplaymobile.domain.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartLocalDataSource @Inject constructor() {

    // Имитация локального хранилища для корзины гостя
    private val _guestCart = MutableStateFlow<List<CartItem>>(emptyList())
    val guestCart: Flow<List<CartItem>> = _guestCart.asStateFlow()

    // Имитация генерации ID для CartItem (как Date.now() на фронтенде)
    private var nextCartId: Long = 1L

    // Повертає поточний список (для MockCartRepositoryImpl)
    val value: List<CartItem>
        get() = _guestCart.value

    suspend fun getNextId(): Long {
        delay(1) // Имитация задержки
        return nextCartId++
    }

    suspend fun saveGuestCart(cartItems: List<CartItem>) {
        delay(50) // Имитация записи
        _guestCart.value = cartItems
        println("MOCK DS: Guest cart saved with ${cartItems.size} items.")
    }

    suspend fun clearGuestCart() {
        delay(50) // Имитация очистки
        _guestCart.value = emptyList()
        nextCartId = 1L
        println("MOCK DS: Guest cart cleared.")
    }
}