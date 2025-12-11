package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetWishlistUseCase @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<List<Product>> {
        val userId = authRepository.getUserId().first()
            ?: return Result.failure(Exception("User not logged in"))
        return wishlistRepository.getWishlist(userId)
    }
}