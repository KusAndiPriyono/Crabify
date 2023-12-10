package com.bangkit.crabify.di

import com.bangkit.crabify.utils.FirebaseStorageConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

//    @Singleton
//    @Provides
//    @Named(SIGN_IN_REQUEST)
//    fun provideSignInRequest(app: Application) =
//        BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    .setServerClientId(app.getString(R.string.default_web_client_id))
//                    .setFilterByAuthorizedAccounts(true)
//                    .build()
//            )
//            .setAutoSelectEnabled(true)
//            .build()

//    @Singleton
//    @Provides
//    fun provideGoogleSignInOptions(
//        app: Application
//    ) = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//        .requestIdToken(app.getString(R.string.default_web_client_id))
//        .requestEmail()
//        .build()

//    @Singleton
//    @Provides
//    fun provideGoogleSignInClient(
//        app: Application,
//        options: GoogleSignInOptions
//    ) = GoogleSignIn.getClient(app, options)


    @Singleton
    @Provides
    fun provideFirebaseStorage(): StorageReference {
        return FirebaseStorage.getInstance().getReference(FirebaseStorageConstants.APP_ENTRY)
    }


}