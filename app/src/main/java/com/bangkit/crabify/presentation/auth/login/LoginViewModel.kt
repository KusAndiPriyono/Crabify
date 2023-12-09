package com.bangkit.crabify.presentation.auth.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

//    val loadingState = MutableStateFlow(LoadingState.IDLE)

    private val auth: FirebaseAuth = Firebase.auth

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun loginWithEmailAndPassword(email: String, password: String) = viewModelScope.launch {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginViewModel", "loginWithEmailAndPassword: ${task.result}")
                    } else {
                        Log.d("LoginViewModel", "loginWithEmailAndPassword: ${task.result}")
                    }
                }
        } catch (e: Exception) {
            Log.d("LoginViewModel", "loginWithEmailAndPassword: ${e.message}")
        }
    }

}