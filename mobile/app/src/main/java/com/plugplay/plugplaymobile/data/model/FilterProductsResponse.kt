package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO для відповіді на ендпоінт фільтрації /api/Products/filter/{categoryId}.
 * Відповідає структурі C# DTO PlugPlay.Api.Dto.Product.FilterProductsResponse.
 */
data class FilterProductsResponse(
    @SerializedName("products")
    val products: List<ProductDto>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("pageSize")
    val pageSize: Int
)