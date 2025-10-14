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

    val authToken: Flow<String?> = _authToken
    val isLoggedIn: Flow<Boolean> = _isLoggedIn

    suspend fun saveToken(token: String) {
        delay(50) // Имитация записи
        _authToken.value = token
        _isLoggedIn.value = true
        println("MOCK DS: Token saved.")
    }

    suspend fun clearToken() {
        delay(50) // Имитация очистки
        _authToken.value = null
        _isLoggedIn.value = false
        println("MOCK DS: Token cleared.")
    }

    // TODO: Реализовать логику с настоящим PreferenceDataStore или EncryptedSharedPreferences
}
