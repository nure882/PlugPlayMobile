package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import java.lang.Exception

class MockAuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val _isLoggedIn = MutableStateFlow(false)
    private val mockAuthData = AuthData("mock_jwt_token_12345", 42)

    // [ДОДАНО] Стан для зберігання ID користувача в моку
    private val _userId = MutableStateFlow<Int?>(null)

    private var currentMockUserProfile = UserProfile(
        id = "42",
        firstName = "John",
        lastName = "Doe",
        email = "test@plugplay.com",
        phoneNumber = "+380123456789"
    )

    override fun getAuthStatus(): Flow<Boolean> = _isLoggedIn.asStateFlow()

    // [ДОДАНО] Реалізація нової функції
    override fun getUserId(): Flow<Int?> = _userId.asStateFlow()

    override suspend fun login(email: String, password: String): Result<AuthData> {
        delay(800L)

        return if (email == "test@plugplay.com" && password == "123456") {
            _isLoggedIn.value = true
            _userId.value = mockAuthData.userId // [ОНОВЛЕНО] Зберігаємо ID
            Result.success(mockAuthData)
        } else if (email.startsWith("error")) {
            Result.failure(Exception("Невірний логін або пароль."))
        } else {
            _isLoggedIn.value = true
            _userId.value = mockAuthData.userId // [ОНОВЛЕНО] Зберігаємо ID
            Result.success(mockAuthData)
        }
    }

    override suspend fun register(firstName: String, lastName: String, phoneNumber: String, email: String, password: String): Result<Unit> {
        delay(1200L)
        // Обновляем мок-профиль при регистрации для имитации свежих данных
        currentMockUserProfile = currentMockUserProfile.copy(
            id = mockAuthData.userId.toString(),
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email
        )
        _isLoggedIn.value = true
        _userId.value = mockAuthData.userId // [ОНОВЛЕНО] Зберігаємо ID
        return Result.success(Unit)
    }

    override suspend fun saveAuthData(authData: AuthData) {
        println("MOCK: AuthData сохранен: token=${authData.token}, userId=${authData.userId}")
        // В реальному житті це робить LocalDataSource, тут імітуємо
        _isLoggedIn.value = true
        _userId.value = authData.userId // [ОНОВЛЕНО] Зберігаємо ID
    }

    override suspend fun logout() {
        delay(500L)
        _isLoggedIn.value = false
        _userId.value = null // [ОНОВЛЕНО] Очищуємо ID
        println("MOCK: Користувач вийшов.")
    }

    override suspend fun getProfile(): Result<UserProfile> {
        delay(500L)
        return Result.success(currentMockUserProfile)
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String?,
        newPassword: String?
    ): Result<UserProfile> {
        delay(800L)

        if (newPassword != null && currentPassword == null) {
            return Result.failure(Exception("Потрібно вказати поточний пароль для зміни."))
        }

        currentMockUserProfile = currentMockUserProfile.copy(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email
        )
        return Result.success(currentMockUserProfile)
    }
}