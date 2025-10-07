package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockAuthRepositoryImpl @Inject constructor() : AuthRepository {

    // Имитация успешного токена и ID пользователя
    private val mockAuthData = AuthData("mock_jwt_token_12345", 42)

    override suspend fun login(email: String, password: String): Result<AuthData> {
        delay(800L)

        return if (email == "test@plugplay.com" && password == "123456") {
            // Успешный вход
            Result.success(mockAuthData)
        } else if (email.startsWith("error")) {
            // Имитация ошибки
            Result.failure(Exception("Невірний логін або пароль."))
        } else {
            // Успешный вход по умолчанию
            Result.success(mockAuthData)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<AuthData> {
        delay(1200L)
        // Имитация успешной регистрации и автоматического входа
        return Result.success(mockAuthData)
    }

    // Заглушка для сохранения токена
    override suspend fun saveAuthToken(token: String) {
        println("MOCK: Токен сохранен: $token")
    }
}