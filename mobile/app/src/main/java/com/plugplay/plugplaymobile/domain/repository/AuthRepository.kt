package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.AuthData
import kotlinx.coroutines.flow.Flow

/**
 * Контракт для роботи з аутентифікацією.
 * Він визначає, які операції доступні для слоя Domain (Use Cases).
 */
interface AuthRepository {

    // Вхід користувача
    suspend fun login(email: String, password: String): Result<AuthData>

    // Реєстрація користувача
    suspend fun register(name: String, email: String, password: String): Result<AuthData>

    // Збереження токена після успішного входу/реєстрації
    suspend fun saveAuthToken(token: String)

    // Вихід користувача (додаємо для логаута)
    suspend fun logout()

    /**
     * Потік, що відображає поточний статус аутентифікації.
     * @return true, якщо користувач авторизований, інакше false.
     */
    fun getAuthStatus(): Flow<Boolean>
}
