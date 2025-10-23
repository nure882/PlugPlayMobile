package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO для ОДИНОЧНОГО товару, що приходить у масиві "$values".
 * Поля оновлені згідно з вашим cURL-відповіддю.
 */
data class ProductDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?, // Робимо nullable

    @SerializedName("price")
    val price: Double?, // Робимо nullable

    @SerializedName("description")
    val description: String?,

    @SerializedName("stockQuantity")
    val stockQuantity: Int?,

    @SerializedName("productImages")
    val productImages: ProductImagesDto?, // Вкладений DTO

    @SerializedName("category")
    val category: CategoryDto? // Вкладений DTO
)