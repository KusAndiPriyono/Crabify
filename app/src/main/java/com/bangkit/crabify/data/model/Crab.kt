package com.bangkit.crabify.data.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Crab(
    var id: String = "",
    var user_id: String = "",
    val label: MutableList<String> = arrayListOf(),
    val score: MutableList<Float> = arrayListOf(),
    val image: String = "",
    @ServerTimestamp
    val date: Date = Date(),
) : Parcelable
