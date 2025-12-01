package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.model.UserAddress // <--- НОВИЙ ІМПОРТ
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<AuthData>

    // [ВИПРАВЛЕНО] Реєстрація більше не повертає AuthData
    suspend fun register(firstName: String, lastName: String, phoneNumber: String, email: String, password: String): Result<Unit>

    suspend fun saveAuthData(authData: AuthData)

    fun getUserId(): Flow<Int?>

    suspend fun logout()

    fun getAuthStatus(): Flow<Boolean>

    suspend fun getProfile(): Result<UserProfile>

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String? = null,
        newPassword: String? = null,
        addresses: List<UserAddress> = emptyList() // <--- ЗМІНА
    ): Result<UserProfile>
}