package com.plugplay.plugplaymobile.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthLocalDataSource @Inject constructor() {

    // 💡 Имитация DataStore: хранение токена и статуса авторизации
    private val _authToken = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)
    private val _userId = MutableStateFlow<Int?>(null) // [ДОДАНО] Зберігаємо ID

    val authToken: Flow<String?> = _authToken
    val isLoggedIn: Flow<Boolean> = _isLoggedIn
    val userId: Flow<Int?> = _userId // [ДОДАНО] Flow для ID

    // [ВИПРАВЛЕНО] Зберігаємо токен та ID
    suspend fun saveAuthData(token: String, userId: Int) {
        delay(50) // Имитация записи
        _authToken.value = token
        _userId.value = userId
        _isLoggedIn.value = true
        println("MOCK DS: Token and UserId saved.")
    }

    // [ВИПРАВЛЕНО] Очищуємо все
    suspend fun clearToken() {
        delay(50) // Имитация очистки
        _authToken.value = null
        _userId.value = null
        _isLoggedIn.value = false
        println("MOCK DS: Token and UserId cleared.")
    }

    // TODO: Реализовать логику с настоящим PreferenceDataStore или EncryptedSharedPreferences
}