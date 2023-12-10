package com.bangkit.crabify.di

import android.content.SharedPreferences
import com.bangkit.crabify.data.repository.AuthRepositoryImpl
import com.bangkit.crabify.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideAuthRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
//        @Named(SIGN_IN_REQUEST)
//        signInRequest: BeginSignInRequest,
        appPreferences: SharedPreferences,
//        gson: Gson
    ): AuthRepository {
        return AuthRepositoryImpl(database, auth, appPreferences)
    }
}