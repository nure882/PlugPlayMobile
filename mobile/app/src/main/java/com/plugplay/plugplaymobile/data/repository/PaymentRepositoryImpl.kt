package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.model.LiqPayInitResponse
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.repository.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService
) : PaymentRepository {

    override suspend fun getLiqPayData(orderId: Int): Result<LiqPayInitResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.initPayment(orderId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to init payment: ${response.code()} ${response.message()}")
            }
        }
    }
}