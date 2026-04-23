package com.example.myapplication2

interface IService {

    fun registerCallback(clientId: Long, callback: IServiceCallback)

    fun unregisterCallback(callback: IServiceCallback)

    fun syncRequest(clientId: Long, request: SyncPaymentRequest): SyncPaymentRequest

}