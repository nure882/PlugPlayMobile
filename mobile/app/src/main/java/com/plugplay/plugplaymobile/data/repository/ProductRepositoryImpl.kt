package com.plugplay.plugplaymobile.data.repository

import com.plugplay.plugplaymobile.data.model.toDomainList
import com.plugplay.plugplaymobile.data.remote.ShopApiService
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.model.Item // НОВИЙ ІМПОРТ
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.delay // НОВИЙ ІМПОРТ
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ShopApiService
) : ProductRepository {

    // [ВИПРАВЛЕНО] Тип повернення тепер Result<List<Product>>
    override suspend fun getProducts(): Result<List<Product>> {
        return runCatching {
            val dtoList = apiService.getProductListRemote()
            // [ВИПРАВЛЕНО] Переконайтеся, що toDomainList повертає List<Product>
            dtoList.toDomainList()
        }
    }

    // [ДОДАНО] Реалізація нової функції з мок-логікою
    override suspend fun getProductById(itemId: String): Result<Item> {
        delay(500) // Імітація затримки мережі
        return Result.success(
            Item(
                id = itemId,
                name = "PlugPlay Pro Mixer $itemId",
                description = "Професійний 4-канальний DJ-мікшер із вбудованим звуковим інтерфейсом і ефектами. Незамінний для клубних виступів.",
                price = 25999.00,
                imageUrl = "https://plugplay.com/images/pro_mixer_$itemId.jpg",
                brand = "PlugPlay",
                category = "DJ Mixer"
            )
        )
    }
}
