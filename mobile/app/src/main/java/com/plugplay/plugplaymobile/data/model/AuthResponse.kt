package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

// Соответствует UserDto.cs
data class UserDto(
    @SerializedName("Id") val id: Int,
    @SerializedName("Email") val email: String,
    @SerializedName("FirstName") val firstName: String,
    @SerializedName("LastName") val lastName: String
)

// Соответствует LoginResponse.cs
data class LoginResponseDto(
    @SerializedName("Token") val token: String,
    @SerializedName("RefreshToken") val refreshToken: String,
    @SerializedName("Expiration") val expiration: String, // DateTime
    @SerializedName("User") val user: UserDto
)
