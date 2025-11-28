package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {

    fun getCartItems(userId: Int?): Flow<List<CartItem>>

    // itemId is the product ID
    suspend fun addToCart(userId: Int?, productId: String, quantity: Int): Result<Unit>

    // cartItemId is the ID of the cart entry (from local or remote storage)
    suspend fun updateQuantity(userId: Int?, cartItemId: Long, newQuantity: Int): Result<Unit>

    // cartItemId is the ID of the cart entry
    suspend fun deleteCartItem(userId: Int?, cartItemId: Long): Result<Unit>

    suspend fun clearCart(userId: Int?): Result<Unit>
}