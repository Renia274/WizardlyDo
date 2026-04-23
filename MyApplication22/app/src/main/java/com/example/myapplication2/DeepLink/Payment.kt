package com.example.myapplication2.DeepLink

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Payment(
    val clientTransactionId: String,
    val type: String,
    val currency: String,
    val price: String,
    val tip: String,
    val installments: Int,
    val appLinkPackageName: String,
    val serviceId: String,
    val referenceId: String,
    val allowPOSsiblePrint: Int,
    val reversalTransactionId: String = "",
    val reversalTransactionRRN: String = ""
) : Parcelable