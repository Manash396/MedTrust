package com.mk.medtrust.util

sealed class Result<out T> {
// Okay Krishna
    data class Success<T>(val data: T) : Result<T>()

    data class Error<T>(val message: String, val data: T? = null) : Result<T>()

    object Loading : Result<Nothing>()
}
