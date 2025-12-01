package com.plugplay.plugplaymobile.data.model

import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Product

// Default placeholder for missing images
private const val PLACEHOLDER_URL = "https://example.com/placeholder.jpg"

fun ProductDto.toDomain(): Product {

    // [ОНОВЛЕНО] Використовуємо нове поле pictureUrls
    val firstImage = this.pictureUrls?.firstOrNull() ?: PLACEHOLDER_URL

    return Product(
        id = this.id.toString(),
        title = this.name ?: "Без назви",
        priceValue = String.format("%.2f ₴", this.price ?: 0.0),
        image = firstImage
    )
}


fun List<ProductDto>.toDomainList(): List<Product> {
    return this.map { it.toDomain() }
}


/**
 * Маппер DTO -> Item (для екрану ДЕТАЛЕЙ)
 */
fun ProductDto.toDomainItem(): Item {



    // [ОНОВЛЕНО] Використовуємо нове поле pictureUrls
    val imageUrls = this.pictureUrls
        .orEmpty()
        .filter { it.isNotBlank() }
        .ifEmpty { listOf(PLACEHOLDER_URL) }

    return Item(
        id = this.id.toString(),
        name = this.name ?: "Без назви",
        description = this.description ?: "Опис відсутній.",
        price = this.price ?: 0.0,
        imageUrls = imageUrls, // ВИКОРИСТОВУЄМО СПИСОК URL
        isAvailable = (this.stockQuantity ?: 0) > 0,

        // У JSON немає "brand", тому беремо назву категорії
        brand = this.category?.name ?: "N/A",

        category = this.category?.name ?: "N/A",


    )
}