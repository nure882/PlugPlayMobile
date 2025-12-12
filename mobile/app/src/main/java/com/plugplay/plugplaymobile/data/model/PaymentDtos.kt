package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class LiqPayInitResponse(
    @SerializedName("data") val data: String,
    @SerializedName("signature") val signature: String
)