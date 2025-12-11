package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.Order
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.OrderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetUserOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<List<Order>> {
        val userId = authRepository.getUserId().first()

        return if (userId != null) {
            orderRepository.getUserOrders(userId)
        } else {
            Result.failure(Exception("User is not logged in"))
        }
    }
}