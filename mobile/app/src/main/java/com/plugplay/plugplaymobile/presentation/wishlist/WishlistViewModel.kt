package com.plugplay.plugplaymobile.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.usecase.GetWishlistUseCase
import com.plugplay.plugplaymobile.domain.usecase.ToggleWishlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WishlistState(
    val items: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val getWishlistUseCase: GetWishlistUseCase,
    private val toggleWishlistUseCase: ToggleWishlistUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WishlistState())
    val state: StateFlow<WishlistState> = _state.asStateFlow()

    init {
        loadWishlist()
    }

    fun loadWishlist() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // Теперь это берет данные из WishlistLocalDataSource через репозиторий
            getWishlistUseCase()
                .onSuccess { products ->
                    _state.update { it.copy(items = products, isLoading = false) }
                }
        }
    }

    fun removeFromWishlist(productId: String) {
        val id = productId.toIntOrNull() ?: return
        viewModelScope.launch {
            toggleWishlistUseCase.remove(id)
                .onSuccess {
                    loadWishlist()
                }
                .onFailure {
                    // Fail silently or show snackbar
                }
        }
    }
}
