package com.bangkit.crabify.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SensorDataValue(
    val id: String = "",
    val name: String = "",
    val value: String = "",
    val unit: String = "",
    val image: String = "",
) : Parcelable
