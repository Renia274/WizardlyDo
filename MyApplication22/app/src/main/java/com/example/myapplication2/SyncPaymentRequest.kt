package com.example.myapplication2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SyncPaymentRequest(val data: String) : Parcelable