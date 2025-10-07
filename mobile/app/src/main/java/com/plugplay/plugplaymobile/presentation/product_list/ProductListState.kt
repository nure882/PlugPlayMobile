package com.plugplay.plugplaymobile.presentation.product_list

import com.plugplay.plugplaymobile.domain.model.Product

sealed interface ProductListState {
    data object Idle : ProductListState
    data object Loading : ProductListState
    data class Success(val products: List<Product>) : ProductListState
    data class Error(val message: String) : ProductListState
}
