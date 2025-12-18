package com.plugplay.plugplaymobile.domain.model

data class AttributeGroup(
    val id: Int,
    val name: String,
    val options: List<AttributeOption>
)

data class AttributeOption(
    val value: String,
    val display: String
)