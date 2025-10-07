package com.plugplay.plugplaymobile.presentation.product_list

import com.plugplay.plugplaymobile.domain.model.Product

// Определяет все возможные состояния UI
sealed interface ProductListState {
    data object Loading : ProductListState // Загрузка данных
    data class Success(val products: List<Product>) : ProductListState // Данные успешно получены
    data class Error(val message: String) : ProductListState // Произошла ошибка
    data object Empty : ProductListState // Список товаров пуст (вариант Success, но без данных)
}