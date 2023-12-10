package com.bangkit.crabify.domain.repository

import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.utils.UiState
import com.google.firebase.auth.AuthCredential

interface AuthRepository {
    fun loginUser(email: String, password: String, result: (UiState<String>) -> Unit)

//    fun firebaseSignInWithGoogle(
//        googleCredential: AuthCredential,
//        result: (UiState<String>) -> Unit
//    )

    fun registerUser(
        fullName: String,
        email: String,
        password: String,
        user: User,
        result: (UiState<String>) -> Unit
    )

    fun updateProfile(user: User, result: (UiState<String>) -> Unit)
    fun forgotPassword(email: String, result: (UiState<String>) -> Unit)
    fun logout(result: () -> Unit)
    fun storeSession(id: String, result: (User?) -> Unit)
    fun getSession(result: (User?) -> Unit)
}