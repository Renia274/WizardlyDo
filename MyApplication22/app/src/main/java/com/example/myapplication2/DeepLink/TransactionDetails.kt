package com.example.myapplication2.DeepLink

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetTransactionDetails(
    val clientTransactionId: String,
    val oldClientTransactionId: String,
    val appLinkPackageName: String
) : Parcelable