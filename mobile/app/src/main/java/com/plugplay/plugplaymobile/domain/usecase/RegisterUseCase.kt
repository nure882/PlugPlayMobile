package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<AuthData> {
        // Логика регистрации. Также сохраняем токен при успехе.
        return repository.register(name, email, password)
            .onSuccess { authData ->
                repository.saveAuthToken(authData.token)
            }
    }
}