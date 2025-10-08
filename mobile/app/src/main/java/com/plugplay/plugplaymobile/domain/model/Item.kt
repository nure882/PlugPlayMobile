package com.plugplay.plugplaymobile.domain.model

/**
 * Модель данных для товара в каталоге.
 */
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val isAvailable: Boolean = true, // По умолчанию доступен
    val brand: String,
    val category: String
)