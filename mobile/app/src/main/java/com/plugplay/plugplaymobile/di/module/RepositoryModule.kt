package com.plugplay.plugplaymobile.di.module

import com.plugplay.plugplaymobile.data.repository.MockAuthRepositoryImpl // 💡 Импорт заглушки
import com.plugplay.plugplaymobile.data.repository.MockProductRepositoryImpl // 💡 Импорт заглушки
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // 💡 БИНДИНГ НА ЗАГЛУШКУ ТОВАРОВ
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        // Используем класс-заглушку
        mockProductRepositoryImpl: MockProductRepositoryImpl
    ): ProductRepository

    // 💡 БИНДИНГ НА ЗАГЛУШКУ АУТЕНТИФИКАЦИИ
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        // Используем класс-заглушку
        mockAuthRepositoryImpl: MockAuthRepositoryImpl
    ): AuthRepository
}