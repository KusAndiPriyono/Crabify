package com.bangkit.crabify.presentation.onBoarding

import androidx.lifecycle.ViewModel
import com.bangkit.crabify.data.model.User
import com.bangkit.crabify.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(private val repository: AuthRepository) :
    ViewModel() {

    fun getSession(result: (User?) -> Unit) = repository.getSession(result)
}