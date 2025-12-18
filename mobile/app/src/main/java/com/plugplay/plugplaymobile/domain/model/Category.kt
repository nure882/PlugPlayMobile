package com.plugplay.plugplaymobile.domain.model

data class Category(
    val id: Int,
    val name: String,
    val parentId: Int? = null
)