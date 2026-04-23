package com.example.myapplication2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScanData(
    val code: String,
    val symbology: String,
    val value: String
) : Parcelable