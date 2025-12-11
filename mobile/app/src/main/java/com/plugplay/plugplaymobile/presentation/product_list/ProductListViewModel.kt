package com.plugplay.plugplaymobile.presentation.product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import com.plugplay.plugplaymobile.domain.usecase.GetProductsUseCase
import com.plugplay.plugplaymobile.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())

    // Фильтры
    private val _currentCategoryId = MutableStateFlow<Int?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _minPrice = MutableStateFlow<Double?>(null)
    private val _maxPrice = MutableStateFlow<Double?>(null)

    // Сортировка (значения: 'price-asc', 'price-desc', 'newest')
    private val _sortOption = MutableStateFlow("price-asc")

    // Выбранные атрибуты: Map<GroupId, Set<Value>>
    private val _selectedAttributes = MutableStateFlow<Map<Int, Set<String>>>(emptyMap())

    // Доступные для фильтрации группы атрибутов
    private val _availableAttributeGroups = MutableStateFlow<List<AttributeGroup>>(emptyList())

    private val _isFilterModalVisible = MutableStateFlow(false)
    val isFilterModalVisible: StateFlow<Boolean> = _isFilterModalVisible.asStateFlow()

    // UI State
    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ProductListState> = combine(
        _products,
        _currentCategoryId,
        _searchQuery,
        _availableAttributeGroups,
        _sortOption // Триггер
    ) { args ->
        val products = args[0] as List<Product>
        val catId = args[1] as? Int
        val query = args[2] as String

        if (products.isEmpty() && (catId != null || query.isNotEmpty())) {
            ProductListState.Loading
        } else if (products.isEmpty() && catId == null && query.isEmpty()) {
            ProductListState.Idle
        } else {
            ProductListState.Success(products)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductListState.Loading
    )

    val currentSortOption: StateFlow<String> = _sortOption.asStateFlow()
    val currentMinPrice: StateFlow<Double?> = _minPrice.asStateFlow()
    val currentMaxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()
    val selectedAttributes: StateFlow<Map<Int, Set<String>>> = _selectedAttributes.asStateFlow()
    val availableAttributeGroups: StateFlow<List<AttributeGroup>> = _availableAttributeGroups.asStateFlow()
    val currentCategoryId: StateFlow<Int?> = _currentCategoryId.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _products.value = emptyList() // Сброс для показа загрузки

            val query = _searchQuery.value
            val categoryId = _currentCategoryId.value

            // 1. Загрузка товаров (Поиск или Фильтр)
            val result = if (query.isNotBlank()) {
                searchProductsUseCase(query)
            } else {
                val filterString = buildFilterString(_selectedAttributes.value)
                getProductsUseCase(
                    categoryId = categoryId,
                    minPrice = _minPrice.value,
                    maxPrice = _maxPrice.value,
                    sort = _sortOption.value,
                    filterString = filterString
                )
            }

            result.onSuccess { products ->
                _products.value = products

                // 2. [ИСПРАВЛЕНО] Загрузка атрибутов для фильтрации
                if (products.isNotEmpty()) {
                    // Если категория не выбрана (null), используем "Магическое число" с фронтенда (Int.MAX_VALUE),
                    // которое сервер понимает как "Все категории" для поиска атрибутов.
                    val attributesCategoryId = categoryId ?: 2147483647
                    val productIds = products.mapNotNull { it.id.toIntOrNull() }

                    loadAvailableAttributes(attributesCategoryId, productIds)
                } else {
                    _availableAttributeGroups.value = emptyList()
                }

            }.onFailure {
                println("Error loading products: ${it.message}")
                _products.value = emptyList()
                _availableAttributeGroups.value = emptyList()
            }
        }
    }

    private fun loadAvailableAttributes(categoryId: Int, productIds: List<Int>) {
        viewModelScope.launch {
            productRepository.getAttributesForFilter(categoryId, productIds)
                .onSuccess { groups ->
                    _availableAttributeGroups.value = groups
                }
                .onFailure {
                    // Fail silently
                }
        }
    }

    private fun buildFilterString(attrs: Map<Int, Set<String>>): String? {
        if (attrs.isEmpty()) return null

        return attrs.entries.joinToString(";") { (id, values) ->
            val safeValues = values.joinToString(",") { it.replace("[,;:]".toRegex(), "") }
            "$id:$safeValues"
        }.ifBlank { null }
    }

    // --- Public Methods ---

    fun setCategoryFilter(categoryId: Int) {
        val newFilter = if (_currentCategoryId.value == categoryId) null else categoryId

        // Сброс при смене категории
        _currentCategoryId.value = newFilter
        _searchQuery.value = ""
        _minPrice.value = null
        _maxPrice.value = null
        _selectedAttributes.value = emptyMap()

        loadProducts()
    }

    fun search(query: String) {
        if (query == _searchQuery.value) return
        _currentCategoryId.value = null
        _searchQuery.value = query
        // Сброс фильтров при новом поиске
        _minPrice.value = null
        _maxPrice.value = null
        _selectedAttributes.value = emptyMap()

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

    fun applyFilters(
        minPrice: Double?,
        maxPrice: Double?,
        sortOption: String,
        attributes: Map<Int, Set<String>>
    ) {
        _minPrice.value = minPrice
        _maxPrice.value = maxPrice
        _sortOption.value = sortOption
        _selectedAttributes.value = attributes

        _isFilterModalVisible.value = false
        loadProducts()
    }
}