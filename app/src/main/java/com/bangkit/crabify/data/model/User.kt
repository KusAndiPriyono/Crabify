package com.bangkit.crabify.data.model

import com.google.gson.annotations.SerializedName

data class User(
    var id: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
)
