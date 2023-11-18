package com.bangkit.crabify.domain.useCases.app_entry

import com.bangkit.crabify.domain.manager.LocalUserManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartCrabifyAppEntry @Inject constructor(private val localUserManager: LocalUserManager) {
    operator fun invoke(): Flow<Boolean> {
        return localUserManager.startCrabifyAppEntry()
    }
}