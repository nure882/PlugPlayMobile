package com.plugplay.plugplaymobile.data.remote

import com.plugplay.plugplaymobile.data.model.*
import com.plugplay.plugplaymobile.data.model.ProductDto
import com.plugplay.plugplaymobile.data.model.ProductListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.lang.Void // [ВИПРАВЛЕНО] Правильний імпорт

interface ShopApiService {

    @GET("api/Products/all")
    suspend fun getProducts(): List<ProductDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // [ВИПРАВЛЕНО] Тип Void тепер імпортовано з java.lang
    @POST("api/Auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @GET("api/UserInfo/{id}")
    suspend fun getProfile(@Path("id") userId: String): Response<ProfileResponse>

    @PUT("api/UserInfo/{id}")
    suspend fun updateProfile(@Path("id") userId: String, @Body request: UpdateProfileRequest): Response<ProfileResponse>

    @GET("api/Products/{id}")
    suspend fun getProductById(@Path("id") itemId: String): Response<ProductDto>
}