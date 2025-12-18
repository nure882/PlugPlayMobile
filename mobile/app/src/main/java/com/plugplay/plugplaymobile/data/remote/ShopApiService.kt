package com.plugplay.plugplaymobile.data.remote

import com.plugplay.plugplaymobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ShopApiService {

    @GET("api/Products/all")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/products/filter/{categoryId}")
    suspend fun filterProducts(
        @Path("categoryId") categoryId: Int,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): Response<FilterProductsResponse>

    @GET("api/Products/{id}")
    suspend fun getProductById(@Path("id") itemId: Int): Response<ProductDto>

    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/Auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleSignInRequest): Response<LoginResponse>

    @POST("api/Auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @GET("api/UserInfo/{id}")
    suspend fun getProfile(@Path("id") userId: Int): Response<ProfileResponse>

    @PUT("api/UserInfo/{id}")
    suspend fun updateProfile(@Path("id") userId: Int, @Body request: UpdateProfileRequest): Response<ProfileResponse>

    @GET("api/Cart/{userId}")
    suspend fun getCartItems(@Path("userId") userId: Int): Response<List<CartItemDto>>

    @POST("api/Cart")
    suspend fun addToCart(@Body request: CreateCartItemDto): Response<Void>

    @PUT("api/Cart/quantity")
    suspend fun updateQuantity(@Body request: UpdateCartItemQuantityDto): Response<Void>

    @DELETE("api/Cart/{itemId}")
    suspend fun deleteCartItem(@Path("itemId") cartItemId: Int): Response<Void>

    @DELETE("api/Cart/clear/{userId}")
    suspend fun clearCart(@Path("userId") userId: Int): Response<Void>

    @POST("api/Order")
    suspend fun placeOrder(@Body request: PlaceOrderRequest): Response<PlaceOrderResponse>

    @GET("api/Order/user/{userId}")
    suspend fun getUserOrders(@Path("userId") userId: Int): Response<List<OrderDto>>

    @PUT("api/Order/cancel/{orderId}")
    suspend fun cancelOrder(@Path("orderId") orderId: Int): Response<Void>

    @GET("api/Products/search/{query}")
    suspend fun searchProducts(
        @Path("query") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): List<ProductDto>

    @POST("api/Products/attribute/{categoryId}")
    suspend fun getAttributeGroups(
        @Path("categoryId") categoryId: Int,
        @Body request: AttributeRequestDto
    ): Response<List<AttributeGroupDto>>

    @GET("api/WishList")
    suspend fun getUserWishList(): Response<List<WishlistItemDto>>

    @POST("api/WishList/{productId}")
    suspend fun addItemToWishList(@Path("productId") productId: Int): Response<AddWishlistItemResponse>

    @DELETE("api/WishList/{itemId}")
    suspend fun removeItemFromWishList(@Path("itemId") itemId: Int): Response<Unit>

    @GET("api/WishList/{prodId}")
    suspend fun isProductInWishList(@Path("prodId") prodId: Int): Response<Boolean>


}