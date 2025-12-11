package com.plugplay.plugplaymobile.presentation.product_detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Клас станів для ItemDetailScreen.
 */
data class ItemDetailState(
    val item: Item? = null,
    val attributes: List<AttributeGroup> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String = savedStateHandle.get<String>("itemId") ?: ""
    private val _state = MutableStateFlow(ItemDetailState())
    val state: StateFlow<ItemDetailState> = _state

    init {
        if (itemId.isNotEmpty()) {
            loadItemDetails()
        } else {
            _state.update { it.copy(isLoading = false, error = "Item ID not found") }
        }
    }

    private fun loadItemDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getProductById(itemId)
                .onSuccess { item ->
                    _state.update { it.copy(item = item, isLoading = false) }

                    Log.d("ItemDetailVM", "Item loaded: ${item.name}, CatID: ${item.categoryId}")

                    // Загружаем атрибуты, только если есть ID категории
                    item.categoryId?.let { catId ->
                        loadAttributes(catId, item.id.toInt())
                    } ?: Log.w("ItemDetailVM", "Category ID is null, attributes won't load")
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message) }
                }
        }
    }

    private fun loadAttributes(categoryId: Int, productId: Int) {
        viewModelScope.launch {
            repository.getProductAttributes(categoryId, productId)
                .onSuccess { attrs ->
                    Log.d("ItemDetailVM", "Attributes loaded: ${attrs.size} groups")
                    _state.update { it.copy(attributes = attrs) }
                }
                .onFailure { e ->
                    Log.e("ItemDetailVM", "Error in attributes flow", e)
                }
        }
    }
}
