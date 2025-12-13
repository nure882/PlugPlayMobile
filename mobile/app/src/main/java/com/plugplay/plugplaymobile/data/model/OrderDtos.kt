package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("orderDate") val orderDate: String,
    @SerializedName("status") val status: Int,
    @SerializedName("paymentStatus") val paymentStatus: Int,
    @SerializedName("deliveryAddressId") val deliveryAddressId: Int,
    @SerializedName("paymentMethod") val paymentMethod: Int,
    @SerializedName("deliveryMethod") val deliveryMethod: Int,
    @SerializedName("orderItems") val orderItems: List<OrderItemDto>
)

data class OrderItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("productId") val productId: Int,
    @SerializedName("productName") val productName: String?,
    @SerializedName("productImageUrl") val productImageUrl: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: Double?,
)

data class PlaceOrderRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("deliveryAddressId") val deliveryAddressId: Int,
    @SerializedName("orderItems") val orderItems: List<OrderItemRequestDto>,
    @SerializedName("paymentMethod") val paymentMethod: Int,
    @SerializedName("deliveryMethod") val deliveryMethod: Int
)

data class OrderItemRequestDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class PlaceOrderResponse(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("paymentData") val paymentData: LiqPayInitResponse?
)