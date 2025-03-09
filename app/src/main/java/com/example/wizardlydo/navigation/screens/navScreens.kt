package com.example.wizardlydo.navigation.screens



sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Signup : Screen("signup")
    data object Login : Screen("login")
    data object Recovery : Screen("recovery")

}