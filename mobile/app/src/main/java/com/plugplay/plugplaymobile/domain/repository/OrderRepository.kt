package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.model.Order

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
        customerPhone: String
    ): Result<Int>

    suspend fun getUserOrders(userId: Int): Result<List<Order>>
    suspend fun cancelOrder(orderId: Int): Result<Unit>
}