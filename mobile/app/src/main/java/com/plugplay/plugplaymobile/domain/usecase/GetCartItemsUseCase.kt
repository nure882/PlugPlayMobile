package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartItemsUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(userId: Int?): Flow<List<CartItem>> {
        return repository.getCartItems(userId)
    }
}