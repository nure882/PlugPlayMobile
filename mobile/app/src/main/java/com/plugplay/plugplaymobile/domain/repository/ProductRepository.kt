package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.Product

interface ProductRepository {

    // Возвращает Result, чтобы явно обрабатывать успех (Success) или ошибку (Failure)
    suspend fun getProducts(): Result<List<Product>>

    // suspend fun getProductDetails(id: Int): Result<Product>
    // suspend fun searchProducts(query: String): Result<List<Product>>
}