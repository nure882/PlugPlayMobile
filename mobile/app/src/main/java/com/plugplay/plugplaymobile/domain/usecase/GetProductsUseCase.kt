package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    // [ИСПРАВЛЕНО] Добавлены параметры для фильтрации и сортировки
    suspend operator fun invoke(
        categoryId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sort: String? = null,
        filterString: String? = null
    ): Result<List<Product>> {
        return repository.getProducts(categoryId, minPrice, maxPrice, sort, filterString)
    }
}