package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.Product

interface WishlistRepository {
    suspend fun getWishlist(userId: Int): Result<List<Product>>
    suspend fun addToWishlist(userId: Int, productId: Int): Result<Unit>
    suspend fun removeFromWishlist(userId: Int, productId: Int): Result<Unit>
}