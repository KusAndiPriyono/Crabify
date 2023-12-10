package com.bangkit.crabify.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.AuthRepository
import com.bangkit.crabify.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _login = MutableLiveData<UiState<String>>()
    val login: LiveData<UiState<String>>
        get() = _login

    fun login(email: String, password: String) {
        _login.value = UiState.Loading
        repository.loginUser(email, password) {
            _login.value = it
        }
    }

    fun logout(result: () -> Unit) {
        repository.logout(result)
    }

    fun getSession(result: (User?) -> Unit) {
        repository.getSession(result)
    }


}