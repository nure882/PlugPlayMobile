package com.plugplay.plugplaymobile.domain.model


data class Item(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrls: List<String>,
    val isAvailable: Boolean = true,
    val brand: String,
    val category: String
)