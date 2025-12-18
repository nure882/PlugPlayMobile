package com.plugplay.plugplaymobile.data.local

import com.plugplay.plugplaymobile.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistLocalDataSource @Inject constructor() {


    private val _wishlist = MutableStateFlow<List<Product>>(emptyList())
    val wishlist: StateFlow<List<Product>> = _wishlist.asStateFlow()

    fun addToWishlist(product: Product) {
        _wishlist.update { currentList ->
            if (currentList.none { it.id == product.id }) {
                currentList + product
            } else {
                currentList
            }
        }
    }

    fun removeFromWishlist(productId: String) {
        _wishlist.update { currentList ->
            currentList.filter { it.id != productId }
        }
    }

    fun isFavorite(productId: String): Boolean {
        return _wishlist.value.any { it.id == productId }
    }
}