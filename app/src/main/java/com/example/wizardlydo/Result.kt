package com.example.wizardlydo


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String? = null) : Result<Nothing>()
}