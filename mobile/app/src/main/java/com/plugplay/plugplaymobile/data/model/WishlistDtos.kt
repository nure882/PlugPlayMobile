package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName


data class WishlistItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("productId") val productId: Int
)


data class AddWishlistItemResponse(
    @SerializedName("itemId") val itemId: Int
)