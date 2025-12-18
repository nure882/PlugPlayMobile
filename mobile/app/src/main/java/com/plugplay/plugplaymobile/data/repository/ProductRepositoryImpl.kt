package com.plugplay.plugplaymobile.data.repository

import android.util.Log
import com.plugplay.plugplaymobile.data.model.AttributeRequestDto
import com.plugplay.plugplaymobile.data.model.toDomain
import com.plugplay.plugplaymobile.data.model.toDomainItem
import com.plugplay.plugplaymobile.data.model.toDomainList
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Category
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList
import javax.inject.Inject

private const val TAG = "ProductRepo"

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




                val actualCategoryId = categoryId ?: 2147483647

                val response = apiService.filterProducts(
                    categoryId = actualCategoryId,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    filter = filterString,
                    sort = sort
                )

                if (response.isSuccessful && response.body() != null) {
                    val productsResponse = response.body()!!
                    Log.d(TAG, "getProducts DTO received: ${productsResponse.products}")
                    productsResponse.products.toDomainList()
                } else {
                    Log.e(TAG, "getProducts API Failed: ${response.code()} ${response.message()}")
                    throw Exception("Failed to filter products: ${response.message()}")
                }
            }
        }
    }

    override suspend fun searchProducts(query: String): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val productsDto = apiService.searchProducts(query = query)
                Log.d(TAG, "searchProducts DTO received: $productsDto")
                productsDto.toDomainList()
            }
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getCategories()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.map { dto ->
                        Category(
                            id = dto.id,
                            name = dto.name ?: "Unknown",
                            parentId = dto.parent?.id
                        )
                    }
                } else {
                    throw Exception("Failed to fetch categories: ${response.message()}")
                }
            }
        }
    }

    override suspend fun getProductById(itemId: String): Result<Item> {
        return runCatching {
            val itemIdInt = itemId.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            val response = apiService.getProductById(itemIdInt)
            if (response.isSuccessful && response.body() != null) {
                val itemDto = response.body()!!
                Log.d(TAG, "getProductById DTO received: $itemDto")
                itemDto.toDomainItem()
            } else {
                Log.e(TAG, "getProductById API Failed: ${response.code()} ${response.message()}")
                throw Exception("Failed to fetch item")
            }
        }
    }

    override suspend fun getProductAttributes(categoryId: Int, productId: Int): Result<List<AttributeGroup>> {

        return getAttributesForFilter(categoryId, listOf(productId))
    }

    override suspend fun getAttributesForFilter(categoryId: Int, productIds: List<Int>): Result<List<AttributeGroup>> {
        return withContext(Dispatchers.IO) {
            runCatching {

                val requestDto = AttributeRequestDto(
                    productIds = productIds,
                    selectedAttrsIds = emptyList()
                )


                val response = apiService.getAttributeGroups(
                    categoryId = categoryId,
                    request = requestDto
                )

                if (response.isSuccessful && response.body() != null) {
                    val dtoList = response.body()!!
                    Log.d(TAG, "getAttributesForFilter DTO received: $dtoList")
                    dtoList.mapNotNull { it.toDomain() }
                } else {

                    val errorMsg = "Failed to fetch product attributes: ${response.message()} Code: ${response.code()}"
                    Log.e(TAG, errorMsg)
                    throw Exception(errorMsg)
                }
            }
        }
    }
}