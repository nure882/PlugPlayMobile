package com.plugplay.plugplaymobile.presentation.product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.usecase.GetProductListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Аннотация Hilt для ViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductListUseCase
) : ViewModel() {

    // MutableStateFlow для изменения состояния внутри ViewModel
    private val _state = MutableStateFlow<ProductListState>(ProductListState.Loading)
    // StateFlow для предоставления состояния в UI (только для чтения)
    val state: StateFlow<ProductListState> = _state.asStateFlow()

    init {
        loadProducts()
    }

    // Главная функция для загрузки данных
    fun loadProducts() {
        // Запуск асинхронной операции в области видимости ViewModel
        viewModelScope.launch {
            _state.value = ProductListState.Loading // Устанавливаем Loading

            getProductsUseCase()
                .onSuccess { products ->
                    if (products.isEmpty()) {
                        _state.value = ProductListState.Empty // Если список пуст
                    } else {
                        _state.value = ProductListState.Success(products) // Успех
                    }
                }
                .onFailure { error ->
                    // Перехват ошибки и передача сообщения в UI
                    val message = error.message ?: "Невідома помилка завантаження товарів."
                    _state.value = ProductListState.Error(message)
                }
        }
    }
}