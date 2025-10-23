package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<AuthData>

    // [ВИПРАВЛЕНО] Реєстрація більше не повертає AuthData
    suspend fun register(firstName: String, lastName: String, phoneNumber: String, email: String, password: String): Result<Unit> // <-- ЗМІНЕНО ТУТ

    suspend fun saveAuthData(authData: AuthData)

    suspend fun logout()

    fun getAuthStatus(): Flow<Boolean>

    suspend fun getProfile(): Result<UserProfile>

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String? = null,
        newPassword: String? = null
    ): Result<UserProfile>
}