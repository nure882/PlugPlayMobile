package com.plugplay.plugplaymobile.data.remote

import com.plugplay.plugplaymobile.data.model.LoginResponseDto
import com.plugplay.plugplaymobile.data.model.LoginRequestDto
import com.plugplay.plugplaymobile.data.model.RegisterRequestDto
import com.plugplay.plugplaymobile.data.model.GoogleSignInRequestDto
import com.plugplay.plugplaymobile.data.model.ProductDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ShopApiService {

    // Функция для получения списка товаров
    @GET("products")
    suspend fun getProductListRemote(): List<ProductDto>

    // 💡 [НОВЫЙ] Вход по логину/паролю (соответствует POST /api/Auth/login)
    @POST("Auth/login")
    suspend fun loginRemote(@Body request: LoginRequestDto): LoginResponseDto

    // 💡 [НОВЫЙ] Регистрация (соответствует POST /api/Auth/register)
    @POST("Auth/register")
    suspend fun registerRemote(@Body request: RegisterRequestDto): Unit // Бэкенд возвращает Ok (200) без тела

    // 💡 [НОВЫЙ] Вход через Google (соответствует POST /api/Auth/google)
    @POST("Auth/google")
    suspend fun googleSignInRemote(@Body request: GoogleSignInRequestDto): LoginResponseDto

    // TODO: Добавить эндпоинты для refresh_token и product_list (если потребуется)
}
