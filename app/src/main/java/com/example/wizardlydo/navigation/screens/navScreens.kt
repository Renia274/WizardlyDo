package com.example.wizardlydo.navigation.screens

import com.example.wizardlydo.data.wizard.WizardClass


sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object WelcomeAuth : Screen("welcome_auth")
    data object Signup : Screen("signup")
    data object Login : Screen("login")
    data object Recovery : Screen("recovery")
    data object PinSetup : Screen("pin_setup")
    data object PinAuth : Screen("pin_auth")

    data object Customization : Screen("customization/{wizardClass}") {
        fun createRoute(wizardClass: WizardClass) = "customization/${wizardClass.name}"
    }
    data object Tasks : Screen("tasks") {
        data object CreateTask : Screen("${Tasks.route}/create")
        data object EditTask : Screen("${Tasks.route}/edit/{taskId}") {
            fun createRoute(taskId: String) = "${Tasks.route}/edit/$taskId"
        }
        data object Settings : Screen("${Tasks.route}/settings")
        data object EditMode : Screen("${Tasks.route}/edit-mode")
    }
    data object Inventory : Screen("inventory")


}