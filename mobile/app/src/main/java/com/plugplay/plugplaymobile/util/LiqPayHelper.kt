package com.plugplay.plugplaymobile.util

import android.util.Base64
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object LiqPayHelper {

    // ВСТАВТЕ ТУТ ВАШІ КЛЮЧІ З САЙТУ LIQPAY
    private const val PUBLIC_KEY = "sandbox_i8040093493"
    private const val PRIVATE_KEY = "sandbox_SK3tZxw51JRUXTJTntuXuWwnplle0W2dRL78EHu3"

    fun generatePaymentLink(orderId: Int, amount: Double): String {
        val params = mapOf(
            "public_key" to PUBLIC_KEY,
            "version" to 3,
            "action" to "pay",
            "amount" to amount,
            "currency" to "UAH",
            "description" to "Payment for order #$orderId",
            "order_id" to orderId.toString(),
            "language" to "uk"
            // "result_url" не обов'язковий, якщо користувач просто закриє браузер
        )

        val json = Gson().toJson(params)
        // Кодуємо дані в Base64
        val data = Base64.encodeToString(json.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        // Підписуємо дані
        val signature = createSignature(data)

        return "https://www.liqpay.ua/api/3/checkout?data=$data&signature=$signature"
    }

    private fun createSignature(data: String): String {
        val signString = PRIVATE_KEY + data + PRIVATE_KEY
        val sha1 = MessageDigest.getInstance("SHA-1")
        val hash = sha1.digest(signString.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}