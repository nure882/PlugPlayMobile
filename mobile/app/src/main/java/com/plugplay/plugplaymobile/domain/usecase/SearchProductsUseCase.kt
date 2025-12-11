package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(query: String): Result<List<Product>> {
        if (query.isBlank()) return Result.success(emptyList())
        return repository.searchProducts(query)
    }
}