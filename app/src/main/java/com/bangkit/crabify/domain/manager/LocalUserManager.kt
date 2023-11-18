package com.bangkit.crabify.domain.manager

import kotlinx.coroutines.flow.Flow

interface LocalUserManager {
    suspend fun saveAppEntry()
    fun startCrabifyAppEntry(): Flow<Boolean>
}