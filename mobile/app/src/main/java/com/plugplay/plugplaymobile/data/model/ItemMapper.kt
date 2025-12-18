package com.plugplay.plugplaymobile.data.model

import com.plugplay.plugplaymobile.domain.model.Item

/**
 * Маппер для DTO деталей товару в доменну модель Item.
 */
fun ItemDto.toDomain(): Item {
    return Item(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        imageUrls = this.imageUrls,
        isAvailable = this.isAvailable,
        brand = this.brand,
        category = this.category,
        stockQuantity = this.stockQuantity,
    )
}