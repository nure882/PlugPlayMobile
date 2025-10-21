package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO-обгортка для СПИСКУ товарів.
 * Вона очікує об'єкт {"$values": [...]}.
 */
data class ProductListResponse(

    // Kapt бачить "products", а Gson під час виконання знаходить "$values"
    @SerializedName("products", alternate = ["\$values"])
    val products: List<ProductDto>
)