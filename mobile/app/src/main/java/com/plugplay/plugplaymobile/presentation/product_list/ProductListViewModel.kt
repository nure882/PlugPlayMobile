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

    private val _originalProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _currentCategoryId = MutableStateFlow<Int?>(null)
    val currentCategoryId: StateFlow<Int?> = _currentCategoryId.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    private val _maxPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()

    private val _isPriceSortAscending = MutableStateFlow<Boolean?>(null)
    val isPriceSortAscending: StateFlow<Boolean?> = _isPriceSortAscending.asStateFlow()

    private val _isFilterModalVisible = MutableStateFlow(false)
    val isFilterModalVisible: StateFlow<Boolean> = _isFilterModalVisible.asStateFlow()

    val state: StateFlow<ProductListState> = combine(
        _originalProducts,
        _minPrice,
        _maxPrice,
        _isPriceSortAscending,
        _currentCategoryId
    ) { products, min, max, isAscending, _ ->
        if (_originalProducts.value.isEmpty() && _currentCategoryId.value != null) {
            return@combine ProductListState.Loading
        }

        // [ОНОВЛЕНО] Используем product.price напрямую, без парсинга строк
        val priceFilteredList = products.filter { product ->
            val price = product.price // Теперь это Double

            val minMatch = min == null || price >= (min ?: 0.0)
            val maxMatch = max == null || price <= (max ?: Double.MAX_VALUE)

            minMatch && maxMatch
        }

        val finalSortedList = if (isAscending != null) {
            priceFilteredList.sortedWith(compareBy { it.price }).let { sorted ->
                if (isAscending == false) sorted.reversed() else sorted
            }
        } else {
            priceFilteredList
        }

        if (products.isEmpty() && _currentCategoryId.value == null) {
            ProductListState.Idle
        } else {
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

    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase.invoke(categoryId = _currentCategoryId.value)
                .onSuccess { products ->
                    _originalProducts.update { products }
                }
                .onFailure { error ->
                    _originalProducts.update { emptyList() }
                    println("Error loading products: ${error.message}")
                }
        }
    }

    fun setCategoryFilter(categoryId: Int) {
        val newFilter = if (_currentCategoryId.value == categoryId) {
            null
        } else {
            categoryId
        }
        _currentCategoryId.update { newFilter }
        _originalProducts.update { emptyList() }
        loadProducts()
    }

    fun toggleFilterModal() {
        _isFilterModalVisible.update { !it }
    }

    fun applyFilters(minPrice: Double? = null, maxPrice: Double? = null, isSortAscending: Boolean? = null) {
        _minPrice.update { minPrice }
        _maxPrice.update { maxPrice }
        _isPriceSortAscending.update { isSortAscending }
        _isFilterModalVisible.update { false }
    }
}