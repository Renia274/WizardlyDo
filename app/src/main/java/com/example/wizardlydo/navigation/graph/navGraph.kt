package com.example.wizardlydo.navigation.graph

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wizardlydo.data.wizard.WizardClass
import com.example.wizardlydo.navigation.screens.Screen
import com.example.wizardlydo.screens.customization.CustomizationScreen
import com.example.wizardlydo.screens.donation.DonationScreen
import com.example.wizardlydo.screens.login.LoginScreen
import com.example.wizardlydo.screens.pin.PinSetupScreen
import com.example.wizardlydo.screens.pin.PinVerifyScreen
import com.example.wizardlydo.screens.recovery.RecoveryScreen
import com.example.wizardlydo.screens.settings.SettingsScreen
import com.example.wizardlydo.screens.signup.SignupScreen
import com.example.wizardlydo.screens.signupsigin.WelcomeAuthScreen
import com.example.wizardlydo.screens.splash.SplashScreen
import com.example.wizardlydo.screens.tasks.CreateTaskScreen
import com.example.wizardlydo.screens.tasks.EditTaskScreen
import com.example.wizardlydo.screens.tasks.InventoryScreen
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
                    navController.navigate(Screen.Auth.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.Welcome.route) {
            WelcomeAuthScreen(
                onSignUpClick = { navController.navigate(Screen.Auth.Signup.route) },
                onSignInClick = { navController.navigate(Screen.Auth.Login.route) }
            )
        }

        composable(Screen.Auth.Signup.route) {
            SignupScreen(
                onSignupSuccess = { wizardClass ->
                    navController.navigate(Screen.Customization.createRoute(wizardClass)) {
                        popUpTo(Screen.Auth.Signup.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Auth.Login.route)
                }
            )
        }

        composable(Screen.Auth.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Pin.Setup.route) {
                        popUpTo(Screen.Auth.Login.route) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.Auth.Recovery.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Auth.Recovery.route) {
            RecoveryScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
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
                    navController.navigate(Screen.Pin.Setup.route) {
                        popUpTo(Screen.Customization.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Pin.Setup.route) {
            PinSetupScreen(
                onPinSetupComplete = {
                    navController.navigate(Screen.Pin.Verify.route) {
                        popUpTo(Screen.Pin.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Pin.Verify.route) {
            PinVerifyScreen(
                onPinSuccess = {
                    navController.navigate(Screen.Tasks.Main.route) {
                        popUpTo(Screen.Pin.Verify.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Tasks.Main.route) {
            TaskScreen(
                onBack = {
                    navController.navigate(Screen.Auth.Signup.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onHome = {
                    navController.popBackStack(Screen.Tasks.Main.route, inclusive = false)
                },
                onCreateTask = {
                    navController.navigate(Screen.Tasks.Create.route)
                },
                onEditTask = { taskId ->
                    navController.navigate(Screen.Tasks.Edit.createRoute(taskId.toString()))
                },
                onSettings = {
                    navController.navigate(Screen.Tasks.Settings.route)
                },
                onInventory = {
                    navController.navigate(Screen.Inventory.route)
                },
                onDonation = {
                    navController.navigate(Screen.Donation.route)
                }
            )
        }

        composable(Screen.Tasks.Create.route) {
            CreateTaskScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Tasks.Edit.route,
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
                    navController.navigate(Screen.Auth.Login.route) {
                        popUpTo(Screen.Tasks.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Donation.route) {
            DonationScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}