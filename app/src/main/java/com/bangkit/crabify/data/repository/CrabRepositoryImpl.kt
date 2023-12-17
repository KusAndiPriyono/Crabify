package com.bangkit.crabify.data.repository

import android.net.Uri
import com.bangkit.crabify.data.model.Crab
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.CrabRepository
import com.bangkit.crabify.utils.FireStoreCollections
import com.bangkit.crabify.utils.FireStoreDocumentField
import com.bangkit.crabify.utils.FirebaseStorageConstants
import com.bangkit.crabify.utils.UiState
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CrabRepositoryImpl(
    private val database: FirebaseFirestore,
    private val storageReference: StorageReference
) : CrabRepository {
    override fun getCrabs(user: User?, result: (UiState<List<Crab>>) -> Unit) {
        database.collection(FireStoreCollections.CRAB)
            .whereEqualTo(FireStoreDocumentField.USER_ID, user?.id)
            .orderBy(FireStoreDocumentField.DATE, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                val crabs = arrayListOf<Crab>()
                for (document in it) {
                    val crab = document.toObject(Crab::class.java)
                    crabs.add(crab)
                }
                result.invoke(UiState.Success(crabs))
            }
            .addOnFailureListener {
                result.invoke(UiState.Error(it.localizedMessage))
            }
    }

    override fun addCrab(crab: Crab, result: (UiState<Pair<Crab, String>>) -> Unit) {
        val document = database.collection(FireStoreCollections.CRAB).document()
        crab.id = document.id
        document
            .set(crab)
            .addOnSuccessListener {
                result.invoke(UiState.Success(Pair(crab, "Success")))
            }
            .addOnFailureListener {
                result.invoke(UiState.Error(it.localizedMessage))
            }
    }

    override fun updateCrab(crab: Crab, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollections.CRAB).document(crab.id)
        document
            .set(crab)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Success"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Error(it.localizedMessage))
            }
    }

    override fun deleteCrab(crab: Crab, result: (UiState<String>) -> Unit) {
        database.collection(FireStoreCollections.CRAB).document(crab.id)
            .delete()
            .addOnSuccessListener {
                result.invoke(UiState.Success("Crab deleted"))
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Error(e.message.toString()))
            }
    }

    override suspend fun uploadSingleFile(fileUri: Uri, onResult: (UiState<Uri>) -> Unit) {
        try {
            val uri: Uri = withContext(Dispatchers.IO) {
                storageReference
                    .putFile(fileUri)
                    .await()
                    .storage
                    .downloadUrl
                    .await()
            }
            onResult.invoke(UiState.Success(uri))
        } catch (e: FirebaseException) {
            onResult.invoke(UiState.Error(e.message.toString()))
        } catch (e: Exception) {
            onResult.invoke(UiState.Error(e.message.toString()))
        }
    }

    override suspend fun uploadMultipleFile(
        fileUri: List<Uri>,
        onResult: (UiState<List<Uri>>) -> Unit
    ) {
        try {
            val uri: List<Uri> = withContext(Dispatchers.IO) {
                fileUri.map { image ->
                    async {
                        storageReference.child(FirebaseStorageConstants.CRAB_IMAGE)
                            .child(image.lastPathSegment ?: "${System.currentTimeMillis()}.jpg")
                            .putFile(image)
                            .await()
                            .storage
                            .downloadUrl
                            .await()
                    }
                }.awaitAll()
            }
            onResult.invoke(UiState.Success(uri))
        } catch (e: FirebaseException) {
            onResult.invoke(UiState.Error(e.message.toString()))
        } catch (e: Exception) {
            onResult.invoke(UiState.Error(e.message.toString()))
        }
    }
}