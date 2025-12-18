package com.plugplay.plugplaymobile.domain.model

data class CartItem(
    val id: Long,
    val productId: String,
    val name: String,
    val imageUrl: String,
    val unitPrice: Double,
    val quantity: Int,
    val total: Double
)