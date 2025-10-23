package com.plugplay.plugplaymobile.data.repository

import android.R.attr.thumbnail
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.model.Item // НОВИЙ ІМПОРТ
import com.plugplay.plugplaymobile.domain.repository.ProductRepository
import kotlinx.coroutines.delay
import javax.inject.Inject


class MockProductRepositoryImpl @Inject constructor() : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        delay(500) // Імітація завантаження
        // Повертаємо мок-дані для списку
        return Result.success(
            listOf(
                Product(
                    "1", "Контролер Alpha", "12000.00 грн",
                    image = "res/mipmap-hdpi/ic_launcher.webp"
                ),
                Product(
                    "2", "Мікшер Beta", "25000.00 грн",
                    image = "res/mipmap-hdpi/ic_launcher.webp"
                ),
                Product(
                    "3", "Навушники Gamma", "3500.00 грн",
                    image = "res/mipmap-hdpi/ic_launcher.webp"
                )
            )
        )
    }

    // [ДОДАНО] Реалізація нової функції getProductById для мок-репозиторію
    override suspend fun getProductById(itemId: String): Result<Item> {
        delay(500) // Імітація затримки

        // Повертаємо мок-об'єкт Item
        return Result.success(
            Item(
                id = itemId,
                name = "PlugPlay Pro Mixer $itemId (MOCK)",
                description = "Це мок-дані з MockProductRepositoryImpl. Професійний DJ-мікшер із вбудованим звуковим інтерфейсом. Ідеально для тестування UI.",
                price = 25999.00,
                imageUrl = "https://plugplay.com/images/mock_mixer_$itemId.jpg",
                brand = "PlugPlay Mock",
                category = "DJ Mixer"
            )
        )
    }
}
