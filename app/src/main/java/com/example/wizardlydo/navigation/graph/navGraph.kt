package com.example.wizardlydo.navigation.graph

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wizardlydo.navigation.screens.Screen
import com.example.wizardlydo.screens.splash.SplashScreen
import com.example.wizardlydo.screens.signup.SignupScreen as SignupScreen1


@Composable
fun NavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                navigateToSignup = {
                    navController.navigate(Screen.Signup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen1(
                onSignupSuccess = {
                    // Navigate to main screen after successful signup
                    // Add your main screen route when ready
                },
                onLoginClick = {
                    // Navigate to login screen when added
                }
            )
        }
    }
}