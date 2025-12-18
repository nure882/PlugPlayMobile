package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Category
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Product

interface ProductRepository {
    // [ОБНОВЛЕНО]
    suspend fun getProducts(
        categoryId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sort: String? = null,
        filterString: String? = null
    ): Result<List<Product>>

    suspend fun searchProducts(query: String): Result<List<Product>>
    suspend fun getProductById(itemId: String): Result<Item>

    suspend fun getCategories(): Result<List<Category>>
    suspend fun getProductAttributes(categoryId: Int, productId: Int): Result<List<AttributeGroup>>
    suspend fun getAttributesForFilter(categoryId: Int, productIds: List<Int>): Result<List<AttributeGroup>>
}