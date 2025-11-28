package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.local.AuthLocalDataSource
import com.plugplay.plugplaymobile.data.model.* import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.lang.Exception

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthData> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = LoginRequest(email, password)
                val response = apiService.login(request)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.toAuthData()
                } else {
                    val errorBody = response.errorBody()?.string()
                    throw Exception(errorBody ?: "Login failed (Check API): ${response.message()}")
                }
            }
        }
    }

    override suspend fun register(firstName: String, lastName: String, phoneNumber: String, email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = RegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    password = password
                )
                val response = apiService.register(request)

                if (response.isSuccessful) {
                    Unit
                } else {
                    val errorBody = response.errorBody()?.string()
                    throw Exception(errorBody ?: "Registration failed: ${response.message()}")
                }
            }
        }
    }

    override suspend fun saveAuthData(authData: AuthData) {
        localDataSource.saveAuthData(authData.token, authData.userId)
    }

    override suspend fun logout() {
        localDataSource.clearToken()
    }

    override fun getAuthStatus(): Flow<Boolean> {
        return localDataSource.isLoggedIn
    }

    // [ДОДАНО] Реалізація відсутнього методу
    override fun getUserId(): Flow<Int?> {
        return localDataSource.userId
    }

    // [ВИПРАВЛЕНО] Використовуємо userId як Int
    override suspend fun getProfile(): Result<UserProfile> {
        val userId = localDataSource.userId.first()
        if (userId == null) {
            return Result.failure(Exception("User not logged in or ID not found."))
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.toDomain()
                } else {
                    val errorBody = response.errorBody()?.string()
                    throw Exception(errorBody ?: "Failed to fetch profile: ${response.message()}")
                }
            }
        }
    }

    // [ВИПРАВЛЕНО] Використовуємо userId як Int
    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String?,
        newPassword: String?
    ): Result<UserProfile> {
        val userId = localDataSource.userId.first()
        if (userId == null) {
            return Result.failure(Exception("User not logged in or ID not found."))
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                val request = UpdateProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
                val response = apiService.updateProfile(userId, request)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.toDomain()
                } else {
                    val errorBody = response.errorBody()?.string()
                    throw Exception(errorBody ?: "Failed to update profile: ${response.message()}")
                }
            }
        }
    }
}
