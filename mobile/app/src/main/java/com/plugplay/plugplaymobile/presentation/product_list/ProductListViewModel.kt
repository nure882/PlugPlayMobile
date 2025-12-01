package com.plugplay.plugplaymobile.presentation.product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    // [НОВЕ] Зберігає оригінальний список продуктів, отриманий з API (відфільтрований лише за категорією)
    private val _originalProducts = MutableStateFlow<List<Product>>(emptyList())

    // Стан для CategoryId. null означає "всі товари"
    private val _currentCategoryId = MutableStateFlow<Int?>(null)
    val currentCategoryId: StateFlow<Int?> = _currentCategoryId.asStateFlow()

    // [ДОДАНО] Стан для діапазону цін
    private val _minPrice = MutableStateFlow<Double?>(null)
    private val _maxPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()

    // [НОВЕ] Стан для сортування: true=Ascending, false=Descending, null=None
    private val _isPriceSortAscending = MutableStateFlow<Boolean?>(null)
    val isPriceSortAscending: StateFlow<Boolean?> = _isPriceSortAscending.asStateFlow()

    // [ДОДАНО] Стан для видимості модального вікна фільтра
    private val _isFilterModalVisible = MutableStateFlow(false)
    val isFilterModalVisible: StateFlow<Boolean> = _isFilterModalVisible.asStateFlow()

    // [ОНОВЛЕНО] Стан, який combine оригінальні продукти, цінові фільтри та сортування
    val state: StateFlow<ProductListState> = combine(
        _originalProducts,
        _minPrice,
        _maxPrice,
        _isPriceSortAscending, // <--- ДОДАНО: Для реактивності
        _currentCategoryId
    ) { products, min, max, isAscending, _ ->
        // Проста імітація стану завантаження/помилки
        if (_originalProducts.value.isEmpty() && _currentCategoryId.value != null) {
            return@combine ProductListState.Loading
        }

        // 1. Фільтрація за ціною (Локально)
        val priceFilteredList = products.filter { product ->
            val price = extractPriceFromProduct(product)

            val minMatch = min == null || price >= (min ?: 0.0)
            val maxMatch = max == null || price <= (max ?: Double.MAX_VALUE)

            minMatch && maxMatch
        }

        // 2. Сортування за ціною (Локально)
        val finalSortedList = if (isAscending != null) {
            priceFilteredList.sortedWith(compareBy { product ->
                extractPriceFromProduct(product)
            }).let { sorted ->
                // Якщо isAscending = false (спадання), інвертуємо список
                if (isAscending == false) sorted.reversed() else sorted
            }
        } else {
            priceFilteredList
        }

        if (products.isEmpty() && _currentCategoryId.value == null) {
            ProductListState.Idle
        } else {
            // Використовуємо finalSortedList
            ProductListState.Success(finalSortedList)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductListState.Loading
    )

    init {
        loadProducts()
    }

    // Допоміжна функція для вилучення ціни з Product.priceValue
    private fun extractPriceFromProduct(product: Product): Double {
        // Product.priceValue має формат "1250.00 ₴"
        return product.priceValue
            .replace(" ₴", "")
            .replace(",", ".") // Якщо використовується кома як десятковий роздільник
            .toDoubleOrNull() ?: 0.0
    }

    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase.invoke(categoryId = _currentCategoryId.value)
                .onSuccess { products ->
                    _originalProducts.update { products } // Зберігаємо оригінальний список
                }
                .onFailure { error ->
                    _originalProducts.update { emptyList() }
                    println("Error loading products: ${error.message}")
                }
        }
    }

    // Функція для встановлення фільтра/перемикання категорії
    fun setCategoryFilter(categoryId: Int) {
        val newFilter = if (_currentCategoryId.value == categoryId) {
            null // Вимкнути фільтр, якщо натиснуто ту саму кнопку
        } else {
            categoryId
        }

        // Оновлюємо фільтр категорії та перезавантажуємо продукти з API (для категорії)
        _currentCategoryId.update { newFilter }
        _originalProducts.update { emptyList() } // Очищаємо, щоб викликати Loading
        loadProducts() // Перезавантажуємо з новим фільтром категорії
    }

    // Функції для керування модальним вікном фільтра
    fun toggleFilterModal() {
        _isFilterModalVisible.update { !it }
    }

    // [ОНОВЛЕНО] Приймаємо min/max ціни ТА сортування
    fun applyFilters(minPrice: Double? = null, maxPrice: Double? = null, isSortAscending: Boolean? = null) {
        // Зберігаємо нові значення ціни
        _minPrice.update { minPrice }
        _maxPrice.update { maxPrice }

        // Зберігаємо нове значення сортування
        _isPriceSortAscending.update { isSortAscending }

        // Просто закриваємо модальне вікно
        _isFilterModalVisible.update { false }
    }
}