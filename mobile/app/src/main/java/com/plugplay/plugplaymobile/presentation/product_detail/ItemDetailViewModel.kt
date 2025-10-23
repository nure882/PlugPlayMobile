package com.plugplay.plugplaymobile.presentation.product_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    // SavedStateHandle використовується для отримання аргументів навігації
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Отримуємо itemId, який був переданий через NavHost
    // Використовуємо .get<String>("itemId") замість checkNotNull, оскільки це більш типово для Compose
    private val itemId: String = savedStateHandle.get<String>("itemId") ?: ""

    private val _state = MutableStateFlow(ItemDetailState())
    val state: StateFlow<ItemDetailState> = _state

    init {
        // Завантажуємо дані одразу при створенні ViewModel, тільки якщо itemId не пустий
        if (itemId.isNotEmpty()) {
            loadItemDetails()
        } else {
            _state.update { it.copy(isLoading = false, error = "ID товару не знайдено.") }
        }
    }

    private fun loadItemDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Викликаємо функцію з оновленого ProductRepository
            repository.getProductById(itemId)
                .onSuccess { item ->
                    _state.update {
                        it.copy(
                            item = item,
                            isLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Помилка завантаження товару: ${throwable.localizedMessage}"
                        )
                    }
                }
        }
    }
}
