package com.plugplay.plugplaymobile.data.repository

import android.util.Log
import com.plugplay.plugplaymobile.data.local.CartLocalDataSource
import com.plugplay.plugplaymobile.data.model.OrderItemRequestDto
import com.plugplay.plugplaymobile.data.model.PlaceOrderRequest
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.repository.OrderRepository
import com.plugplay.plugplaymobile.domain.model.Order
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.plugplay.plugplaymobile.data.model.toDomain as toOrderDomain

class OrderRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService,
    private val cartLocalDataSource: CartLocalDataSource,
    private val productRepository: ProductRepository
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
            if (userId == null) {
                throw Exception("Guest checkout is not supported. Please log in.")
            }

            if (address.id == null) {
                throw Exception("Please select a saved address to place an order.")
            }

            val orderItemsDto = cartItems.map {
                OrderItemRequestDto(
                    productId = it.productId.toInt(),
                    quantity = it.quantity
                )
            }

            val request = PlaceOrderRequest(
                userId = userId,
                deliveryAddressId = address.id,
                deliveryMethod = deliveryMethod.id,
                paymentMethod = paymentMethod.id,
                orderItems = orderItemsDto
            )

            val response = apiService.placeOrder(request)

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.orderId
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("OrderRepo", "Error: $errorBody")
                throw Exception(errorBody ?: "Server error: ${response.code()}")
            }
        }
    }

    override suspend fun getUserOrders(userId: Int): Result<List<Order>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getUserOrders(userId)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception(errorBody ?: "Failed to fetch orders: ${response.message()}")
            }

            val ordersDto = response.body() ?: emptyList()
            var orders = ordersDto.map { it.toOrderDomain() }

            // Подгружаем актуальные данные товаров
            val productsResult = productRepository.getProducts(null)

            if (productsResult.isSuccess) {
                val productsMap = productsResult.getOrThrow().associateBy { it.id }

                orders = orders.map { order ->
                    val enrichedItems = order.orderItems.map { item ->
                        val product = productsMap[item.productId]
                        if (product != null) {
                            item.copy(
                                productName = product.title,
                                imageUrl = if (item.imageUrl.contains("placeholder")) product.image else item.imageUrl,
                                price = product.price // [ИСПРАВЛЕНО] Теперь цена берется из актуального товара
                            )
                        } else {
                            item
                        }
                    }
                    order.copy(orderItems = enrichedItems)
                }
            }

            orders
        }
    }

    override suspend fun cancelOrder(orderId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.cancelOrder(orderId)
            if (response.isSuccessful) Unit else throw Exception("Failed to cancel")
        }
    }
}