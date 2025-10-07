package com.plugplay.plugplaymobile.domain.model

// Чистая модель для передачи токена и ID пользователя
data class AuthData(
    val token: String,
    val userId: Int
)