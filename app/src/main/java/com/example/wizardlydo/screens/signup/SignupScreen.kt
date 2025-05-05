package com.example.wizardlydo.screens.signup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.data.wizard.WizardClass
import com.example.wizardlydo.screens.signup.comps.EmailField
import com.example.wizardlydo.screens.signup.comps.ErrorDialogComponent
import com.example.wizardlydo.screens.signup.comps.GoogleSignInButton
import com.example.wizardlydo.screens.signup.comps.LoginRedirectButton
import com.example.wizardlydo.screens.signup.comps.PasswordField
import com.example.wizardlydo.screens.signup.comps.SignupButton
import com.example.wizardlydo.screens.signup.comps.SignupHeader
import com.example.wizardlydo.screens.signup.comps.WizardClassSelector
import com.example.wizardlydo.screens.signup.comps.WizardNameField
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.signup.WizardAuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupScreen(
    viewModel: WizardAuthViewModel = koinViewModel(),
    onLoginClick: () -> Unit,
    onSignupSuccess: (WizardClass) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isProfileComplete) {
        if (state.isProfileComplete) {
            // Pass the selected class to parent
            onSignupSuccess(state.wizardClass)
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        SignupContent(
            wizardName = state.wizardName,
            onNameChange = viewModel::updateWizardName,
            wizardClass = state.wizardClass,
            onClassSelected = viewModel::updateWizardClass,
            email = state.email,
            onEmailChange = viewModel::updateEmail,
            password = state.password,
            onPasswordChange = viewModel::updatePassword,
            onSignupClick = viewModel::signUpWithEmail,
            onGoogleSignIn = {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            },
            onLoginClick = onLoginClick,
            nameError = state.usernameError,
            emailError = state.emailError,
            passwordError = state.passwordError,
            isLoading = state.isLoading,
            isPasswordVisible = state.isPasswordVisible,
            onTogglePasswordVisibility = viewModel::togglePasswordVisibility
        )

        state.error?.let { error ->
            ErrorDialogComponent(
                error = error,
                onDismiss = { viewModel.handleError(null) }
            )
        }
    }
}

@Composable
fun SignupContent(
    wizardName: String,
    onNameChange: (String) -> Unit,
    wizardClass: WizardClass,
    onClassSelected: (WizardClass) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSignupClick: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onLoginClick: () -> Unit,
    nameError: String?,
    emailError: String?,
    passwordError: String?,
    isLoading: Boolean,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Simple responsive values
    val padding = (screenWidth * 0.06f).coerceIn(24.dp, 40.dp)
    val spacing = (screenHeight * 0.02f).coerceIn(16.dp, 32.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacer
        Spacer(modifier = Modifier.height((screenHeight * 0.05f).coerceIn(20.dp, 50.dp)))

        SignupHeader()
        Spacer(modifier = Modifier.height(spacing))

        WizardNameField(
            name = wizardName,
            onNameChange = onNameChange,
            error = nameError,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height((spacing * 0.8f)))

        WizardClassSelector(
            selectedClass = wizardClass,
            onClassSelected = onClassSelected,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height((spacing * 0.8f)))

        EmailField(
            email = email,
            onEmailChange = onEmailChange,
            emailError = emailError,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height((spacing * 0.8f)))

        PasswordField(
            password = password,
            onPasswordChange = onPasswordChange,
            passwordError = passwordError,
            enabled = !isLoading,
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = onTogglePasswordVisibility
        )

        Spacer(modifier = Modifier.height(spacing))

        SignupButton(
            onClick = onSignupClick,
            isLoading = isLoading,
            enabled = !isLoading // Always enabled
        )

        Spacer(modifier = Modifier.height((spacing * 0.8f)))

        GoogleSignInButton(
            onClick = onGoogleSignIn,
            enabled = !isLoading // Always enabled
        )

        Spacer(modifier = Modifier.height((spacing * 0.8f)))

        LoginRedirectButton(
            onClick = onLoginClick,
            enabled = !isLoading
        )

        // Bottom spacer to ensure content isn't cut off
        Spacer(modifier = Modifier.height((screenHeight * 0.05f).coerceIn(20.dp, 50.dp)))
    }
}


@Composable
@Preview(showBackground = true)
fun SignupContentPreview() {
    WizardlyDoTheme {
        SignupContent(
            wizardName = "Gandalf",
            onNameChange = {},
            wizardClass = WizardClass.MYSTWEAVER,
            onClassSelected = {},
            email = "gandalf@middleearth.com",
            onEmailChange = {},
            password = "YouShallNotPass123",
            onPasswordChange = {},
            onSignupClick = {},
            onGoogleSignIn = {},
            onLoginClick = {},
            nameError = null,
            emailError = null,
            passwordError = null,
            isLoading = false,
            isPasswordVisible = false,
            onTogglePasswordVisibility = {}
        )
    }
}
