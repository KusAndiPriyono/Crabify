package com.bangkit.crabify.di

import android.content.SharedPreferences
import com.bangkit.crabify.data.repository.SensorRepositoryImpl
import com.bangkit.crabify.domain.repository.SensorRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {

    @Singleton
    @Provides
    fun provideSensorRepository(
        database: FirebaseFirestore,
    ): SensorRepository {
        return SensorRepositoryImpl(database)
    }
}