package com.bangkit.crabify.utils

data class LoadingState(
    val status: Status,
    val message: String? = null,
) {
    enum class Status {
        LOADING,
        SUCCESS,
        FAILED,
        IDLE
    }

    companion object {
        val IDLE = LoadingState(Status.IDLE)
        val LOADING = LoadingState(Status.LOADING)
        val SUCCESS = LoadingState(Status.SUCCESS)
        val FAILED = LoadingState(Status.FAILED)
    }
}
