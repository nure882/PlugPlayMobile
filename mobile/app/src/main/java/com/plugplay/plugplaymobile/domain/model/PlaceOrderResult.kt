package com.plugplay.plugplaymobile.domain.model

import com.plugplay.plugplaymobile.data.model.LiqPayInitResponse

data class PlaceOrderResult(
    val orderId: Int,
    val paymentData: LiqPayInitResponse?
)