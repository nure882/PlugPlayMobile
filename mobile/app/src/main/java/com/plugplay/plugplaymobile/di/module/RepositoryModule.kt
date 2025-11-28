package com.plugplay.plugplaymobile.di.module

// [НОВИЙ ІМПОРТ] Імпортуємо мокову реалізацію
import com.plugplay.plugplaymobile.data.repository.MockAuthRepositoryImpl
import com.plugplay.plugplaymobile.data.repository.ProductRepositoryImpl
import com.plugplay.plugplaymobile.data.repository.MockCartRepositoryImpl // [НОВИЙ ІМПОРТ]
import com.plugplay.plugplaymobile.domain.repository.AuthRepository
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import com.plugplay.plugplaymobile.domain.repository.CartRepository // [НОВИЙ ІМПОРТ]
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
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        mockAuthRepositoryImpl: MockAuthRepositoryImpl
    ): AuthRepository

    // [ДОДАНО] Биндинг CartRepository
    @Binds
    @Singleton
    abstract fun bindCartRepository(
        mockCartRepositoryImpl: MockCartRepositoryImpl
    ): CartRepository

    /*
    // [ВИМКНЕНО] Поки що вимикаємо реальну реалізацію
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    */
}