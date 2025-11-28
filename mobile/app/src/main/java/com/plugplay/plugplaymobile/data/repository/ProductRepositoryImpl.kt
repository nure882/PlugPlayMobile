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

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        return runCatching {
            val productsDtoList = apiService.getProducts()
            Log.d("ProductRepositoryImpl", "Fetched products: $productsDtoList")
            productsDtoList.toDomainList()
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
}