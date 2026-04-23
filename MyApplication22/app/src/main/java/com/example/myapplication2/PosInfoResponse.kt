package com.example.myapplication2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PosInformationResponse(
    val posSerialNumber: String = "",
    val posModelName: String = "",
    val posOSName: String = "",
    val posSIMSerialNumber: String = "",
    val posPrinterAvailable: String = ""
) : Parcelable {

    fun isValid(): Boolean =
        posSerialNumber.isNotBlank() && posModelName.isNotBlank()

    fun hasSIM(): Boolean =
        posSIMSerialNumber.isNotBlank()

    fun hasPrinter(): Boolean =
        posPrinterAvailable.isNotBlank()
}