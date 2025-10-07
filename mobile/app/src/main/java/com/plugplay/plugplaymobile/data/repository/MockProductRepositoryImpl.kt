package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

// 💡 Класс-заглушка для репозитория
class MockProductRepositoryImpl @Inject constructor() : ProductRepository {

    // Статические данные для имитации списка товаров
    private val mockProducts = listOf(
        Product(1, "Світшот 'Плагін'", "899 ₴", "url_1"),
        Product(2, "Худі 'Плей'", "1250 ₴", "url_2"),
        Product(3, "Футболка 'Лого'", "450 ₴", "url_3"),
        Product(4, "Кросівки 'Флеш'", "2999 ₴", "url_4")
    )

    override suspend fun getProducts(): Result<List<Product>> {
        // Имитируем задержку сети
        delay(1000L)

        // Возвращаем успех с нашими статическими данными
        return Result.success(mockProducts)

        // 💡 Чтобы имитировать ошибку, можно вернуть:
        // return Result.failure(Exception("Сервер недоступний (Mock Error)"))
    }
}