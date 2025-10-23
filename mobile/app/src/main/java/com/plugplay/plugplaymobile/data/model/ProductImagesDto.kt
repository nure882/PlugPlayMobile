package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO-обгортка для об'єкта "productImages".
 * Вона очікує {"$values": [...]}.
 */
data class ProductImagesDto(

    // Той самий трюк з alternate
    @SerializedName("images", alternate = ["\$values"])
    val images: List<ImageDto>?
)

/**
 * DTO для окремого об'єкта зображення в масиві "productImages.$values".
 */
data class ImageDto(
    @SerializedName("id")
    val id: Int? = null,

    // TODO: Уточніть назву цього поля ("imageUrl" чи "url") у бекендера
    @SerializedName("imageUrl")
    val imageUrl: String?
)