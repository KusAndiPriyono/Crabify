package com.bangkit.crabify.di

import com.bangkit.crabify.data.manager.LocalUserManagerImpl
import com.bangkit.crabify.domain.manager.LocalUserManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {


    @Binds
    @Singleton
    abstract fun provideLocalUserManager(
        localUserManagerImpl: LocalUserManagerImpl
    ): LocalUserManager
}