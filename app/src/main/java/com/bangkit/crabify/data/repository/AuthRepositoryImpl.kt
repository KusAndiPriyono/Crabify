package com.bangkit.crabify.data.repository

import android.content.SharedPreferences
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.AuthRepository
import com.bangkit.crabify.utils.FireStoreCollections.USER
import com.bangkit.crabify.utils.SharedPrefConstants
import com.bangkit.crabify.utils.UiState
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val database: FirebaseFirestore, private val auth: FirebaseAuth,
//    @Named(SIGN_IN_REQUEST)
//    private val signInRequest: BeginSignInRequest,
    private val appPreferences: SharedPreferences,
//    private val gson: Gson
) : AuthRepository {

    override fun loginUser(email: String, password: String, result: (UiState<String>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storeSession(id = task.result?.user?.uid ?: "") {
                    if (it == null) {
                        result.invoke(UiState.Error("Failed to store local session"))
                    } else {
                        result.invoke(UiState.Success("Login Success"))
                    }
                }
            }
        }.addOnFailureListener {
            result.invoke(UiState.Error(it.message.toString()))
        }
    }

    override fun firebaseSignInWithGoogle(
        googleCredential: AuthCredential, result: (UiState<String>) -> Unit
    ) {
        auth.signInWithCredential(googleCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    val user = auth.currentUser
                    if (user != null) {
                        val newUser = User(
                            id = user.uid,
                            fullName = user.displayName.toString(),
                            email = user.email.toString(),
                        )
                        database.collection(USER).document(user.uid).set(newUser)
                            .addOnSuccessListener {
                                storeSession(id = user.uid) {
                                    if (it == null) {
                                        result.invoke(UiState.Error("Failed to store local session"))
                                    } else {
                                        result.invoke(UiState.Success("Login Success"))
                                    }
                                }
                            }.addOnFailureListener {
                                result.invoke(UiState.Error(it.message.toString()))
                            }
                    }
                }
            }
        }.addOnFailureListener {
            result.invoke(UiState.Error(it.message.toString()))
        }
    }


    override fun registerUser(
        fullName: String,
        email: String,
        password: String,
        user: User,
        result: (UiState<String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { it ->
            if (it.isSuccessful) {
                user.id = it.result?.user?.uid.toString()
                updateProfile(user) { state ->
                    when (state) {
                        is UiState.Success -> {
                            storeSession(id = it.result?.user?.uid ?: "") {
                                if (it == null) {
                                    result.invoke(UiState.Error("Failed to store local session"))
                                } else {
                                    result.invoke(UiState.Success("Register Success"))
                                }
                            }
                        }

                        is UiState.Error -> {
                            result.invoke(UiState.Error(state.message))
                        }

                        is UiState.Loading -> {
                            result.invoke(UiState.Loading)
                        }

                        else -> {}
                    }
                }
            } else {
                try {
                    throw it.exception ?: Exception("Invalid authentication")
                } catch (e: FirebaseAuthWeakPasswordException) {
                    result.invoke(UiState.Error("Authentication failed, password should be at least 6 characters"))
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    result.invoke(UiState.Error("Authentication failed, invalid email"))
                } catch (e: FirebaseAuthUserCollisionException) {
                    result.invoke(UiState.Error("Authentication failed, user with this email already exist"))
                } catch (e: Exception) {
                    result.invoke(UiState.Error(e.message.toString()))
                }
            }
        }.addOnFailureListener {
            result.invoke(UiState.Error(it.localizedMessage))
        }
    }

    override fun updateProfile(user: User, result: (UiState<String>) -> Unit) {
        val document = database.collection(USER).document(user.id)
        document.set(user).addOnSuccessListener {
            result.invoke(UiState.Success("Update Success"))
        }.addOnFailureListener {
            result.invoke(UiState.Error(it.message.toString()))
        }
    }

    override fun forgotPassword(email: String, result: (UiState<String>) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                result.invoke(UiState.Success("Email sent"))
            } else {
                result.invoke(UiState.Error(task.exception?.message.toString()))
            }
        }.addOnFailureListener {
            result.invoke(UiState.Error(it.message.toString()))
        }
    }

    override fun logout(result: () -> Unit) {
        auth.signOut()
        appPreferences.edit().putString(SharedPrefConstants.USER_SESSION, null).clear().apply()
        result.invoke()
    }

    override fun storeSession(id: String, result: (User?) -> Unit) {
        database.collection(USER).document(id)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result?.toObject(User::class.java)
                    appPreferences.edit().putString(SharedPrefConstants.USER_SESSION, id).apply()
                    result.invoke(user)
                } else {
                    result.invoke(null)
                }

            }.addOnFailureListener {
                result.invoke(null)
            }
    }

    override fun getSession(result: (User?) -> Unit) {
        val userId = appPreferences.getString(SharedPrefConstants.USER_SESSION, null)
        if (userId == null) {
            result.invoke(null)
        } else {
//            val user = gson.fromJson(userId, User::class.java)
            result.invoke(User(id = userId))
        }
    }
}