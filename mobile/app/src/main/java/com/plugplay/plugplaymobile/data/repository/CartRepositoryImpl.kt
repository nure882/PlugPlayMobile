package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.local.CartLocalDataSource
import com.plugplay.plugplaymobile.data.model.CartItemDto
import com.plugplay.plugplaymobile.data.model.CreateCartItemDto
import com.plugplay.plugplaymobile.data.model.UpdateCartItemQuantityDto
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.repository.CartRepository
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import java.lang.Exception


fun CartItemDto.toDomain(): CartItem {
    val price = this.unitPrice ?: 0.0
    val imgUrl = this.productImage ?: "https://example.com/placeholder.jpg"


    val name = this.productName.orEmpty().ifEmpty { "Без назви" }

    return CartItem(
        id = this.id.toLong(),
        productId = this.productId.toString(),
        name = name,
        imageUrl = imgUrl,
        unitPrice = price,
        quantity = this.quantity,
        total = this.total
    )
}

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService,
    private val localDataSource: CartLocalDataSource,
    private val productRepository: ProductRepository
) : CartRepository {

    override fun getCartItems(userId: Int?): Flow<List<CartItem>> {

        return localDataSource.guestCart
    }

    override suspend fun addToCart(userId: Int?, productId: String, quantity: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId != null) {

            runCatching {
                val request = CreateCartItemDto(
                    productId = productId.toInt(),
                    userId = userId,
                    quantity = quantity
                )
                val response = apiService.addToCart(request)
                if (!response.isSuccessful) {
                    throw Exception("Failed to add to cart via API: ${response.message()}")
                }

                refreshLocalCart(userId)
                Unit
            }
        } else {

            runCatching {

                val product = productRepository.getProductById(productId).getOrThrow()
                val cart = localDataSource.value.toMutableList()
                val existingItem = cart.find { it.productId == productId }

                if (existingItem != null) {
                    val newQuantity = existingItem.quantity + quantity
                    val updatedItem = existingItem.copy(
                        quantity = newQuantity,
                        total = newQuantity * product.price
                    )
                    cart[cart.indexOf(existingItem)] = updatedItem
                } else {
                    val newCartItem = CartItem(
                        id = localDataSource.getNextId(),
                        productId = product.id,
                        name = product.name,
                        imageUrl = product.imageUrls[0],
                        unitPrice = product.price,
                        quantity = quantity,
                        total = product.price * quantity
                    )
                    cart.add(newCartItem)
                }
                localDataSource.saveGuestCart(cart)
                Unit
            }
        }
    }

    override suspend fun updateQuantity(userId: Int?, cartItemId: Long, newQuantity: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (newQuantity < 1) return@withContext Result.success(Unit)

        if (userId != null) {

            runCatching {
                val request = UpdateCartItemQuantityDto(
                    cartItemId = cartItemId.toInt(),
                    newQuantity = newQuantity
                )
                val response = apiService.updateQuantity(request)
                if (!response.isSuccessful) {
                    throw Exception("Failed to update quantity via API: ${response.message()}")
                }
                refreshLocalCart(userId)
                Unit
            }
        } else {

            runCatching {
                val cart = localDataSource.value.toMutableList()
                val item = cart.find { it.id == cartItemId } ?: throw Exception("Cart item not found")

                item.copy(
                    quantity = newQuantity,
                    total = newQuantity * item.unitPrice
                ).also { updatedItem ->
                    cart[cart.indexOf(item)] = updatedItem
                }
                localDataSource.saveGuestCart(cart)
                Unit
            }
        }
    }

    override suspend fun deleteCartItem(userId: Int?, cartItemId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId != null) {

            runCatching {
                val response = apiService.deleteCartItem(cartItemId.toInt())
                if (!response.isSuccessful) {
                    throw Exception("Failed to delete cart item via API: ${response.message()}")
                }
                refreshLocalCart(userId)
                Unit
            }
        } else {

            runCatching {
                val updatedCart = localDataSource.value.filter { it.id != cartItemId }
                localDataSource.saveGuestCart(updatedCart)
                Unit
            }
        }
    }

    override suspend fun clearCart(userId: Int?): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId != null) {

            runCatching {
                val response = apiService.clearCart(userId)
                if (!response.isSuccessful) {
                    throw Exception("Failed to clear cart via API: ${response.message()}")
                }
                localDataSource.clearGuestCart()
                Unit
            }
        } else {
            // [LOCAL] Логіка для гостя
            runCatching {
                localDataSource.clearGuestCart()
                Unit
            }
        }
    }

    /**
     * Helper для оновлення локального кешу після успішного API-виклику.
     * Включає логіку для завантаження назви/зображення, якщо вони відсутні в DTO кошика (що є причиною "Без назви").
     */
    private suspend fun refreshLocalCart(userId: Int) {
        try {
            val dtos = apiService.getCartItems(userId).body()

            val newCartItems = dtos?.map { dto ->
                var domainItem = dto.toDomain()

                // Перевірка, чи не отримали ми "Без назви" або пусту назву з API
                if (domainItem.name.isBlank() || domainItem.name == "Без назви") {
                    productRepository.getProductById(domainItem.productId)
                        .onSuccess { fullItem ->
                            domainItem = domainItem.copy(
                                name = fullItem.name,
                                imageUrl = fullItem.imageUrls.firstOrNull() ?: domainItem.imageUrl,
                                unitPrice = fullItem.price // Оновлюємо ціну, якщо потрібно
                            )
                        }
                        .onFailure {
                            // Якщо не вдалося завантажити деталі, залишаємо як є
                            println("ERROR: Failed to fetch full product details for cart item ${domainItem.productId}")
                        }
                }
                domainItem
            } ?: emptyList()

            localDataSource.saveGuestCart(newCartItems)

        } catch (e: Exception) {
            println("ERROR: Failed to refresh local cart after API mutation: ${e.message}")
        }
    }
}