package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.Order
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.PlaceOrderResult // Додано
import com.plugplay.plugplaymobile.domain.model.UserAddress

interface OrderRepository {
    suspend fun placeOrder(
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
    ): Result<PlaceOrderResult> // [FIX] Було Result<Int>

    suspend fun getUserOrders(userId: Int): Result<List<Order>>
    suspend fun cancelOrder(orderId: Int): Result<Unit>
}