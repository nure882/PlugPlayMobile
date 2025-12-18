package com.plugplay.plugplaymobile.data.repository

import android.util.Log
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import com.plugplay.plugplaymobile.domain.repository.WishlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService,
    private val productRepository: ProductRepository
) : WishlistRepository {



    private val productToWishlistIdMap = ConcurrentHashMap<Int, Int>()

    override suspend fun getWishlist(userId: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        runCatching {

            val response = apiService.getUserWishList()

            if (response.isSuccessful && response.body() != null) {
                val wishlistItems = response.body()!!


                productToWishlistIdMap.clear()
                wishlistItems.forEach {
                    productToWishlistIdMap[it.productId] = it.id
                }


                val deferredProducts = wishlistItems.map { dto ->
                    async {

                        productRepository.getProductById(dto.productId.toString())
                            .map { item ->

                                Product(
                                    id = item.id,
                                    title = item.name,
                                    priceValue = String.format("%.2f ₴", item.price),
                                    image = item.imageUrls.firstOrNull() ?: "",
                                    price = item.price
                                )
                            }
                            .getOrNull()
                    }
                }


                deferredProducts.awaitAll().filterNotNull()
            } else {
                throw Exception("Failed to fetch wishlist: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun addToWishlist(userId: Int, productId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.addItemToWishList(productId)

            if (response.isSuccessful && response.body() != null) {

                val newItemId = response.body()!!.itemId
                productToWishlistIdMap[productId] = newItemId
            } else if (response.code() == 409) {

            } else {
                throw Exception("Failed to add to wishlist: ${response.code()}")
            }
            Unit
        }
    }

    override suspend fun removeFromWishlist(userId: Int, productId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {

            val wishlistItemId = productToWishlistIdMap[productId]

            if (wishlistItemId != null) {
                val response = apiService.removeItemFromWishList(wishlistItemId)
                if (response.isSuccessful) {
                    productToWishlistIdMap.remove(productId)
                } else {
                    throw Exception("Failed to remove from wishlist: ${response.code()}")
                }
            } else {

                Log.w("WishlistRepo", "Wishlist Item ID not found for product $productId. Refreshing list might be needed.")


                val listResult = getWishlist(userId)


                val newId = productToWishlistIdMap[productId]

                if (newId != null) {
                    val retryResponse = apiService.removeItemFromWishList(newId)
                    if (retryResponse.isSuccessful) {
                        productToWishlistIdMap.remove(productId)
                    } else {
                        throw Exception("Failed retry remove: ${retryResponse.code()}")
                    }
                } else {

                }
            }
            Unit
        }
    }
}