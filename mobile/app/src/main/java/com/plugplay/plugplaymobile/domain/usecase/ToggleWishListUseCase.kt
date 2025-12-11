package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ToggleWishlistUseCase @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val authRepository: AuthRepository
) {
    suspend fun add(productId: Int): Result<Unit> {
        val userId = authRepository.getUserId().first()
            ?: return Result.failure(Exception("User not logged in"))
        return wishlistRepository.addToWishlist(userId, productId)
    }

    suspend fun remove(productId: Int): Result<Unit> {
        val userId = authRepository.getUserId().first()
            ?: return Result.failure(Exception("User not logged in"))
        return wishlistRepository.removeFromWishlist(userId, productId)
    }
}