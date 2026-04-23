package com.example.myapplication2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SyncPaymentResponse(val result: String) : Parcelable