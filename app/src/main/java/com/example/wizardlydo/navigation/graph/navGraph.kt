package com.example.wizardlydo.navigation.graph

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wizardlydo.navigation.screens.Screen
import com.example.wizardlydo.screens.signup.SignupScreen



@Composable
fun NavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Signup.route) {
            SignupScreen(
                onLoginClick = { },

            )
        }


    }
}