package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class OrderAddressDto(
    val city: String,
    val street: String,
    val house: String,
    val apartments: String? = null
)

data class PlaceOrderRequest(
    @SerializedName("cartId")
    val cartId: Int? = null,

    @SerializedName("userId")
    val userId: Int?,

    @SerializedName("deliveryMethod")
    val deliveryMethod: Int,

    @SerializedName("paymentMethod")
    val paymentMethod: Int,

    @SerializedName("address")
    val address: OrderAddressDto,

    @SerializedName("customerName")
    val customerName: String,

    @SerializedName("customerEmail")
    val customerEmail: String,

    @SerializedName("customerPhone")
    val customerPhone: String
)

data class PlaceOrderResponse(
    @SerializedName("orderId")
    val orderId: Int,
    @SerializedName("totalPrice")
    val totalPrice: Double
)