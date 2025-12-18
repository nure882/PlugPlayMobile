package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class AttributeRequestDto(
    @SerializedName("productIds") val productIds: List<Int>,

    @SerializedName("selectedAttrsIds") val selectedAttrsIds: List<Int> = emptyList()
)

data class AttributeGroupDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("dataType") val dataType: String?,
    @SerializedName("unit") val unit: String?,
    @SerializedName("productAttributeDtos") val attributes: List<ProductAttributeDto>?
)

data class ProductAttributeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("attributeId") val attributeId: Int,
    @SerializedName("productId") val productId: Int,
    @SerializedName("strValue") val strValue: String?,
    @SerializedName("numValue") val numValue: Double?
)