package com.example.wizardlydo

import androidx.room.TypeConverter
import java.util.Date

class WizardTypeConverters {
    @TypeConverter
    fun fromWizardClass(wizardClass: WizardClass): String {
        return wizardClass.name
    }

    @TypeConverter
    fun toWizardClass(wizardClassName: String): WizardClass {
        return WizardClass.valueOf(wizardClassName)
    }

    @TypeConverter
    fun fromSignInProvider(signInProvider: SignInProvider): String {
        return signInProvider.name
    }

    @TypeConverter
    fun toSignInProvider(signInProviderName: String): SignInProvider {
        return SignInProvider.valueOf(signInProviderName)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        return data?.split(",")?.filter { it.isNotBlank() }
    }
}