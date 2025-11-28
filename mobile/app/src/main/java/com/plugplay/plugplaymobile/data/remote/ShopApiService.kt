package com.plugplay.plugplaymobile.data.remote

import com.plugplay.plugplaymobile.data.model.*
import retrofit2.Response
import retrofit2.http.*
import java.lang.Void

interface ShopApiService {

    // Product API
    @GET("api/Products/all")
    suspend fun getProducts(): List<ProductDto>

    // [ОНОВЛЕНО] Product ID тепер Int
    @GET("api/Products/{id}")
    suspend fun getProductById(@Path("id") itemId: Int): Response<ProductDto>

    // Auth API
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/Auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    // UserInfo API
    // [ОНОВЛЕНО] UserInfo API використовує id (Int)
    @GET("api/UserInfo/{id}")
    suspend fun getProfile(@Path("id") userId: Int): Response<ProfileResponse>

    // [ОНОВЛЕНО] UserInfo API використовує id (Int)
    @PUT("api/UserInfo/{id}")
    suspend fun updateProfile(@Path("id") userId: Int, @Body request: UpdateProfileRequest): Response<ProfileResponse>

    // ------------------ CART API ------------------

    // GET /api/Cart/{userId}
    @GET("api/Cart/{userId}")
    suspend fun getCartItems(@Path("userId") userId: Int): Response<List<CartItemDto>>

    // POST /api/Cart
    @POST("api/Cart")
    suspend fun addToCart(@Body request: CreateCartItemDto): Response<Void>

    // PUT /api/Cart/quantity
    @PUT("api/Cart/quantity")
    suspend fun updateQuantity(@Body request: UpdateCartItemQuantityDto): Response<Void>

    // DELETE /api/Cart/{itemId} (itemId тут - це cartItemId)
    @DELETE("api/Cart/{itemId}")
    suspend fun deleteCartItem(@Path("itemId") cartItemId: Int): Response<Void>

    // DELETE /api/Cart/clear/{userId}
    @DELETE("api/Cart/clear/{userId}")
    suspend fun clearCart(@Path("userId") userId: Int): Response<Void>
}