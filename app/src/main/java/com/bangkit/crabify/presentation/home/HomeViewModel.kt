package com.bangkit.crabify.presentation.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.crabify.data.model.SensorDataValue
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.SensorRepository
import com.bangkit.crabify.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val sensorRepository: SensorRepository) :
    ViewModel() {

    private val _sensor = MutableLiveData<UiState<List<SensorDataValue>>>()
    val sensor: MutableLiveData<UiState<List<SensorDataValue>>>
        get() = _sensor

    fun getSensorData(user: User?) {
        _sensor.value = UiState.Loading
        sensorRepository.getSensorData(user) {
            _sensor.value = it
            Log.d("Sensor", it.toString())
        }
    }
}