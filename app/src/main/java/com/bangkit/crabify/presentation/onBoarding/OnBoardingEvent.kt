package com.bangkit.crabify.presentation.onBoarding

sealed class OnBoardingEvent {
    data object SaveAppEntry : OnBoardingEvent()
}
