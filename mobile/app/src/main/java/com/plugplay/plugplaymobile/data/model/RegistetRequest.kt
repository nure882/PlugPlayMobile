package com.plugplay.plugplaymobile.data.model

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val password: String
)