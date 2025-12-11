package com.plugplay.plugplaymobile.data.model

import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Product

private const val PLACEHOLDER_URL = "https://example.com/placeholder.jpg"

fun ProductDto.toDomain(): Product {
    val firstImage = this.pictureUrls?.firstOrNull() ?: PLACEHOLDER_URL

    return Product(
        id = this.id.toString(),
        title = this.name ?: "Без назви",
        priceValue = String.format("%.2f ₴", this.price ?: 0.0),
        image = firstImage,
        price = this.price ?: 0.0 // [НОВОЕ] Сохраняем цену как число
    )
}

fun List<ProductDto>.toDomainList(): List<Product> {
    return this.map { it.toDomain() }
}

fun ProductDto.toDomainItem(): Item {
    val imageUrls = this.pictureUrls
        .orEmpty()
        .filter { it.isNotBlank() }
        .ifEmpty { listOf(PLACEHOLDER_URL) }

    return Item(
        id = this.id.toString(),
        name = this.name ?: "Без назви",
        description = this.description ?: "Опис відсутній.",
        price = this.price ?: 0.0,
        imageUrls = imageUrls,
        isAvailable = (this.stockQuantity ?: 0) > 0,
        brand = this.category?.name ?: "N/A",
        category = this.category?.name ?: "N/A",
    )
}