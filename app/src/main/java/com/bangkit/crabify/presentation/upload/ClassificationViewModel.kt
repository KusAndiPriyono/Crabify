package com.bangkit.crabify.presentation.upload

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.crabify.data.model.Crab
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.CrabRepository
import com.bangkit.crabify.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassificationViewModel @Inject constructor(private val crabRepository: CrabRepository) :
    ViewModel() {

    private val _crabs = MutableLiveData<UiState<List<Crab>>>()
    val crab: LiveData<UiState<List<Crab>>>
        get() = _crabs

    private val _addCrab = MutableLiveData<UiState<Pair<Crab, String>>>()
    val addCrab: LiveData<UiState<Pair<Crab, String>>>
        get() = _addCrab

    private val _updateCrab = MutableLiveData<UiState<String>>()
    val updateCrab: LiveData<UiState<String>>
        get() = _updateCrab

    private val _deleteCrab = MutableLiveData<UiState<String>>()
    val deleteCrab: LiveData<UiState<String>>
        get() = _deleteCrab

    fun getCrabs(user: User?) {
        _crabs.value = UiState.Loading
        crabRepository.getCrabs(user) {
            _crabs.value = it
        }
    }

    fun addCrab(crab: Crab) {
        _addCrab.value = UiState.Loading
        crabRepository.addCrab(crab) {
            _addCrab.value = it
        }
    }

    fun updateCrab(crab: Crab) {
        _updateCrab.value = UiState.Loading
        crabRepository.updateCrab(crab) {
            _updateCrab.value = it
        }
    }

    fun deleteCrab(crab: Crab) {
        _deleteCrab.value = UiState.Loading
        crabRepository.deleteCrab(crab) {
            _deleteCrab.value = it
        }
    }

    fun uploadSingleFile(fileUris: Uri, onResult: (UiState<Uri>) -> Unit) {
        onResult.invoke(UiState.Loading)
        viewModelScope.launch {
            crabRepository.uploadSingleFile(fileUris, onResult)
        }
    }

//    fun uploadMultipleFile(fileUris: List<Uri>, onResult: (UiState<List<Uri>>) -> Unit) {
//        onResult.invoke(UiState.Loading)
//        viewModelScope.launch {
//            crabRepository.uploadMultipleFile(fileUris, onResult)
//        }
//    }
}