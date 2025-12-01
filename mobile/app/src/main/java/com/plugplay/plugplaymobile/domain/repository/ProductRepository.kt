package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.model.Item // Використовуємо Item для деталей

interface ProductRepository {
    // [ЗМІНЕНО] Функція тепер приймає необов'язковий categoryId (Int)
    suspend fun getProducts(categoryId: Int? = null): Result<List<Product>> // <-- ЗМІНА ТУТ

    // Функція для отримання одного товару за ID (використовує модель Item)
    suspend fun getProductById(itemId: String): Result<Item>
}