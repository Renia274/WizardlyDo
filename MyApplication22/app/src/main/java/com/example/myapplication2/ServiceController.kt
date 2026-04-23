package com.example.myapplication2

import android.content.Context
import com.example.myapplication2.connector.ServiceConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ServiceController(context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val connector = ServiceConnector.Builder(context).build()
    private val callbacks = mutableListOf<Callback>()

    interface Callback {
        fun onConnected()
        fun onDisconnected()
        fun onResponse(response: SyncPaymentResponse)
    }

    init {
        connector.connectionState.onEach { isConnected ->
            if (isConnected) callbacks.forEach { it.onConnected() }
            else callbacks.forEach { it.onDisconnected() }
        }.launchIn(scope)

        connector.asyncResponse.onEach { response ->
            callbacks.forEach { it.onResponse(response) }
        }.launchIn(scope)
    }

    fun addCallback(cb: Callback) = callbacks.add(cb)

    fun removeCallback(cb: Callback) = callbacks.remove(cb)

    fun connect() = connector.connect()

    fun disconnect() = connector.disconnect()

    fun isConnected() = connector.serviceConnected
}