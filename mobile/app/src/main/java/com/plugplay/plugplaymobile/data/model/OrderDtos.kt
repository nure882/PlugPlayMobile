package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.Order
import com.plugplay.plugplaymobile.domain.model.OrderItem
import com.plugplay.plugplaymobile.domain.model.OrderStatus
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.PaymentStatus

// Request DTOs
data class PlaceOrderRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("deliveryAddressId") val deliveryAddressId: Int,
    @SerializedName("deliveryMethod") val deliveryMethod: Int,
    @SerializedName("paymentMethod") val paymentMethod: Int,
    @SerializedName("orderItems") val orderItems: List<OrderItemRequestDto>
)

data class OrderItemRequestDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class PlaceOrderResponse(
    @SerializedName("orderId") val orderId: Int
)

// Response DTOs
data class OrderItemDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("productName") val productName: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("productImageUrl") val productImageUrl: String?,
)

data class OrderDto(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("orderDate") val orderDate: String,
    @SerializedName("status") val status: Int,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("deliveryMethod") val deliveryMethod: Int,
    @SerializedName("paymentMethod") val paymentMethod: Int,
    @SerializedName("deliveryAddressId") val deliveryAddressId: Int,

    // [ВАЖЛИВО] Переконуємось, що це поле є. Якщо JSON не містить його, буде 0 (Paid).
    @SerializedName("paymentStatus") val paymentStatus: Int,

    @SerializedName("orderItems") val orderItems: List<OrderItemDto>
)

// Mappers
fun OrderItemDto.toDomain(): OrderItem {
    return OrderItem(
        productId = this.productId.toString(),
        quantity = this.quantity,
        productName = this.productName ?: "Product #${this.productId}", // Fallback name
        price = this.price ?: 0.0,
        imageUrl = this.productImageUrl ?: "" // Empty string or placeholder url
    )
}

fun OrderDto.toDomain(): Order {
    // Дефолтні значення на випадок, якщо прийде невідомий ID enum-а
    val defaultStatus = OrderStatus.Created
    val defaultDelivery = DeliveryMethod.Courier
    val defaultPayment = PaymentMethod.Card
    val defaultPaymentStatus = PaymentStatus.NotPaid // Default to NotPaid for safety

    return Order(
        id = this.id,
        userId = this.userId,
        orderDate = this.orderDate,
        totalAmount = this.totalAmount,
        deliveryAddressId = this.deliveryAddressId,

        // Безпечний маппінг Enum-ів
        status = OrderStatus.entries.find { it.id == this.status } ?: defaultStatus,
        deliveryMethod = DeliveryMethod.entries.find { it.id == this.deliveryMethod } ?: defaultDelivery,
        paymentMethod = PaymentMethod.entries.find { it.id == this.paymentMethod } ?: defaultPayment,
        paymentStatus = PaymentStatus.entries.find { it.id == this.paymentStatus } ?: defaultPaymentStatus,

        orderItems = this.orderItems.map { it.toDomain() }
    )
}