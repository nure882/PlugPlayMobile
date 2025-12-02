package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.local.CartLocalDataSource
import com.plugplay.plugplaymobile.data.model.OrderAddressDto
import com.plugplay.plugplaymobile.data.model.PlaceOrderRequest
import com.plugplay.plugplaymobile.data.model.PlaceOrderResponse
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.lang.Exception

class OrderRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService,
    private val cartLocalDataSource: CartLocalDataSource
) : OrderRepository {
    override suspend fun placeOrder(
        userId: Int?,
        cartItems: List<CartItem>,
        totalPrice: Double,
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod,
        address: UserAddress,
        customerName: String,
        customerEmail: String,
        customerPhone: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val addressDto = OrderAddressDto(
                city = address.city,
                street = address.street,
                house = address.house ?: "",
                apartments = address.apartments
            )

            val request = PlaceOrderRequest(
                userId = userId,
                deliveryMethod = deliveryMethod.id,
                paymentMethod = paymentMethod.id,
                address = addressDto,
                customerName = customerName,
                customerEmail = customerEmail,
                customerPhone = customerPhone
            )

            val response = apiService.placeOrder(request)

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.orderId
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception(errorBody ?: "Failed to place order: ${response.message()}")
            }
        }
    }
}