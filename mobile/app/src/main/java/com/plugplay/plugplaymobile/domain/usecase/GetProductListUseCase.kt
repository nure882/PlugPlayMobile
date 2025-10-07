package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import javax.inject.Inject

// Hilt инжектирует ProductRepository
class GetProductListUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    // Оператор 'invoke' позволяет вызывать класс как функцию: useCase()
    suspend operator fun invoke(): Result<List<Product>> {
        // Здесь могла бы быть дополнительная бизнес-логика,
        // но пока просто передаем запрос в репозиторий
        return repository.getProducts()
    }
}