package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.OrderRepository
import com.plugplay.plugplaymobile.util.LiqPayHelper
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class InitPaymentUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(orderId: Int): Result<String> {
        return try {
            // 1. Отримуємо ID користувача
            val userId = authRepository.getUserId().first()
                ?: throw Exception("User not logged in")

            // 2. Отримуємо список замовлень, щоб знайти суму конкретного замовлення
            // (Оскільки на бекенді немає методу GetOrderById, беремо зі списку)
            val ordersResult = orderRepository.getUserOrders(userId)
            val orders = ordersResult.getOrThrow()

            val order = orders.find { it.id == orderId }
                ?: throw Exception("Order #$orderId not found")

            // 3. Генеруємо посилання локально
            val url = LiqPayHelper.generatePaymentLink(order.id, order.totalAmount)

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}