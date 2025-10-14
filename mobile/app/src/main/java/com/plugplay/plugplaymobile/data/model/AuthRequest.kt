package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

// Соответствует ReqisterRequest.cs на бэкенде
data class RegisterRequestDto(
    @SerializedName("Email") val email: String,
    @SerializedName("Password") val password: String,
    @SerializedName("FirstName") val firstName: String,
    @SerializedName("LastName") val lastName: String,
    @SerializedName("PhoneNumber") val phoneNumber: String? = null // Опционально
)

// Соответствует LoginRequest.cs на бэкенде
data class LoginRequestDto(
    @SerializedName("Email") val email: String,
    @SerializedName("Password") val password: String
)

// Соответствует GoogleSignInRequest.cs
data class GoogleSignInRequestDto(
    @SerializedName("IdToken") val idToken: String
)
