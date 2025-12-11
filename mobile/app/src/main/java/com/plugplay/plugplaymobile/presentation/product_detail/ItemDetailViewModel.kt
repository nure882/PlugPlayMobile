package com.plugplay.plugplaymobile.presentation.product_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import com.plugplay.plugplaymobile.domain.usecase.GetWishlistUseCase
import com.plugplay.plugplaymobile.domain.usecase.ToggleWishlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemDetailState(
    val item: Item? = null,
    val attributes: List<AttributeGroup> = emptyList(),
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val getWishlistUseCase: GetWishlistUseCase,
    private val toggleWishlistUseCase: ToggleWishlistUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String = savedStateHandle.get<String>("itemId") ?: ""
    private val _state = MutableStateFlow(ItemDetailState())
    val state: StateFlow<ItemDetailState> = _state

    // [NEW] Статус авторизации
    val isLoggedIn: StateFlow<Boolean> = authRepository.getAuthStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        if (itemId.isNotEmpty()) {
            loadItemDetails()
            checkIfFavorite()
        } else {
            _state.update { it.copy(isLoading = false, error = "Item ID not found") }
        }
    }

    private fun checkIfFavorite() {
        viewModelScope.launch {
            if (authRepository.getUserId().first() != null) {
                getWishlistUseCase()
                    .onSuccess { wishlist ->
                        val isFav = wishlist.any { it.id == itemId }
                        _state.update { it.copy(isFavorite = isFav) }
                    }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.getUserId().first() != null
            if (!isLoggedIn) return@launch

            val isCurrentlyFavorite = _state.value.isFavorite
            val idInt = itemId.toIntOrNull() ?: return@launch

            _state.update { it.copy(isFavorite = !isCurrentlyFavorite) }

            if (isCurrentlyFavorite) {
                toggleWishlistUseCase.remove(idInt)
            } else {
                toggleWishlistUseCase.add(idInt)
            }
        }
    }

    private fun loadItemDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getProductById(itemId)
                .onSuccess { item ->
                    _state.update { it.copy(item = item, isLoading = false) }
                    item.categoryId?.let { catId ->
                        loadAttributes(catId, item.id.toInt())
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message) }
                }
        }
    }

    private fun loadAttributes(categoryId: Int, productId: Int) {
        viewModelScope.launch {
            repository.getProductAttributes(categoryId, productId)
                .onSuccess { attrs -> _state.update { it.copy(attributes = attrs) } }
        }
    }
}