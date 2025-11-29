package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName
import com.plugplay.plugplaymobile.data.model.CategoryDto

/**
 * DTO для ОДИНОЧНОГО товару, оновлене згідно з новим JSON-форматом.
 */
data class ProductDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("price")
    val price: Double?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("stockQuantity")
    val stockQuantity: Int?,

    // [НОВЕ ПОЛЕ] Зображення тепер приходять як прямий масив URL
    @SerializedName("pictureUrls")
    val pictureUrls: List<String>?, // <-- ЗМІНА

    @SerializedName("category")
    val category: CategoryDto?,

    // [ДОДАНО] Поле для повного списку відгуків
    @SerializedName("reviews")
    val reviews: List<ReviewDto>?
)