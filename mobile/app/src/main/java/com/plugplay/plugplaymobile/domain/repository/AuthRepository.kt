package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.AuthData

/**
 * Контракт для работы с аутентификацией.
 * Он определяет, какие операции доступны для слоя Domain (Use Cases).
 */
interface AuthRepository {

    // Вход пользователя
    suspend fun login(email: String, password: String): Result<AuthData>

    // Регистрация пользователя
    suspend fun register(name: String, email: String, password: String): Result<AuthData>

    // Сохранение токена после успешного входа/регистрации
    suspend fun saveAuthToken(token: String)
}