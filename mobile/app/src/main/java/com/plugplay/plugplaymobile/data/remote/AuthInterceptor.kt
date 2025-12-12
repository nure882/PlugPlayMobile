package com.plugplay.plugplaymobile.data.remote

import com.plugplay.plugplaymobile.data.local.AuthLocalDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authLocalDataSource: AuthLocalDataSource
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Отримуємо токен синхронно (runBlocking допустимий тут, оскільки це мережевий потік)
        val token = runBlocking {
            authLocalDataSource.authToken.first()
        }

        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}