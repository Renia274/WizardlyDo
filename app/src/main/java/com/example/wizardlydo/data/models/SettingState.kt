package com.example.wizardlydo.data.models

data class SettingsState(
    val username: String = "",
    val reminderEnabled: Boolean = true,
    val reminderDays: Int = 1,
    val pushNotificationsEnabled: Boolean = true,
    val damageNotificationsEnabled: Boolean = true,
    val passwordChangedSuccessfully: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
    val email: String?=null
)