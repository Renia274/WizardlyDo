package com.example.myapplication2

class PaymentClient private constructor() {

    companion object {
        val instance: PaymentClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PaymentClient()
        }
    }
}