package com.plugplay.plugplaymobile.presentation.product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.usecase.GetProductsUseCase
import com.plugplay.plugplaymobile.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase
) : ViewModel() {

    private val _originalProducts = MutableStateFlow<List<Product>>(emptyList())

    private val _currentCategoryId = MutableStateFlow<Int?>(null)
    val currentCategoryId: StateFlow<Int?> = _currentCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    private val _maxPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()

    private val _isPriceSortAscending = MutableStateFlow<Boolean?>(null)
    val isPriceSortAscending: StateFlow<Boolean?> = _isPriceSortAscending.asStateFlow()

    private val _isFilterModalVisible = MutableStateFlow(false)
    val isFilterModalVisible: StateFlow<Boolean> = _isFilterModalVisible.asStateFlow()

    // [ИСПРАВЛЕНО] Используем combine для 6 потоков.
    // При передаче > 5 аргументов combine возвращает Array<Any?>.
    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ProductListState> = combine(
        _originalProducts,
        _minPrice,
        _maxPrice,
        _isPriceSortAscending,
        _currentCategoryId,
        _searchQuery
    ) { args ->
        // Распаковываем аргументы вручную
        val products = args[0] as List<Product>
        val min = args[1] as? Double
        val max = args[2] as? Double
        val isAscending = args[3] as? Boolean
        val catId = args[4] as? Int
        val query = args[5] as String

        // Логика загрузки
        if (products.isEmpty() && (catId != null || query.isNotEmpty())) {
            return@combine ProductListState.Loading
        }

        // Фильтрация (локальная)
        val priceFilteredList = products.filter { product ->
            val price = product.price
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

        // Если список пуст, но мы не фильтруем (стартовый экран)
        if (products.isEmpty() && catId == null && query.isEmpty()) {
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
            _originalProducts.value = emptyList() // Сброс для показа загрузки

            val query = _searchQuery.value
            val categoryId = _currentCategoryId.value

            val result = if (query.isNotBlank()) {
                searchProductsUseCase(query)
            } else {
                getProductsUseCase(categoryId = categoryId)
            }

            result.onSuccess { products ->
                _originalProducts.update { products }
            }.onFailure { error ->
                _originalProducts.update { emptyList() }
                println("Error loading products: ${error.message}")
            }
        }
    }

    fun setCategoryFilter(categoryId: Int) {
        val newFilter = if (_currentCategoryId.value == categoryId) null else categoryId

        _searchQuery.value = "" // Сброс поиска
        _currentCategoryId.value = newFilter

        loadProducts()
    }

    fun search(query: String) {
        if (query == _searchQuery.value) return

        _currentCategoryId.value = null // Сброс категории
        _searchQuery.value = query

        loadProducts()
    }

    fun clearSearch() {
        if (_searchQuery.value.isNotEmpty()) {
            _searchQuery.value = ""
            loadProducts()
        }
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