package com.plugplay.plugplaymobile.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object LiqPayHelper {
    fun generatePaymentLink(data: String, signature: String): String {
        val encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8.toString())
        val encodedSignature = URLEncoder.encode(signature, StandardCharsets.UTF_8.toString())

        return "https://www.liqpay.ua/api/3/checkout?data=$encodedData&signature=$encodedSignature"
    }
}