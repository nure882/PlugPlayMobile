package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.repository.OrderRepository
import javax.inject.Inject

class CancelOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: Int): Result<Unit> {
        return repository.cancelOrder(orderId)
    }
}