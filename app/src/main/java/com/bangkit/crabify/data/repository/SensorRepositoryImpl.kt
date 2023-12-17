package com.bangkit.crabify.data.repository

import com.bangkit.crabify.data.model.SensorDataValue
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.SensorRepository
import com.bangkit.crabify.utils.UiState
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepositoryImpl @Inject constructor(private val database: FirebaseFirestore) :
    SensorRepository {
    override fun getSensorData(
        user: User?,
        result: (UiState<List<SensorDataValue>>) -> Unit
    ) {
        database.collection("sensor")
            .get()
            .addOnSuccessListener {
                val list = arrayListOf<SensorDataValue>()
                for (document in it) {
                    val sensorDataValue = document.toObject(SensorDataValue::class.java)
                    list.add(sensorDataValue)
                }
                result(UiState.Success(list))
            }
            .addOnFailureListener { exception ->
                result(UiState.Error(exception.message.toString()))
            }
    }
}


