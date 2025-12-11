package com.plugplay.plugplaymobile.presentation.product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import com.plugplay.plugplaymobile.domain.usecase.GetProductsUseCase
import com.plugplay.plugplaymobile.domain.usecase.GetWishlistUseCase
import com.plugplay.plugplaymobile.domain.usecase.SearchProductsUseCase
import com.plugplay.plugplaymobile.domain.usecase.ToggleWishlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val productRepository: ProductRepository,
    // [NEW] UseCases для вишлиста
    private val getWishlistUseCase: GetWishlistUseCase,
    private val toggleWishlistUseCase: ToggleWishlistUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())

    // [NEW] Список ID товаров в избранном
    private val _wishlistIds = MutableStateFlow<Set<String>>(emptySet())
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    // Фильтры
    private val _currentCategoryId = MutableStateFlow<Int?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _minPrice = MutableStateFlow<Double?>(null)
    private val _maxPrice = MutableStateFlow<Double?>(null)
    private val _sortOption = MutableStateFlow("price-asc")
    private val _selectedAttributes = MutableStateFlow<Map<Int, Set<String>>>(emptyMap())
    private val _availableAttributeGroups = MutableStateFlow<List<AttributeGroup>>(emptyList())
    private val _isFilterModalVisible = MutableStateFlow(false)

    val isFilterModalVisible: StateFlow<Boolean> = _isFilterModalVisible.asStateFlow()

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

    val currentSortOption = _sortOption.asStateFlow()
    val currentMinPrice = _minPrice.asStateFlow()
    val currentMaxPrice = _maxPrice.asStateFlow()
    val selectedAttributes = _selectedAttributes.asStateFlow()
    val availableAttributeGroups = _availableAttributeGroups.asStateFlow()
    val currentCategoryId = _currentCategoryId.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadProducts()
        loadWishlist() // [NEW] Загружаем вишлист при старте
    }

    // [NEW] Загрузка вишлиста (только для авторизованных)
    private fun loadWishlist() {
        viewModelScope.launch {
            if (authRepository.getUserId().first() != null) {
                getWishlistUseCase()
                    .onSuccess { wishlist ->
                        _wishlistIds.value = wishlist.map { it.id }.toSet()
                    }
            }
        }
    }

    // [NEW] Тоггл лайка
    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            val isLoggedIn = authRepository.getUserId().first() != null
            if (!isLoggedIn) return@launch // Или показать сообщение "Войдите"

            val currentIds = _wishlistIds.value
            val isFavorite = currentIds.contains(productId)
            val idInt = productId.toIntOrNull() ?: return@launch

            // Оптимистичное обновление UI
            if (isFavorite) {
                _wishlistIds.value = currentIds - productId
                toggleWishlistUseCase.remove(idInt)
            } else {
                _wishlistIds.value = currentIds + productId
                toggleWishlistUseCase.add(idInt)
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _products.value = emptyList()

            val query = _searchQuery.value
            val categoryId = _currentCategoryId.value

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
                if (products.isNotEmpty()) {
                    val attributesCategoryId = categoryId ?: 2147483647
                    val productIds = products.mapNotNull { it.id.toIntOrNull() }
                    loadAvailableAttributes(attributesCategoryId, productIds)
                } else {
                    _availableAttributeGroups.value = emptyList()
                }
            }.onFailure {
                _products.value = emptyList()
                _availableAttributeGroups.value = emptyList()
            }
        }
    }

    private fun loadAvailableAttributes(categoryId: Int, productIds: List<Int>) {
        viewModelScope.launch {
            productRepository.getAttributesForFilter(categoryId, productIds)
                .onSuccess { groups -> _availableAttributeGroups.value = groups }
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

    fun applyFilters(min: Double?, max: Double?, sort: String, attrs: Map<Int, Set<String>>) {
        _minPrice.value = min
        _maxPrice.value = max
        _sortOption.value = sort
        _selectedAttributes.value = attrs
        _isFilterModalVisible.value = false
        loadProducts()
    }
}