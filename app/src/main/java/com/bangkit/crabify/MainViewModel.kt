package com.bangkit.crabify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.crabify.domain.useCases.app_entry.StartCrabifyAppEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val startCrabifyAppEntry: StartCrabifyAppEntry) :
    ViewModel() {

    private val _isCrabifyAppStarted = MutableStateFlow(true)
    val isCrabifyAppStarted = _isCrabifyAppStarted.asStateFlow()

    init {
        startAppEntry()
    }

    private fun startAppEntry() {
        viewModelScope.launch {
            startCrabifyAppEntry().collect { isStarted ->
                _isCrabifyAppStarted.value = isStarted
            }
            delay(3000)
            _isCrabifyAppStarted.value = false
        }
    }
}