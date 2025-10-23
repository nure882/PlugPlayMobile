package com.plugplay.plugplaymobile.domain.usecase

import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthData> {
        return repository.login(email, password)
            // 💡 Важная логика: если вход успешен, сохраняем токен
            .onSuccess { authData ->
                // [ВИПРАВЛЕНО] Зберігаємо повні AuthData (токен + ID)
                repository.saveAuthData(authData)
            }
    }
}