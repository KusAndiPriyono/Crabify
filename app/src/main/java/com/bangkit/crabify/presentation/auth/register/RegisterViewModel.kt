package com.bangkit.crabify.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.AuthRepository
import com.bangkit.crabify.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _register = MutableLiveData<UiState<String>>()
    val register: LiveData<UiState<String>>
        get() = _register


    fun register(fullName: String, email: String, password: String, user: User) {
        _register.value = UiState.Loading
        repository.registerUser(fullName, email, password, user) {
            _register.value = it
        }
    }
}