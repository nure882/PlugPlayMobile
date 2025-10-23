package com.plugplay.plugplaymobile.data.model

// Переконайтеся, що id має тип Int, як того вимагає AuthData.userId
data class UserDto(
    val id: Int, // ЗМІНЕНО/ВИПРАВЛЕНО: Тип має бути Int
    val userName: String,
    val email: String
)
