package com.example.wizardlydo.navigation.graph

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wizardlydo.navigation.screens.Screen
import com.example.wizardlydo.screens.login.LoginScreen
import com.example.wizardlydo.screens.pin.PinAuthScreen
import com.example.wizardlydo.screens.pin.PinSetupScreen
import com.example.wizardlydo.screens.recovery.RecoveryScreen
import com.example.wizardlydo.screens.splash.SplashScreen
import com.example.wizardlydo.screens.signup.SignupScreen


@RequiresApi(Build.VERSION_CODES.R)
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
            SignupScreen(
                onSignupSuccess = {

                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.PinSetup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.Recovery.route)
                }
            )
        }

        composable(Screen.Recovery.route) {
            RecoveryScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PinSetup.route) {
            PinSetupScreen(
                onPinSetupComplete = {
                    navController.navigate(Screen.PinAuth.route) {
                        popUpTo(Screen.PinSetup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.PinAuth.route) {
            PinAuthScreen(
                onPinSuccess = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.PinAuth.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
