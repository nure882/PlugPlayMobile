package com.plugplay.plugplaymobile.data.model

import com.plugplay.plugplaymobile.domain.model.Product

// Функция расширения для преобразования DTO в чистую Domain Model
fun ProductDto.toDomain(): Product {
    return Product(
        id = this.id,
        title = this.name,
        priceValue = String.format("%.2f ₴", this.price), // Форматирование цены
        image = this.imageUrl
    )
}

// Удобная функция для маппинга списка
fun List<ProductDto>.toDomainList(): List<Product> {
    return this.map { it.toDomain() }
}