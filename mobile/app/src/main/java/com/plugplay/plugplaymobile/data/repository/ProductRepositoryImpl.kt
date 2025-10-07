package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.model.toDomainList
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        // runCatching обрабатывает сетевые ошибки (нет соединения, таймаут и т.д.)
        return runCatching {
            // 1. Получаем DTO с сервера
            val dtoList = apiService.getProductListRemote()
            // 2. Преобразуем DTO в Domain Models
            dtoList.toDomainList()
        }
    }
}