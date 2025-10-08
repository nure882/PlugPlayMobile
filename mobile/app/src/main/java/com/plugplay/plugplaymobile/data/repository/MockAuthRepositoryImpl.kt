package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MockAuthRepositoryImpl @Inject constructor() : AuthRepository {

    // 💡 Стан для імітації аутентифікації
    private val _isLoggedIn = MutableStateFlow(false)
    private val mockAuthData = AuthData("mock_jwt_token_12345", 42)

    override fun getAuthStatus(): Flow<Boolean> = _isLoggedIn.asStateFlow()

    override suspend fun login(email: String, password: String): Result<AuthData> {
        delay(800L)

        return if (email == "test@plugplay.com" && password == "123456") {
            _isLoggedIn.value = true // 💡 Успішний вхід
            Result.success(mockAuthData)
        } else if (email.startsWith("error")) {
            Result.failure(Exception("Невірний логін або пароль."))
        } else {
            _isLoggedIn.value = true // Успішний вхід по замовчуванню
            Result.success(mockAuthData)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<AuthData> {
        delay(1200L)
        _isLoggedIn.value = true // 💡 Успішна реєстрація
        return Result.success(mockAuthData)
    }

    override suspend fun saveAuthToken(token: String) {
        println("MOCK: Токен сохранен: $token")
    }

    override suspend fun logout() {
        delay(500L)
        _isLoggedIn.value = false // 💡 Вихід
        println("MOCK: Користувач вийшов.")
    }
}
