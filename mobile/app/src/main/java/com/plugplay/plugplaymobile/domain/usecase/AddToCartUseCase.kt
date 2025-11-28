package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.repository.CartRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(userId: Int?, productId: String, quantity: Int): Result<Unit> {
        return repository.addToCart(userId, productId, quantity)
    }
}