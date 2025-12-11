package com.plugplay.plugplaymobile.domain.model

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val productName: String,
    val price: Double,
    val imageUrl: String
)

data class Order(
    val id: Int,
    val userId: Int,
    val orderDate: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val deliveryMethod: DeliveryMethod,
    val paymentMethod: PaymentMethod,
    val deliveryAddressId: Int,
    val paymentStatus: PaymentStatus,
    val orderItems: List<OrderItem>
)