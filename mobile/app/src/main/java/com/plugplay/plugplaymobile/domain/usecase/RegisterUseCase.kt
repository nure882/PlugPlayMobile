package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    // [ВИПРАВЛЕНО] Повертає Result<Unit> і не зберігає токен
    suspend operator fun invoke(firstName: String, lastName: String, phoneNumber: String, email: String, password: String): Result<Unit> {
        return repository.register(firstName, lastName, phoneNumber, email, password)
        // [ВИДАЛЕНО] .onSuccess { ... } - більше немає AuthData для збереження
    }
}