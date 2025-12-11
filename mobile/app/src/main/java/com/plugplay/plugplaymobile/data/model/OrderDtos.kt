package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.Order
import com.plugplay.plugplaymobile.domain.model.OrderItem
import com.plugplay.plugplaymobile.domain.model.OrderStatus
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.PaymentStatus

// [ИСПРАВЛЕНО] Структура запроса теперь совпадает с Frontend (src/api/orderApi.ts)
data class PlaceOrderRequest(
    @SerializedName("userId")
    val userId: Int,

    @SerializedName("deliveryAddressId")
    val deliveryAddressId: Int, // Сервер ждет ID, а не объект!

    @SerializedName("deliveryMethod")
    val deliveryMethod: Int,

    @SerializedName("paymentMethod")
    val paymentMethod: Int,

    @SerializedName("orderItems")
    val orderItems: List<OrderItemRequestDto>
)

// [НОВОЕ] Упрощенный DTO для элемента заказа в запросе (как в Frontend src/models/OrderItem.ts)
data class OrderItemRequestDto(
    @SerializedName("productId")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int
)

data class PlaceOrderResponse(
    @SerializedName("orderId")
    val orderId: Int
    // paymentData тут опускаем, пока не делаем LiqPay
)

// ... (OrderDto и мапперы ответов оставляем как были, они для чтения истории) ...
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
    @SerializedName("paymentStatus") val paymentStatus: Int,
    @SerializedName("orderItems") val orderItems: List<OrderItemDto>
)

fun OrderItemDto.toDomain(): OrderItem {
    return OrderItem(
        productId = this.productId.toString(),
        quantity = this.quantity,
        productName = this.productName ?: "Unknown Product",
        price = this.price ?: 0.0,
        imageUrl = this.productImageUrl ?: "https://example.com/placeholder.jpg"
    )
}

fun OrderDto.toDomain(): Order {
    val defaultStatus = OrderStatus.Created
    val defaultDelivery = DeliveryMethod.Courier
    val defaultPayment = PaymentMethod.Card
    val defaultPaymentStatus = PaymentStatus.NotPaid

    return Order(
        id = this.id,
        userId = this.userId,
        orderDate = this.orderDate,
        status = OrderStatus.entries.find { it.id == this.status } ?: defaultStatus,
        totalAmount = this.totalAmount,
        deliveryMethod = DeliveryMethod.entries.find { it.id == this.deliveryMethod } ?: defaultDelivery,
        paymentMethod = PaymentMethod.entries.find { it.id == this.paymentMethod } ?: defaultPayment,
        deliveryAddressId = this.deliveryAddressId,
        paymentStatus = PaymentStatus.entries.find { it.id == this.paymentStatus } ?: defaultPaymentStatus,
        orderItems = this.orderItems.map { it.toDomain() }
    )
}