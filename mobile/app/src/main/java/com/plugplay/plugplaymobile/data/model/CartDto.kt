package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO для POST /api/Cart (додавання товару)
 */
data class CreateCartItemDto(
    val productId: Int,
    val userId: Int,
    val quantity: Int
)

/**
 * DTO для PUT /api/Cart/quantity (оновлення кількості)
 */
data class UpdateCartItemQuantityDto(
    val cartItemId: Int,
    val newQuantity: Int
)

/**
 * DTO для одного елемента кошика, повертається API GET /api/Cart/{userId}
 * Назви полів імітують наявність необхідних даних для мапінгу в CartItem Domain Model.
 */
data class CartItemDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("productId")
    val productId: Int,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("total")
    val total: Double,


    @SerializedName("productName")
    val productName: String?,

    @SerializedName("unitPrice")
    val unitPrice: Double?,

    @SerializedName("productImage")
    val productImage: String?
)