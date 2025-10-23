package com.plugplay.plugplaymobile.domain.model

data class Product(
    val id: String,
    val title: String,
    val priceValue: String, // Форматированная цена (например, "1250 ₴")
    val image: String
)