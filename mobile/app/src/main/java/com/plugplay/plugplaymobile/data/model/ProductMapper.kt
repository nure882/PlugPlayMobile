package com.plugplay.plugplaymobile.data.model

import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Product

/**
 * Маппер DTO -> Product (для екрану СПИСКУ)
 */
fun ProductDto.toDomain(): Product {

    val firstImage = this.productImages?.images?.firstOrNull()?.imageUrl
        ?: "https://example.com/placeholder.jpg" // Заглушка

    return Product(
        id = this.id.toString(),
        title = this.name ?: "Без назви",
        priceValue = String.format("%.2f ₴", this.price ?: 0.0),
        image = firstImage
    )
}

/**
 * Допоміжна функція для мапінгу списку
 */
fun List<ProductDto>.toDomainList(): List<Product> {
    return this.map { it.toDomain() }
}


/**
 * [НОВИЙ МАППЕР]
 * Маппер DTO -> Item (для екрану ДЕТАЛЕЙ)
 */
fun ProductDto.toDomainItem(): Item {

    val firstImage = this.productImages?.images?.firstOrNull()?.imageUrl
        ?: "https://example.com/placeholder.jpg" // Заглушка

    return Item(
        id = this.id.toString(),
        name = this.name ?: "Без назви",
        description = this.description ?: "Опис відсутній.",
        price = this.price ?: 0.0,
        imageUrl = firstImage,
        isAvailable = (this.stockQuantity ?: 0) > 0,

        // У JSON немає "brand", тому беремо назву категорії
        brand = this.category?.name ?: "N/A",

        category = this.category?.name ?: "N/A"
    )
}