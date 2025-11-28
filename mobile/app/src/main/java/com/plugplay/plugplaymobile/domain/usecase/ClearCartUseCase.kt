package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.repository.CartRepository
import javax.inject.Inject

class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(userId: Int?): Result<Unit> {
        return repository.clearCart(userId)
    }
}