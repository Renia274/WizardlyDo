package com.wizardlydo.app.utilities

import android.content.Context
import androidx.core.content.edit

class RememberMeManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("remember_me_prefs", Context.MODE_PRIVATE)

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
}