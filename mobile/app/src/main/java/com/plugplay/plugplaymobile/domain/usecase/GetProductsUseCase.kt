package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Use Case для отримання списку всіх товарів.
 */
class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    // [ЗМІНЕНО] Приймає необов'язковий categoryId
    suspend operator fun invoke(categoryId: Int? = null): Result<List<Product>> {
        return repository.getProducts(categoryId) // <--- ПЕРЕДАЄМО
    }
}