package com.bangkit.crabify.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    var id: String = "",
    @SerializedName("fullName")
    val fullName: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("password")
    val password: String = "",
)
