package com.example.wizardlydo.utilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class NotificationPermissionHandler(private val activity: ComponentActivity) {
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    private var permissionCallback: ((Boolean) -> Unit)? = null

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestNotificationPermissionIfNeeded(callback: (Boolean) -> Unit) {
        permissionCallback = callback

        if (hasNotificationPermission()) {
            callback(true)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } catch (e: Exception) {
                Log.e("NotificationPermission", "Failed to request permission", e)
                callback(false)
            }
        } else {
            callback(true)
        }
    }

    private fun onPermissionResult(isGranted: Boolean) {
        permissionCallback?.invoke(isGranted)
    }

    companion object {
        fun checkPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }
}