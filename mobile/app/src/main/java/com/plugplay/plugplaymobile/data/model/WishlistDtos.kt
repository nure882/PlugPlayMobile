package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

// Ответ на GET /api/WishList
data class WishlistItemDto(
    @SerializedName("id") val id: Int,        // ID записи в базе (потребуется для удаления)
    @SerializedName("userId") val userId: Int,
    @SerializedName("productId") val productId: Int
)

// Ответ на POST /api/WishList/{productId}
data class AddWishlistItemResponse(
    @SerializedName("itemId") val itemId: Int
)