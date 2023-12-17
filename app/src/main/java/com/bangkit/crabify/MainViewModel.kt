package com.bangkit.crabify

import androidx.lifecycle.ViewModel
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: AuthRepository) :
    ViewModel() {

    private val _isCrabifyAppStarted = MutableStateFlow(true)
    val isCrabifyAppStarted = _isCrabifyAppStarted.asStateFlow()

    init {
        startAppEntry()
    }

    private fun startAppEntry() {
        repository.getSession { user ->
            _isCrabifyAppStarted.value = user != null
        }
        _isCrabifyAppStarted.value = false
    }

    fun getSession(result: (User?) -> Unit) {
        repository.getSession(result)
    }

}