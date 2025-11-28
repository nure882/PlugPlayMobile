package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartItemQuantityUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(userId: Int?, cartItemId: Long, newQuantity: Int): Result<Unit> {
        return repository.updateQuantity(userId, cartItemId, newQuantity)
    }
}