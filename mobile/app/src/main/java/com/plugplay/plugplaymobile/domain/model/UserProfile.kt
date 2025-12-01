package com.plugplay.plugplaymobile.domain.model

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    // [NEW] Додаємо список адрес
    val addresses: List<UserAddress> = emptyList()
)

// [NEW] Доменна модель адреси
data class UserAddress(
    val id: Int? = null,
    val apartments: String?,
    val house: String?,
    val street: String,
    val city: String
)