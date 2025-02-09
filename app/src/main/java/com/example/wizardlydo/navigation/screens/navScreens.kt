package com.example.wizardlydo.navigation.screens



sealed class Navigation(val route: String) {
    object Screen {
        object Splash : Navigation("splash")
        object Main : Navigation("main")
    }
}