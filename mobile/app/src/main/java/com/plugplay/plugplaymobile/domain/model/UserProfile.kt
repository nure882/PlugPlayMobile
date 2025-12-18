package com.plugplay.plugplaymobile.domain.model

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,

    val addresses: List<UserAddress> = emptyList()
)


data class UserAddress(
    val id: Int? = null,
    val apartments: String?,
    val house: String?,
    val street: String,
    val city: String
)