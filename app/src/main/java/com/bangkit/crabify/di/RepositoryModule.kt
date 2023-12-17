package com.bangkit.crabify.di

import android.content.SharedPreferences
import com.bangkit.crabify.data.repository.AuthRepositoryImpl
import com.bangkit.crabify.data.repository.CrabRepositoryImpl
import com.bangkit.crabify.domain.repository.AuthRepository
import com.bangkit.crabify.domain.repository.CrabRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
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
    fun provideCrabRepository(
        database: FirebaseFirestore,
        storageReference: StorageReference
    ): CrabRepository {
        return CrabRepositoryImpl(database, storageReference)
    }

    @Singleton
    @Provides
    fun provideAuthRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): AuthRepository {
        return AuthRepositoryImpl(database, auth, appPreferences, gson)
    }
}