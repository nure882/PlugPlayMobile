package com.plugplay.plugplaymobile.data.model

import com.plugplay.plugplaymobile.domain.model.AttributeGroup
import com.plugplay.plugplaymobile.domain.model.Item
import com.plugplay.plugplaymobile.domain.model.Product
import com.plugplay.plugplaymobile.domain.model.Review
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.plugplay.plugplaymobile.data.model.ReviewDto
import com.plugplay.plugplaymobile.domain.model.AttributeOption

private const val PLACEHOLDER_URL = "https://example.com/placeholder.jpg"

fun ProductDto.toDomain(): Product {
    val firstImage = this.pictureUrls?.firstOrNull() ?: PLACEHOLDER_URL
    return Product(
        id = this.id.toString(),
        title = this.name ?: "No Name",
        priceValue = String.format("%.2f ₴", this.price ?: 0.0),
        image = firstImage,
        price = this.price ?: 0.0
    )
}

fun List<ProductDto>.toDomainList(): List<Product> = this.map { it.toDomain() }

fun ProductDto.toDomainItem(): Item {
    val imageUrls = this.pictureUrls
        .orEmpty()
        .filter { it.isNotBlank() }
        .ifEmpty { listOf(PLACEHOLDER_URL) }

    // Map reviews
    val domainReviews = this.reviews?.map { dto ->
        val dateStr = try {
            // Simple date parsing, can be improved with proper DateTimeFormatter for ISO strings
            dto.createdAt.take(10)
        } catch (e: Exception) {
            ""
        }

        Review(
            id = dto.id,
            userName = "${dto.user?.firstName ?: ""} ${dto.user?.lastName ?: ""}".trim().ifBlank { "User" },
            rating = dto.rating,
            comment = dto.comment ?: "",
            date = dateStr
        )
    } ?: emptyList()

    val avgRating = if (domainReviews.isNotEmpty()) {
        domainReviews.map { it.rating }.average()
    } else 0.0

    return Item(
        id = this.id.toString(),
        name = this.name ?: "No Name",
        description = this.description ?: "No description available.",
        price = this.price ?: 0.0,
        imageUrls = imageUrls,
        isAvailable = (this.stockQuantity ?: 0) > 0,
        brand = this.category?.name ?: "N/A",
        category = this.category?.name ?: "N/A",
        categoryId = this.category?.id, // Capture category ID
        averageRating = avgRating,
        reviewCount = domainReviews.size,
        reviews = domainReviews.sortedByDescending { it.date } // Sort by date descending
    )
}

// Attribute Mapper Logic
fun AttributeGroupDto.toDomain(): AttributeGroup? {
    if (this.attributes.isNullOrEmpty()) return null

    val options = this.attributes.mapNotNull { attr ->
        // 1. Формируем "сырое" значение для фильтра (как на фронте)
        val rawValueForFilter: String? = when {
            this.dataType == "bool" -> if (attr.numValue?.toInt() == 1) "true" else "false"
            this.dataType?.contains("num", true) == true -> attr.numValue?.toString() // Просто число "16.0"
            else -> attr.strValue // Просто строка
        }

        // 2. Формируем красивое значение для отображения
        val displayValue: String? = when {
            this.dataType == "bool" -> if (attr.numValue?.toInt() == 1) "Yes" else "No"
            else -> {
                // Берем значение + юнит (если есть)
                val raw = if (this.dataType?.contains("num", true) == true) attr.numValue else attr.strValue
                if (raw != null) "${raw}${if (!this.unit.isNullOrBlank()) " ${this.unit}" else ""}" else null
            }
        }

        if (rawValueForFilter.isNullOrBlank() || displayValue.isNullOrBlank()) return@mapNotNull null

        AttributeOption(value = rawValueForFilter, display = displayValue)
    }.distinctBy { it.value } // Убираем дубликаты по значению фильтра

    if (options.isEmpty()) return null

    return AttributeGroup(
        id = this.id,
        name = this.name,
        options = options
    )
}