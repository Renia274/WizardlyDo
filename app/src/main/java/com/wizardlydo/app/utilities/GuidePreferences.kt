package com.wizardlydo.app.utilities

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class GuidePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "guide_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_GUIDE_SHOWN = "guide_shown"
    }

    fun hasSeenGuide(): Boolean {
        return prefs.getBoolean(KEY_GUIDE_SHOWN, false)
    }

    fun markGuideAsShown() {
        prefs.edit { putBoolean(KEY_GUIDE_SHOWN, true) }
    }

    fun resetGuide() {
        prefs.edit { putBoolean(KEY_GUIDE_SHOWN, false) }
    }
}