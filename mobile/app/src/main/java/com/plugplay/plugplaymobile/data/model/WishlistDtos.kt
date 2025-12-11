package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class ToggleWishlistRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("productId") val productId: Int
)