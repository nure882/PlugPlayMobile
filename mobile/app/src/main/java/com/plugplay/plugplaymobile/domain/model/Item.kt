package com.plugplay.plugplaymobile.domain.model


data class Item(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrls: List<String>,
    val isAvailable: Boolean = true,
    val brand: String,
    val category: String,

    // [ДОБАВЛЕНО] Поля для отображения рейтинга
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0
)