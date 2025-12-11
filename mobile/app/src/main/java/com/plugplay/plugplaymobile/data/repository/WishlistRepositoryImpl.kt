package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.local.WishlistLocalDataSource
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import com.plugplay.plugplaymobile.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WishlistRepositoryImpl @Inject constructor(
    private val localDataSource: WishlistLocalDataSource,
    private val productRepository: ProductRepository
) : WishlistRepository {

    override suspend fun getWishlist(userId: Int): Result<List<Product>> {
        // Игнорируем userId для локальной реализации, или используем его как ключ в будущем
        return Result.success(localDataSource.wishlist.value)
    }

    override suspend fun addToWishlist(userId: Int, productId: Int): Result<Unit> {
        return runCatching {
            // 1. Получаем детали товара, чтобы сохранить их в вишлист
            // productId у нас Int, но ProductRepository работает со String ID в методе getProductById
            // Предполагаем, что getProductById принимает String
            val productResult = productRepository.getProductById(productId.toString())

            val item = productResult.getOrThrow()

            // Преобразуем Item в Product (упрощенная модель для списков)
            val product = Product(
                id = item.id,
                title = item.name,
                priceValue = String.format("%.2f ₴", item.price), // Формируем строку цены
                image = item.imageUrls.firstOrNull() ?: "",
                price = item.price
            )

            localDataSource.addToWishlist(product)
        }
    }

    override suspend fun removeFromWishlist(userId: Int, productId: Int): Result<Unit> {
        return runCatching {
            localDataSource.removeFromWishlist(productId.toString())
        }
    }
}