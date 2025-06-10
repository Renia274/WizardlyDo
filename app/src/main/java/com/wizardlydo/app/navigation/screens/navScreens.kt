package com.wizardlydo.app.navigation.screens

import com.wizardlydo.app.data.wizard.WizardClass


sealed class Screen(val route: String) {
    data object Splash : Screen("splash")

    // Authentication Screens
    sealed class Auth(route: String) : Screen(route) {
        data object Welcome : Auth("auth/welcome")
        data object Signup : Auth("auth/signup")
        data object Login : Auth("auth/login")
        data object Recovery : Auth("auth/recovery")
    }

    // PIN Security Screens
    sealed class Pin(route: String) : Screen(route) {
        data object Setup : Pin("pin/setup")
        data object Verify : Pin("pin/verify")
    }

    // Customization Screen
    data object Customization : Screen("customization/{wizardClass}") {
        fun createRoute(wizardClass: WizardClass) = "customization/${wizardClass.name}"
    }

    // Task Related Screens
    sealed class Tasks(route: String) : Screen(route) {
        data object Main : Tasks("tasks")
        data object Create : Tasks("tasks/create")
        data object Edit : Tasks("tasks/edit/{taskId}") {
            fun createRoute(taskId: String) = "tasks/edit/$taskId"
        }
        data object Settings : Tasks("tasks/settings")
    }

    // Other Screens
    data object Inventory : Screen("inventory")
    data object Donation : Screen("donation")
}
