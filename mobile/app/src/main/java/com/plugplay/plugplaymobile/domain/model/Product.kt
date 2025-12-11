package com.plugplay.plugplaymobile.domain.model

data class Product(
    val id: String,
    val title: String,
    val priceValue: String, // Форматированная цена (например, "1250 ₴") для UI
    val image: String,
    val price: Double // [НОВОЕ] Числовая цена для расчетов и маппинга
)