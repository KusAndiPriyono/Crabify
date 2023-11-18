package com.bangkit.crabify.data.manager

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.bangkit.crabify.domain.manager.LocalUserManager
import com.bangkit.crabify.utils.Constants
import com.bangkit.crabify.utils.Constants.USER_SETTINGS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalUserManagerImpl @Inject constructor(private val application: Application) :
    LocalUserManager {
    override suspend fun saveAppEntry() {
        application.dataStore.edit { settings ->
            settings[PreferencesKeys.APP_ENTRY] = true
        }
    }

    override fun startCrabifyAppEntry(): Flow<Boolean> {
        return application.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.APP_ENTRY] ?: false
        }
    }
}

private val startCrabifyOnlyProperty = preferencesDataStore(name = USER_SETTINGS)
val Context.dataStore: DataStore<Preferences> by startCrabifyOnlyProperty

private object PreferencesKeys {
    val APP_ENTRY = booleanPreferencesKey(Constants.APP_ENTRY)
}