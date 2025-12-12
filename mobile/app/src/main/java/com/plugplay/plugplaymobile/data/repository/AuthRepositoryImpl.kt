package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.local.AuthLocalDataSource
import com.plugplay.plugplaymobile.data.model.*
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.AuthData
import com.plugplay.plugplaymobile.domain.model.UserProfile
import com.plugplay.plugplaymobile.domain.model.UserAddress
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

    override suspend fun loginWithGoogle(googleIdToken: String): Result<AuthData> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = GoogleSignInRequest(googleIdToken)
                try {
                    val response = apiService.loginWithGoogle(request)

                    if (response.isSuccessful && response.body() != null) {
                        val authData = response.body()!!.toAuthData()
                        localDataSource.saveAuthData(authData.token, authData.userId)
                        authData
                    } else {
                        // Читаємо тіло помилки
                        val errorBody = response.errorBody()?.string()
                        // Якщо тіло пусте, формуємо своє повідомлення
                        val errorMessage = if (errorBody.isNullOrBlank()) {
                            "Login failed: Code ${response.code()} (${response.message()})"
                        } else {
                            errorBody
                        }
                        // Логуємо для дебагу
                        android.util.Log.e("AuthRepo", "API Error: $errorMessage")
                        throw Exception(errorMessage)
                    }
                } catch (e: Exception) {
                    // Ловимо мережеві помилки (наприклад, Cleartext not permitted)
                    android.util.Log.e("AuthRepo", "Network Exception", e)
                    throw Exception("Network Error: ${e.localizedMessage ?: e.javaClass.simpleName}")
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

    override fun getUserId(): Flow<Int?> {
        return localDataSource.userId
    }

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

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        currentPassword: String?,
        newPassword: String?,
        addresses: List<UserAddress>
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
                    addresses = addresses.map { it.toDto() },
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