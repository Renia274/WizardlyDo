package com.example.myapplication2.DeepLink


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reversal(
    val clientTransactionId: String,
    val type: String,
    val currency: String,
    val bankId: String,
    val posTransactionId: String,
    val batchId: String,
    val sequenceId: String,
    val posDateTime: String,
    val appLinkPackageName: String,
    val serviceId: String,
    val referenceId: String
) : Parcelable