package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.repository.CartRepository
import javax.inject.Inject

class DeleteCartItemUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(userId: Int?, cartItemId: Long): Result<Unit> {
        return repository.deleteCartItem(userId, cartItemId)
    }
}