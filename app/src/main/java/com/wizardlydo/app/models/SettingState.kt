package com.wizardlydo.app.models

data class SettingsState(
    val email: String? = null,
    val wizardName: String = "",
    val darkModeEnabled: Boolean = false
)
