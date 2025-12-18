package com.plugplay.plugplaymobile.domain.model

data class Product(
    val id: String,
    val title: String,
    val priceValue: String,
    val image: String,
    val price: Double
)