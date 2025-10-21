package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.model.toDomainItem // [ДОДАНО]
import com.plugplay.plugplaymobile.data.model.toDomainList
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import javax.inject.Inject
import java.lang.Exception

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        return runCatching {
            // [ВИПРАВЛЕНО] Отримуємо об'єкт -> беремо поле .products -> мапимо
            val response = apiService.getProducts()
            response.products.toDomainList()
        }
    }

    override suspend fun getProductById(itemId: String): Result<Item> {
        return runCatching {
            // [ВИПРАВЛЕНО] Отримуємо ProductDto -> мапимо в Item
            val response = apiService.getProductById(itemId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomainItem()
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception(errorBody ?: "Failed to fetch item $itemId: ${response.message()}")
            }
        }
    }
}