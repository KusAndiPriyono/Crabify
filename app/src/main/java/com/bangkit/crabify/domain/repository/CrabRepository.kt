package com.bangkit.crabify.domain.repository

import android.net.Uri
import com.bangkit.crabify.data.model.Crab
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.utils.UiState

interface CrabRepository {
    fun getCrabs(user: User?, result: (UiState<List<Crab>>) -> Unit)

    fun addCrab(crab: Crab, result: (UiState<Pair<Crab, String>>) -> Unit)

    fun updateCrab(crab: Crab, result: (UiState<String>) -> Unit)

    fun deleteCrab(crab: Crab, result: (UiState<String>) -> Unit)

    suspend fun uploadSingleFile(fileUri: Uri, onResult: (UiState<Uri>) -> Unit)

    suspend fun uploadMultipleFile(fileUri: List<Uri>, onResult: (UiState<List<Uri>>) -> Unit)
}