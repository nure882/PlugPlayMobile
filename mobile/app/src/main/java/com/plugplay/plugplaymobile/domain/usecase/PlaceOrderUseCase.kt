package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.PlaceOrderResult // Додано
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.repository.OrderRepository
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val repository: OrderRepository,
    private val clearCartUseCase: ClearCartUseCase
) {
    suspend operator fun invoke(
        userId: Int?,
        cartItems: List<CartItem>,
        totalPrice: Double,
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod,
        address: UserAddress,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        description: String,
    ): Result<PlaceOrderResult> { // [FIX] Повертаємо PlaceOrderResult
        return repository.placeOrder(
            userId, cartItems, totalPrice, deliveryMethod, paymentMethod, address, customerName, customerEmail, customerPhone, description
        ).onSuccess {
            clearCartUseCase(userId)
        }
    }
}