package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName
import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.data.model.UserDto


data class UserAddressDto(
    val id: Int? = null,
    val apartments: String?,
    val house: String?,
    val street: String?,
    val city: String?
)

data class LoginResponse(
    val token: String,
    val user: UserDto
)


data class ProfileResponse(
    val id: Int,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("addresses") val addresses: List<UserAddressDto> = emptyList()
)


data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val addresses: List<UserAddressDto> = emptyList(),
    val currentPassword: String? = null,
    val newPassword: String? = null,
)


fun LoginResponse.toAuthData(): AuthData {
    return AuthData(
        token = this.token,
        userId = this.user.id
    )
}


fun ProfileResponse.toDomain(): UserProfile {
    return UserProfile(
        id = this.id.toString(),
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        addresses = this.addresses.map { it.toDomain() }
    )
}


fun UserAddressDto.toDomain(): UserAddress {
    return UserAddress(
        id = this.id,
        apartments = this.apartments,
        house = this.house,
        street = this.street ?: "",
        city = this.city ?: ""
    )
}


fun UserAddress.toDto(): UserAddressDto {
    return UserAddressDto(
        id = this.id,
        apartments = this.apartments,
        house = this.house,
        street = this.street,
        city = this.city
    )
}