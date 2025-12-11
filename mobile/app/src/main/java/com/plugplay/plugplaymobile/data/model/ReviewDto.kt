package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class ReviewDto(
    @SerializedName("id") val id: Int,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("userDto") val user: ReviewUserDto?
)

data class ReviewUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?
)