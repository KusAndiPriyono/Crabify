package com.bangkit.crabify.domain.repository

import com.bangkit.crabify.data.model.SensorDataValue
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.utils.UiState

interface SensorRepository {
    fun getSensorData(user: User?, result: (UiState<List<SensorDataValue>>) -> Unit)
}