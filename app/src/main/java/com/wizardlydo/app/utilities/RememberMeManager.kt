package com.wizardlydo.app.utilities

import android.content.Context
import android.view.autofill.AutofillManager
import androidx.core.content.edit

class RememberMeManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("remember_me_prefs", Context.MODE_PRIVATE)
    private val autofillManager = context.getSystemService(AutofillManager::class.java)

    fun saveEmail(email: String) {
        prefs.edit { putString("remembered_email", email) }
    }

    fun getRememberedEmail(): String? {
        return prefs.getString("remembered_email", null)
    }

    fun clearRememberedEmail() {
        prefs.edit { remove("remembered_email") }
    }

    fun setRememberMe(remember: Boolean) {
        prefs.edit { putBoolean("remember_me", remember) }
    }

    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean("remember_me", false)
    }

    /**
     * Check if autofill is available and enabled on the device
     */
    fun isAutofillAvailable(): Boolean {
        return autofillManager?.isEnabled == true
    }

    /**
     * Check if autofill is supported for this app
     */
    fun hasAutofillService(): Boolean {
        return autofillManager?.hasEnabledAutofillServices() == true
    }

    /**
     * Save credentials for autofill (to be used after successful login)
     */
    fun saveCredentialsForAutofill(email: String) {
        if (isAutofillAvailable()) {
            // The actual autofill saving is handled by the framework
            // when proper autofill hints are set on the compose fields
            saveEmail(email)
        }
    }

    /**
     * Clear all saved data including autofill preferences
     */
    fun clearAll() {
        prefs.edit { clear() }
    }
}