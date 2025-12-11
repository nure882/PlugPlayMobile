package com.plugplay.plugplaymobile.domain.model

data class Review(
    val id: Int,
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String
)