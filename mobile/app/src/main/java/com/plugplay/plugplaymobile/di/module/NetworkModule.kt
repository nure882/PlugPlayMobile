package com.plugplay.plugplaymobile.di.module

import com.plugplay.plugplaymobile.data.remote.ShopApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ВИПРАВЛЕНО: Встановлено IP-адресу, отриману з логів (192.168.0.111)
    // Використовуємо HTTP/5298. Переконайтеся, що ви дозволили Cleartext Traffic в Android Manifest!
    private const val BASE_URL = "http://192.168.0.111:5298/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideShopApiService(retrofit: Retrofit): ShopApiService {
        return retrofit.create(ShopApiService::class.java)
    }
}
