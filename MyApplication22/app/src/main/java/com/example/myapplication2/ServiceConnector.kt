package com.example.myapplication2


import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ServiceConnector(private val context: Context) {

    private val tag = ServiceConnector::class.java.simpleName

    var serviceConnected = false

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val _asyncResponse = MutableSharedFlow<SyncPaymentResponse>()
    val asyncResponse: SharedFlow<SyncPaymentResponse> = _asyncResponse.asSharedFlow()

    fun connect() {
        if (serviceConnected) return
        serviceConnected = true
        _connectionState.value = true
        Log.d(tag, "connected")
    }

    fun disconnect() {
        if (!serviceConnected) return
        serviceConnected = false
        _connectionState.value = false
        Log.d(tag, "disconnected")
    }

    fun emitResponse(response: SyncPaymentResponse) {
        _asyncResponse.tryEmit(response)
    }
}