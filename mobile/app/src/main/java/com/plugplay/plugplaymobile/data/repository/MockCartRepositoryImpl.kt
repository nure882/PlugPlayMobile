package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.local.CartLocalDataSource
import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.repository.CartRepository
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import java.lang.Exception

@Singleton
class MockCartRepositoryImpl @Inject constructor(
    private val localDataSource: CartLocalDataSource,
    private val productRepository: ProductRepository // To fetch product details
) : CartRepository {

    // Имитация API/Remote Cart для зарегистрированных пользователей
    private val _remoteCart = mutableMapOf<Int, MutableList<CartItem>>()

    override fun getCartItems(userId: Int?): Flow<List<CartItem>> {
        // Для мока завжди повертаємо локальний Flow, щоб UI оновлювався
        return localDataSource.guestCart
    }

    override suspend fun addToCart(userId: Int?, productId: String, quantity: Int): Result<Unit> = runCatching {
        delay(200)

        // 1. Получаем детали продукта для создания CartItem
        val productResult = productRepository.getProductById(productId)
        // [ИСПРАВЛЕНО] Нужно убедиться, что MockProductRepositoryImpl возвращает Item с полем price (Item.kt)
        val product = productResult.getOrThrow()

        // 2. Определяем, куда добавлять (Local/Remote)
        val currentCart = if (userId != null) {
            _remoteCart.getOrPut(userId) { mutableListOf() }
        } else {
            localDataSource.value.toMutableList() // Беремо поточне значення
        }

        val existingItem = currentCart.find { it.productId == productId }

        if (existingItem != null) {
            // Обновляем количество существующего товара
            val newQuantity = existingItem.quantity + quantity
            val updatedItem = existingItem.copy(
                quantity = newQuantity,
                total = newQuantity * product.price
            )
            val index = currentCart.indexOf(existingItem)
            currentCart[index] = updatedItem

        } else {
            // Добавляем новый товар
            val newCartItem = CartItem(
                id = localDataSource.getNextId(), // Мок ID
                productId = product.id,
                name = product.name,
                imageUrl = product.imageUrl,
                unitPrice = product.price,
                quantity = quantity,
                total = product.price * quantity
            )
            currentCart.add(newCartItem)
        }

        // 3. Сохраняем (Local/Remote)
        if (userId == null) {
            localDataSource.saveGuestCart(currentCart)
        } else {
            // В мок-репозитории достаточно обновить mutableMap, но для UI нужно отправить сигнал
            localDataSource.saveGuestCart(currentCart) // Для мока используем localDataSource.saveGuestCart для обновления Flow
        }
        Unit
    }

    override suspend fun updateQuantity(userId: Int?, cartItemId: Long, newQuantity: Int): Result<Unit> = runCatching {
        delay(200)
        if (newQuantity < 1) return@runCatching Unit // Ігноруємо, якщо кількість < 1

        val currentCart = if (userId != null) {
            _remoteCart.getOrPut(userId) { mutableListOf() }
        } else {
            localDataSource.value.toMutableList()
        }

        val existingItem = currentCart.find { it.id == cartItemId } ?: throw Exception("Cart item not found")
        val index = currentCart.indexOf(existingItem)

        // Обновляем количество и общую стоимость
        val updatedItem = existingItem.copy(
            quantity = newQuantity,
            total = newQuantity * existingItem.unitPrice
        )
        currentCart[index] = updatedItem

        // Сохраняем (Local/Remote)
        if (userId == null) {
            localDataSource.saveGuestCart(currentCart)
        } else {
            localDataSource.saveGuestCart(currentCart)
        }
        Unit
    }

    override suspend fun deleteCartItem(userId: Int?, cartItemId: Long): Result<Unit> = runCatching {
        delay(200)

        val currentCart = if (userId != null) {
            _remoteCart.getOrPut(userId) { mutableListOf() }
        } else {
            localDataSource.value.toMutableList()
        }

        val updatedCart = currentCart.filter { it.id != cartItemId }

        // Сохраняем (Local/Remote)
        if (userId == null) {
            localDataSource.saveGuestCart(updatedCart)
        } else {
            _remoteCart[userId] = updatedCart.toMutableList()
            localDataSource.saveGuestCart(updatedCart)
        }
        Unit
    }

    override suspend fun clearCart(userId: Int?): Result<Unit> = runCatching {
        delay(200)
        if (userId == null) {
            localDataSource.clearGuestCart()
        } else {
            _remoteCart[userId] = mutableListOf()
            localDataSource.clearGuestCart() // Обновляем и локальный Flow для мока
        }
        Unit
    }
}