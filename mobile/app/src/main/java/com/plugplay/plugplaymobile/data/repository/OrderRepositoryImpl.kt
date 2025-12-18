package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.local.CartLocalDataSource
import com.plugplay.plugplaymobile.data.model.OrderItemRequestDto
import com.plugplay.plugplaymobile.data.model.PlaceOrderRequest
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.CartItem
import com.plugplay.plugplaymobile.domain.model.DeliveryMethod
import com.plugplay.plugplaymobile.domain.model.Order
import com.plugplay.plugplaymobile.domain.model.OrderItem
import com.plugplay.plugplaymobile.domain.model.OrderStatus
import com.plugplay.plugplaymobile.domain.model.PaymentMethod
import com.plugplay.plugplaymobile.domain.model.PaymentStatus
import com.plugplay.plugplaymobile.domain.model.PlaceOrderResult // Додано
import com.plugplay.plugplaymobile.domain.model.UserAddress
import com.plugplay.plugplaymobile.domain.repository.OrderRepository
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
        customerPhone: String,
        description: String,
    ): Result<PlaceOrderResult> = withContext(Dispatchers.IO) { // [FIX] Тип Result
        runCatching {
            if (userId == null) {
                throw Exception("Guest checkout is not supported. Please log in.")
            }

            val addressId = address.id ?: throw Exception("Please select a saved address to place an order.")

            val orderItemsDto = cartItems.map {
                OrderItemRequestDto(
                    productId = it.productId.toInt(),
                    quantity = it.quantity
                )
            }

            val request = PlaceOrderRequest(
                userId = userId,
                deliveryAddressId = addressId,
                deliveryMethod = deliveryMethod.id,
                paymentMethod = paymentMethod.id,
                orderItems = orderItemsDto,
                description = description,
            )

            val response = apiService.placeOrder(request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // [FIX] Повертаємо об'єкт з даними, а не просто ID
                PlaceOrderResult(
                    orderId = body.orderId,
                    paymentData = body.paymentData
                )
            } else {
                val errorBody = response.errorBody()?.string()
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

            val productIds = ordersDto.flatMap { it.orderItems }
                .map { it.productId }
                .distinct()

            val productsMap = productIds.map { id ->
                async {
                    id to productRepository.getProductById(id.toString()).getOrNull()
                }
            }.awaitAll().toMap()

            ordersDto.map { orderDto ->
                val enrichedItems = orderDto.orderItems.map { itemDto ->
                    val product = productsMap[itemDto.productId]

                    val realName = itemDto.productName?.takeIf { it.isNotBlank() }
                        ?: product?.name
                        ?: "Product #${itemDto.productId}"

                    val realPrice = if (itemDto.price == null || itemDto.price == 0.0) {
                        product?.price ?: 0.0
                    } else {
                        itemDto.price
                    }

                    val realImage = itemDto.productImageUrl?.takeIf { it.isNotBlank() && !it.contains("placeholder") }
                        ?: product?.imageUrls?.firstOrNull()
                        ?: ""

                    OrderItem(
                        productId = itemDto.productId.toString(),
                        quantity = itemDto.quantity,
                        productName = realName,
                        price = realPrice,
                        imageUrl = realImage
                    )
                }

                Order(
                    id = orderDto.id,
                    userId = orderDto.userId,
                    orderDate = orderDto.orderDate,
                    totalAmount = orderDto.totalAmount,
                    deliveryAddressId = orderDto.deliveryAddressId,
                    status = OrderStatus.entries.find { it.id == orderDto.status } ?: OrderStatus.Created,
                    deliveryMethod = DeliveryMethod.entries.find { it.id == orderDto.deliveryMethod } ?: DeliveryMethod.Courier,
                    paymentMethod = PaymentMethod.entries.find { it.id == orderDto.paymentMethod } ?: PaymentMethod.Card,
                    paymentStatus = PaymentStatus.entries.find { it.id == orderDto.paymentStatus } ?: PaymentStatus.NotPaid,
                    orderItems = enrichedItems
                )
            }
        }
    }

    override suspend fun cancelOrder(orderId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.cancelOrder(orderId)
            if (response.isSuccessful) Unit else throw Exception("Failed to cancel")
        }
    }
}