package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName


data class ItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("imageUrls") val imageUrls: List<String>,
    @SerializedName("isAvailable") val isAvailable: Boolean = true,
    @SerializedName("brand") val brand: String,
    @SerializedName("category") val category: String,
    @SerializedName("stockQuantity") val stockQuantity: Int,
)