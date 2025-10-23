package com.plugplay.plugplaymobile.di.module

// [НОВИЙ ІМПОРТ] Імпортуємо мокову реалізацію
import com.plugplay.plugplaymobile.data.repository.MockAuthRepositoryImpl
import com.plugplay.plugplaymobile.data.repository.AuthRepositoryImpl
import com.plugplay.plugplaymobile.data.repository.ProductRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        // Тут залишаємо реальний репозиторій, бо список товарів працює
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        // [ВИПРАВЛЕНО] Вказуємо мокову реалізацію
        mockAuthRepositoryImpl: MockAuthRepositoryImpl
    ): AuthRepository

    /*
    // [ВИМКНЕНО] Поки що вимикаємо реальну реалізацію
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    */
}