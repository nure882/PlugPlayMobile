package com.plugplay.plugplaymobile.di.module

// [НОВИЙ ІМПОРТ] Імпортуємо реальні реалізації
import com.plugplay.plugplaymobile.data.repository.AuthRepositoryImpl
import com.plugplay.plugplaymobile.data.repository.ProductRepositoryImpl
import com.plugplay.plugplaymobile.data.repository.CartRepositoryImpl
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import com.plugplay.plugplaymobile.domain.repository.CartRepository
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
        // Використовуємо реальну реалізацію
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        // [ОНОВЛЕНО] Використовуємо реальну реалізацію
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    // [ОНОВЛЕНО] Використовуємо реальну реалізацію
    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository
}