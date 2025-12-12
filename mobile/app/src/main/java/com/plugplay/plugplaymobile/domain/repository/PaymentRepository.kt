package com.plugplay.plugplaymobile.domain.repository

import com.plugplay.plugplaymobile.data.model.LiqPayInitResponse

interface PaymentRepository {
    suspend fun getLiqPayData(orderId: Int): Result<LiqPayInitResponse>
}