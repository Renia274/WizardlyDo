package com.example.wizardlydo.navigation.graph

import com.example.wizardlydo.screens.splash.WizardSplashScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wizardlydo.navigation.screens.Navigation
import com.example.wizardlydo.screens.signup.SignupScreen

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Navigation.Screen.Splash.route
    ) {
        composable(Navigation.Screen.Splash.route) {
            WizardSplashScreen(
                onSplashComplete = {
                    navController.navigate(Navigation.Screen.Main.route) {
                        popUpTo(Navigation.Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Navigation.Screen.Signup.route) {
            val viewModel = hiltViewModel<SignupViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.authSuccess) {
                if (state.authSuccess) {
                    navController.navigate(Navigation.Screen.Home.route) {
                        popUpTo(Navigation.Screen.Signup.route) { inclusive = true }
                    }
                }
            }

            SignupScreen(
                viewModel = viewModel
//                onLoginClick = {
//                    navController.navigate(Navigation.Screen.Login.route)
//                }
            )
        }