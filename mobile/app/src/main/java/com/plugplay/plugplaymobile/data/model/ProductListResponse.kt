package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO-обгортка для СПИСКУ товарів.
 * Вона очікує об'єкт {"$values": [...]}.
 */
data class ProductListResponse(


    @SerializedName("products", alternate = ["\$values"])
    val products: List<ProductDto>
)