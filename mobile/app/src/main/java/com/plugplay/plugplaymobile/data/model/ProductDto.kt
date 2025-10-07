package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("product_id") val id: Int,
    @SerializedName("title") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("image_url") val imageUrl: String
    // Добавьте другие поля, необходимые для вашего API
)