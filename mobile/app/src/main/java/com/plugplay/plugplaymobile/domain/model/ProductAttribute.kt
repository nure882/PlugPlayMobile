package com.plugplay.plugplaymobile.domain.model

data class AttributeGroup(
    val id: Int,
    val name: String,
    val options: List<AttributeOption> // [ИЗМЕНЕНО] Было List<String>
)

data class AttributeOption(
    val value: String,  // Сырое значение для API (например, "16", "true")
    val display: String // Красивое значение для UI (например, "16 GB", "Yes")
)