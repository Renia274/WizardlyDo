package com.example.wizardlydo.screens.signup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.viewmodel.WizardAuthViewModel
import com.example.wizardlydo.screens.signup.comps.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupScreen(
    viewModel: WizardAuthViewModel = koinViewModel(),
    onLoginClick: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            task.addOnSuccessListener { account ->
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                viewModel.handleGoogleSignIn(credential)
            }.addOnFailureListener {
                viewModel.handleError("Google sign-in failed")
            }
        }
    }

    LaunchedEffect(state.isProfileComplete) {
        if (state.isProfileComplete) onSignupSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignupHeader()
            Spacer(modifier = Modifier.height(32.dp))

            WizardNameField(
                name = state.wizardName,
                onNameChange = viewModel::updateWizardName,
                error = state.error?.takeIf { it.contains("Wizard name") },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            WizardClassSelector(
                selectedClass = state.wizardClass,
                onClassSelected = viewModel::updateWizardClass,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            EmailField(
                email = state.email,
                onEmailChange = viewModel::updateEmail,
                emailError = state.error?.takeIf { it.contains("email") },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                password = state.password,
                onPasswordChange = viewModel::updatePassword,
                passwordError = state.error?.takeIf { it.contains("Password") },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            SignupButton(
                onClick = viewModel::signUpWithEmail,
                isLoading = state.isLoading,
                enabled = !state.isLoading && viewModel.isFormValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            GoogleSignInButton(
                onClick = {
                    if (viewModel.validateForm()) {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                enabled = !state.isLoading && viewModel.isFormValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginRedirectButton(
                onClick = onLoginClick,
                enabled = !state.isLoading
            )
        }

        state.error?.let { error ->
            ErrorDialogComponent(
                error = error,
                onDismiss = { viewModel.handleError(null) }
            )
        }
    }
}