package com.plugplay.plugplaymobile.data.remote

import com.plugplay.plugplaymobile.data.model.ProductDto
import retrofit2.http.GET

interface ShopApiService {

    // Функция для получения списка товаров
    @GET("products")
    suspend fun getProductListRemote(): List<ProductDto>

    // Здесь будут другие методы: @POST("cart/add"), @GET("user/profile") и т.д.
}