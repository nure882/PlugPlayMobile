package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.model.Item // Використовуємо Item для деталей

interface ProductRepository {
    // Функція для отримання списку товарів (використовує модель Product)
    suspend fun getProducts(): Result<List<Product>>

    // Функція для отримання одного товару за ID (використовує модель Item)
    suspend fun getProductById(itemId: String): Result<Item>
}
