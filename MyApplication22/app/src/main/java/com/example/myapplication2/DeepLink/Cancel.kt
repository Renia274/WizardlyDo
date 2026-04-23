package com.example.myapplication2.DeepLink

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cancel(
    val clientTransactionId: String,
    val posTransactionId: String,
    val appLinkPackageName: String
) : Parcelable