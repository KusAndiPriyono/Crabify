package com.bangkit.crabify.domain.useCases.app_entry

import com.bangkit.crabify.domain.manager.LocalUserManager
import javax.inject.Inject

class SaveAppEntry @Inject constructor(private val localUserManager: LocalUserManager) {
    suspend operator fun invoke() {
        localUserManager.saveAppEntry()
    }
}