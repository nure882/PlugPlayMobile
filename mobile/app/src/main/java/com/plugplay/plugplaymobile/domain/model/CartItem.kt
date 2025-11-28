package com.plugplay.plugplaymobile.domain.model

data class CartItem(
    val id: Long, // Unique ID for the cart entry (used for deletion/update)
    val productId: String,
    val name: String,
    val imageUrl: String,
    val unitPrice: Double, // The price of a single item
    val quantity: Int,
    val total: Double // quantity * unitPrice
)