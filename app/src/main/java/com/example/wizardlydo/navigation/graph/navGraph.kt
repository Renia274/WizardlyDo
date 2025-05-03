package com.example.wizardlydo.navigation.graph

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.wizardlydo.data.WizardClass
import com.example.wizardlydo.navigation.screens.Screen
import com.example.wizardlydo.screens.customization.CustomizationScreen
import com.example.wizardlydo.screens.login.LoginScreen
import com.example.wizardlydo.screens.pin.PinAuthScreen
import com.example.wizardlydo.screens.pin.PinSetupScreen
import com.example.wizardlydo.screens.recovery.RecoveryScreen
import com.example.wizardlydo.screens.settings.SettingsScreen
import com.example.wizardlydo.screens.signup.SignupScreen
import com.example.wizardlydo.screens.signupsigin.WelcomeAuthScreen
import com.example.wizardlydo.screens.splash.SplashScreen
import com.example.wizardlydo.screens.tasks.CreateTaskScreen
import com.example.wizardlydo.screens.tasks.EditTaskScreen
import com.example.wizardlydo.screens.tasks.TaskScreen

@SuppressLint("NewApi")
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
                navigateToWelcomeAuth = {
                    navController.navigate(Screen.WelcomeAuth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.WelcomeAuth.route) {
            WelcomeAuthScreen(
                onSignUpClick = { navController.navigate(Screen.Signup.route) },
                onSignInClick = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = { wizardClass ->
                    navController.navigate(Screen.Customization.createRoute(wizardClass)) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(
            route = Screen.Customization.route,
            arguments = listOf(
                navArgument("wizardClass") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val className = backStackEntry.arguments?.getString("wizardClass")
                ?: WizardClass.MYSTWEAVER.name

            val wizardClass = try {
                WizardClass.valueOf(className)
            } catch (e: IllegalArgumentException) {
                WizardClass.MYSTWEAVER
            }

            CustomizationScreen(
                wizardClass = wizardClass,
                onComplete = {
                    navController.navigate(Screen.PinSetup.route) {
                        popUpTo(Screen.Customization.route) { inclusive = true }
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
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.PinAuth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Tasks.route) {
            TaskScreen(
                onBack = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onHome = {
                    navController.popBackStack(Screen.Tasks.route, inclusive = false)
                },
                onCreateTask = {
                    navController.navigate(Screen.Tasks.CreateTask.route)
                },
                onEditTask = { taskId ->
                    navController.navigate(Screen.Tasks.EditTask.createRoute(taskId.toString()))
                },
                onSettings = {
                    navController.navigate(Screen.Tasks.Settings.route)
                },
            )
        }

        // Define all nested Task routes explicitly
        composable(Screen.Tasks.CreateTask.route) {
            CreateTaskScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Tasks.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskIdString = backStackEntry.arguments?.getString("taskId") ?: "0"
            val taskId = taskIdString.toIntOrNull() ?: 0

            EditTaskScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Tasks.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskIdString = backStackEntry.arguments?.getString("taskId") ?: "0"
            val taskId = taskIdString.toIntOrNull() ?: 0

            EditTaskScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Tasks.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Tasks.route) { inclusive = true }
                    }
                }
            )
        }
    }
}