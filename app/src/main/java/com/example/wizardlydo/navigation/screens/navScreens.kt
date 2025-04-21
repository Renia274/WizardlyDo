package com.example.wizardlydo.navigation.screens

import com.example.wizardlydo.data.WizardClass


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
        data object CreateTask : Screen("$route/create")
        data object EditTask : Screen("$route/edit/{taskId}") {
            fun createRoute(taskId: String) = "$route/edit/$taskId"
        }
        data object Settings : Screen("$route/settings")
        data object EditMode : Screen("$route/edit-mode")
    }
}