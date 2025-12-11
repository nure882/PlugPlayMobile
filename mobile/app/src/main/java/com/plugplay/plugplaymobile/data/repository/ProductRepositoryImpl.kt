package com.plugplay.plugplaymobile.data.repository

import android.util.Log
import com.plugplay.plugplaymobile.data.model.AttributeRequestDto
import com.plugplay.plugplaymobile.data.model.toDomain
import com.plugplay.plugplaymobile.data.model.toDomainItem
import com.plugplay.plugplaymobile.data.model.toDomainList
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService
) : ProductRepository {

    private val TAG = "ProductRepo"

    override suspend fun getProducts(categoryId: Int?): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val productsDtoList = if (categoryId == null) {
                    apiService.getProducts()
                } else {
                    val response = apiService.filterProducts(categoryId = categoryId)
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.products
                    } else {
                        throw Exception("Failed to filter products: ${response.message()}")
                    }
                }
                productsDtoList.toDomainList()
            }
        }
    }

    override suspend fun searchProducts(query: String): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val productsDto = apiService.searchProducts(query = query)
                productsDto.toDomainList()
            }
        }
    }

    override suspend fun getProductById(itemId: String): Result<Item> {
        return runCatching {
            val itemIdInt = itemId.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            val response = apiService.getProductById(itemIdInt)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomainItem()
            } else {
                throw Exception("Failed to fetch item: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getProductAttributes(categoryId: Int, productId: Int): Result<List<AttributeGroup>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                Log.d(TAG, "Fetching attributes for cat=$categoryId, prod=$productId")

                // [ИСПРАВЛЕНО] Отправляем просто список ID
                val productIds = listOf(productId)

                // Вызываем обновленный метод API
                val response = apiService.getAttributeGroups(categoryId, productIds)

                if (response.isSuccessful && response.body() != null) {
                    val dtoList = response.body()!!
                    val domainList = dtoList.mapNotNull { it.toDomain() }
                    domainList
                } else {
                    val error = response.errorBody()?.string()
                    Log.e(TAG, "Failed to load attributes: ${response.code()} $error")
                    emptyList()
                }
            }.onFailure { e ->
                Log.e(TAG, "Exception loading attributes", e)
            }
        }
    }
}