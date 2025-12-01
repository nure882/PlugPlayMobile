package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.model.UserAddress // <--- НОВИЙ ІМПОРТ
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    // [ОНОВЛЕНО] Додано список адрес
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String? = null,
        newPassword: String? = null,
        addresses: List<UserAddress> = emptyList() // <--- ЗМІНА СИГНАТУРИ
    ): Result<UserProfile> {
        return repository.updateProfile(firstName, lastName, phoneNumber, email, currentPassword, newPassword, addresses)
    }
}