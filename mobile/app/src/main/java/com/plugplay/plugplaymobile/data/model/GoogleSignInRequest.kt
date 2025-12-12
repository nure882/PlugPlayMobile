package com.plugplay.plugplaymobile.data.model

import com.google.gson.annotations.SerializedName

data class GoogleSignInRequest(
    @SerializedName("idToken")
    val googleIdToken: String
)