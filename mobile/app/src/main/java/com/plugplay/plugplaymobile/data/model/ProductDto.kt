package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("description") val description: String?,
    @SerializedName("stockQuantity") val stockQuantity: Int?,
    @SerializedName("pictureUrls") val pictureUrls: List<String>?,
    @SerializedName("category") val category: CategoryDto?,


    @SerializedName("reviews") val reviews: List<ReviewDto>?
)