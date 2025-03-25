package com.example.wizardlydo.room

import androidx.room.TypeConverter
import com.example.wizardlydo.providers.SignInProvider
import com.example.wizardlydo.data.WizardClass
import java.util.Date
import com.google.firebase.Timestamp


class WizardTypeConverters {

    @TypeConverter
    fun timestampToLong(timestamp: Timestamp?): Long? = timestamp?.toDate()?.time

    @TypeConverter
    fun longToTimestamp(value: Long?): Timestamp? = value?.let { Timestamp(Date(it)) }

    // WizardClass converters
    @TypeConverter
    fun toWizardClass(wizardClassName: String): WizardClass {
        return WizardClass.valueOf(wizardClassName)
    }

    @TypeConverter
    fun fromWizardClass(value: WizardClass): String {
        return value.name
    }

    // SignInProvider converters
    @TypeConverter
    fun fromSignInProvider(signInProvider: SignInProvider): String {
        return signInProvider.name
    }

    @TypeConverter
    fun toSignInProvider(signInProviderName: String): SignInProvider {
        return SignInProvider.valueOf(signInProviderName)
    }

    // String list converters
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        return data?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
}