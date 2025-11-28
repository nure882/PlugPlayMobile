package com.plugplay.plugplaymobile.data.model

import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Product

// Default placeholder for missing images
private const val PLACEHOLDER_URL = "https://example.com/placeholder.jpg"

/**
 * Маппер DTO -> Product (для екрану СПИСКУ)
 */
fun ProductDto.toDomain(): Product {

    val firstImage = this.productImages?.images?.firstOrNull()?.imageUrl
        ?: PLACEHOLDER_URL // Заглушка

    return Product(
        id = this.id.toString(),
        title = this.name ?: "Без назви",
        priceValue = String.format("%.2f ₴", this.price ?: 0.0),
        image = firstImage // ВИКОРИСТОВУЄМО ОДНУ URL
    )
}

/**
 * Допоміжна функція для мапінгу списку
 */
fun List<ProductDto>.toDomainList(): List<Product> {
    return this.map { it.toDomain() }
}


/**
 * Маппер DTO -> Item (для екрану ДЕТАЛЕЙ)
 */
fun ProductDto.toDomainItem(): Item {

    // [ОНОВЛЕНО] Збираємо список усіх доступних URL
    val imageUrls = this.productImages?.images
        ?.mapNotNull { it.imageUrl }
        ?.filter { it.isNotBlank() }
        .orEmpty()
        .ifEmpty { listOf(PLACEHOLDER_URL) } // Якщо список порожній, додаємо заглушку

    return Item(
        id = this.id.toString(),
        name = this.name ?: "Без назви",
        description = this.description ?: "Опис відсутній.",
        price = this.price ?: 0.0,
        imageUrls = imageUrls, // ВИКОРИСТОВУЄМО СПИСОК URL
        isAvailable = (this.stockQuantity ?: 0) > 0,

        // У JSON немає "brand", тому беремо назву категорії
        brand = this.category?.name ?: "N/A",

        category = this.category?.name ?: "N/A"
    )
}