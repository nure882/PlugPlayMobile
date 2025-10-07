package com.plugplay.plugplaymobile.di.module

import com.plugplay.plugplaymobile.data.remote.ShopApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Модуль будет жить столько же, сколько и приложение
object NetworkModule {

    // 💡 Базовый URL для вашего API
    private const val BASE_URL = "https://your-shop-backend.com/api/v1/"

    // Предоставляет OkHttpClient (для логгирования запросов)
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Уровень логгирования: BODY показывает тело запроса и ответа (полезно для отладки)
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Предоставляет Retrofit-клиент
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Предоставляет реализацию ShopApiService
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ShopApiService {
        return retrofit.create(ShopApiService::class.java)
    }
}