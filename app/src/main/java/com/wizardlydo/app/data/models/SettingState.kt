package com.wizardlydo.app.data.models

data class SettingsState(
    val email: String? = null,
    val wizardName: String = "",
    val darkModeEnabled: Boolean = false
)
