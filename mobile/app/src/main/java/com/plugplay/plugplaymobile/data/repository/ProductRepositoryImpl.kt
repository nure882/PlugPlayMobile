package com.plugplay.plugplaymobile.data.repository

import android.util.Log
import com.plugplay.plugplaymobile.data.model.toDomainItem
import com.plugplay.plugplaymobile.data.model.toDomainList
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import javax.inject.Inject
import java.lang.Exception
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService
) : ProductRepository {

    // [ЗМІНЕНО] Реалізація тепер приймає categoryId
    override suspend fun getProducts(categoryId: Int?): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val productsDtoList = if (categoryId == null) {
                    // Якщо ID категорії немає, використовуємо існуючий ендпоінт "all"
                    val allProducts = apiService.getProducts()
                    Log.d("ProductRepositoryImpl", "Fetched all products: ${allProducts.size}")
                    allProducts
                } else {
                    // [НОВИЙ ВИКЛИК] Використовуємо ендпоінт фільтрації
                    val response = apiService.filterProducts(
                        categoryId = categoryId,
                        page = 1,
                        pageSize = 100
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val filteredProducts = response.body()!!.products
                        Log.d("ProductRepositoryImpl", "Fetched products by category $categoryId: ${filteredProducts.size}")
                        filteredProducts
                    } else {
                        val errorBody = response.errorBody()?.string()
                        throw Exception(errorBody ?: "Failed to filter products by category: ${response.message()}")
                    }
                }

                productsDtoList.toDomainList()
            }
        }
    }

    // [ОНОВЛЕНО] Конвертуємо String ID в Int для виклику API
    override suspend fun getProductById(itemId: String): Result<Item> {
        return runCatching {
            // API вимагає Int ID, тому парсимо String
            val itemIdInt = itemId.toIntOrNull() ?: throw IllegalArgumentException("Invalid product ID format. Expected integer, got $itemId")

            val response = apiService.getProductById(itemIdInt)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomainItem()
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception(errorBody ?: "Failed to fetch item $itemId: ${response.message()}")
            }
        }
    }

    override suspend fun searchProducts(query: String): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                // Вызываем API поиска
                val productsDto = apiService.searchProducts(query = query)
                // Мапим в доменные модели
                productsDto.toDomainList()
            }
        }
    }
}