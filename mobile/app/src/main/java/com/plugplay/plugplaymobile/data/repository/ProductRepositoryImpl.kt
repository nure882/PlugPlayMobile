package com.plugplay.plugplaymobile.data.repository

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

    override suspend fun getProducts(
        categoryId: Int?,
        minPrice: Double?,
        maxPrice: Double?,
        sort: String?,
        filterString: String?
    ): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                // [ИСПРАВЛЕНО]
                // Если категория не выбрана (null), используем Int.MAX_VALUE (2147483647),
                // чтобы вызвать метод фильтрации для "Всех товаров", как на фронтенде.
                // Иначе параметры сортировки и цены просто игнорировались.
                val actualCategoryId = categoryId ?: 2147483647

                val response = apiService.filterProducts(
                    categoryId = actualCategoryId,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    filter = filterString,
                    sort = sort
                )

                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.products.toDomainList()
                } else {
                    throw Exception("Failed to filter products: ${response.message()}")
                }
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
                throw Exception("Failed to fetch item")
            }
        }
    }

    override suspend fun getProductAttributes(categoryId: Int, productId: Int): Result<List<AttributeGroup>> {
        // Используем общий метод
        return getAttributesForFilter(categoryId, listOf(productId))
    }

    override suspend fun getAttributesForFilter(categoryId: Int, productIds: List<Int>): Result<List<AttributeGroup>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                // Отправляем просто список ID [1, 2, 3]
                val response = apiService.getAttributeGroups(categoryId, productIds)

                if (response.isSuccessful && response.body() != null) {
                    val dtoList = response.body()!!
                    dtoList.mapNotNull { it.toDomain() }
                } else {
                    emptyList()
                }
            }
        }
    }
}