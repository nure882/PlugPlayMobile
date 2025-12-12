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

    // Локальний кеш для мапінгу: Product ID -> Wishlist Item ID
    // Потрібен, щоб видалити товар (API вимагає Wishlist Item ID, а UI знає лише Product ID)
    private val productToWishlistIdMap = ConcurrentHashMap<Int, Int>()

    override suspend fun getWishlist(userId: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Отримуємо список зв'язків [ {id: 55, productId: 10}, ... ]
            val response = apiService.getUserWishList()

            if (response.isSuccessful && response.body() != null) {
                val wishlistItems = response.body()!!

                // Оновлюємо мапу ID для майбутніх видалень
                productToWishlistIdMap.clear()
                wishlistItems.forEach {
                    productToWishlistIdMap[it.productId] = it.id
                }

                // 2. Паралельно завантажуємо деталі товарів ("гідратація")
                val deferredProducts = wishlistItems.map { dto ->
                    async {
                        // Завантажуємо товар по productId
                        productRepository.getProductById(dto.productId.toString())
                            .map { item ->
                                // Мапимо в Product для списку
                                Product(
                                    id = item.id,
                                    title = item.name,
                                    priceValue = String.format("%.2f ₴", item.price),
                                    image = item.imageUrls.firstOrNull() ?: "",
                                    price = item.price
                                )
                            }
                            .getOrNull() // Якщо товар не знайдено (видалений з магазину), ігноруємо
                    }
                }

                // Чекаємо всіх і фільтруємо помилки
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
                // Зберігаємо новий ID зв'язку в мапу
                val newItemId = response.body()!!.itemId
                productToWishlistIdMap[productId] = newItemId
            } else if (response.code() == 409) {
                // Вже додано - ігноруємо помилку, все ок
            } else {
                throw Exception("Failed to add to wishlist: ${response.code()}")
            }
            Unit // <--- [FIX] Явно повертаємо Unit, щоб runCatching знав тип
        }
    }

    override suspend fun removeFromWishlist(userId: Int, productId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Знаходимо ID запису вішлиста по ID товару
            val wishlistItemId = productToWishlistIdMap[productId]

            if (wishlistItemId != null) {
                val response = apiService.removeItemFromWishList(wishlistItemId)
                if (response.isSuccessful) {
                    productToWishlistIdMap.remove(productId)
                } else {
                    throw Exception("Failed to remove from wishlist: ${response.code()}")
                }
            } else {
                // Якщо ID немає в кеші, спробуємо "сліпий" метод або перезавантажимо список
                Log.w("WishlistRepo", "Wishlist Item ID not found for product $productId. Refreshing list might be needed.")

                // Спробуємо отримати актуальний список, щоб знайти ID (fallback)
                val listResult = getWishlist(userId) // Це оновить productToWishlistIdMap побічним ефектом

                // Тепер перевіряємо знову
                val newId = productToWishlistIdMap[productId]

                if (newId != null) {
                    val retryResponse = apiService.removeItemFromWishList(newId)
                    if (retryResponse.isSuccessful) {
                        productToWishlistIdMap.remove(productId)
                    } else {
                        throw Exception("Failed retry remove: ${retryResponse.code()}")
                    }
                } else {
                    // Якщо і після оновлення немає - значить товару і так немає у вішлисті, все ок
                }
            }
            Unit // <--- [FIX] Явно повертаємо Unit
        }
    }
}