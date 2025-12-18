package com.plugplay.plugplaymobile.presentation.product_list

import com.plugplay.plugplaymobile.domain.model.Category
import com.plugplay.plugplaymobile.domain.model.Product

/**
 * Основной интерфейс состояний UI
 */
sealed interface ProductListState {
    data object Idle : ProductListState
    data object Loading : ProductListState
    data class Success(val products: List<Product>) : ProductListState
    data class Error(val message: String) : ProductListState
}

/**
 * Класс для хранения параметров фильтрации.
 * Его удобно использовать в ViewModel для управления состоянием фильтров.
 */
data class ProductFiltersState(
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null
) {
    companion object {
        // Начальное (пустое) состояние фильтров
        val Empty = ProductFiltersState()
    }
}