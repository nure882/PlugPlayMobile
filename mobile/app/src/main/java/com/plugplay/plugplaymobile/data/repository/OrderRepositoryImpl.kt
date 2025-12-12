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
    private val productRepository: ProductRepository // Використовуємо для підтягування назв
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
                orderItems = orderItemsDto
            )

            val response = apiService.placeOrder(request)

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.orderId
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

            // 1. Збираємо унікальні ID товарів з усіх замовлень
            val productIds = ordersDto.flatMap { it.orderItems }
                .map { it.productId }
                .distinct()

            // 2. Паралельно завантажуємо деталі ТІЛЬКИ цих товарів (швидко)
            val productsMap = productIds.map { id ->
                async {
                    // Якщо товар не знайдено (видалений), повернеться null
                    id to productRepository.getProductById(id.toString()).getOrNull()
                }
            }.awaitAll().toMap()

            // 3. Формуємо список замовлень, підставляючи реальні дані
            ordersDto.map { orderDto ->

                // Збагачуємо позиції замовлення
                val enrichedItems = orderDto.orderItems.map { itemDto ->
                    val product = productsMap[itemDto.productId]

                    // Назва: якщо є в DTO - беремо її, якщо ні - з каталогу, інакше заглушка
                    val realName = itemDto.productName?.takeIf { it.isNotBlank() }
                        ?: product?.name
                        ?: "Product #${itemDto.productId}"

                    // Ціна: якщо в історії 0 - беремо актуальну
                    val realPrice = if (itemDto.price == null || itemDto.price == 0.0) {
                        product?.price ?: 0.0
                    } else {
                        itemDto.price
                    }

                    // Картинка
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

                // Збираємо Order
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