package com.plugplay.plugplaymobile.presentation.product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.usecase.GetProductsUseCase // ВИПРАВЛЕНО: правильний імпорт
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // [ДОДАНО]
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase // ВИПРАВЛЕНО: правильна назва класу
) : ViewModel() {

    private val _state = MutableStateFlow<ProductListState>(ProductListState.Idle)
    val state: StateFlow<ProductListState> = _state.asStateFlow()

    // [ДОДАНО] Стан для CategoryId. null означає "всі товари"
    private val _currentCategoryId = MutableStateFlow<Int?>(null)
    val currentCategoryId: StateFlow<Int?> = _currentCategoryId.asStateFlow() // Додано для UI

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.value = ProductListState.Loading

            // ВИПРАВЛЕНО: викликаємо Use Case з поточним CategoryId
            getProductsUseCase.invoke(_currentCategoryId.value) // <--- ЗМІНА ТУТ
                .onSuccess { products ->
                    _state.value = ProductListState.Success(products)
                }
                .onFailure { error ->
                    _state.value = ProductListState.Error(error.message ?: "Невідома помилка завантаження товарів.")
                }
        }
    }

    // [ДОДАНО] Функція для встановлення фільтра/перемикання
    fun setCategoryFilter(categoryId: Int) {
        // Логіка для перемикання фільтра
        val newFilter = if (_currentCategoryId.value == categoryId) {
            null // Вимкнути фільтр, якщо натиснуто ту саму кнопку
        } else {
            categoryId
        }

        // Оновлюємо фільтр і перезавантажуємо продукти
        _currentCategoryId.update { newFilter }
        loadProducts()
    }
}