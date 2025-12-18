package com.plugplay.plugplaymobile.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthLocalDataSource @Inject constructor() {


    private val _authToken = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)
    private val _userId = MutableStateFlow<Int?>(null)

    val authToken: Flow<String?> = _authToken
    val isLoggedIn: Flow<Boolean> = _isLoggedIn
    val userId: Flow<Int?> = _userId


    suspend fun saveAuthData(token: String, userId: Int) {
        delay(50)
        _authToken.value = token
        _userId.value = userId
        _isLoggedIn.value = true
        println("MOCK DS: Token and UserId saved.")
    }


    suspend fun clearToken() {
        delay(50)
        _authToken.value = null
        _userId.value = null
        _isLoggedIn.value = false
        println("MOCK DS: Token and UserId cleared.")
    }


}