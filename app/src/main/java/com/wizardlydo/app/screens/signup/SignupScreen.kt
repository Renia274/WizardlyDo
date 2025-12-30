package com.wizardlydo.app.screens.signup

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
import com.wizardlydo.app.comps.errors.ErrorDialog
import com.wizardlydo.app.comps.form.EmailField
import com.wizardlydo.app.comps.form.PasswordField
import com.wizardlydo.app.data.wizard.WizardClass
import com.wizardlydo.app.screens.signup.comps.LoginRedirectButton
import com.wizardlydo.app.screens.signup.comps.SignupButton
import com.wizardlydo.app.screens.signup.comps.SignupHeader
import com.wizardlydo.app.screens.signup.comps.WizardClassSelector
import com.wizardlydo.app.screens.signup.comps.WizardNameField
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import com.wizardlydo.app.viewmodel.signup.SignupViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel = koinViewModel(),
    onLoginClick: () -> Unit,
    onSignupSuccess: (WizardClass) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isProfileComplete) {
        if (state.isProfileComplete) {
            onSignupSuccess(state.wizardClass)
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
            onLoginClick = onLoginClick,
            nameError = state.usernameError,
            emailError = state.emailError,
            passwordError = state.passwordError,
            isLoading = state.isLoading,
            isPasswordVisible = state.isPasswordVisible,
            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
            isCheckingUsername = state.isCheckingUsername,
            usernameSuggestions = state.usernameSuggestions
        )

        state.error?.let { error ->
            ErrorDialog(
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
    onLoginClick: () -> Unit,
    nameError: String?,
    emailError: String?,
    passwordError: String?,
    isLoading: Boolean,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isCheckingUsername: Boolean = false,
    usernameSuggestions: List<String> = emptyList()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val padding = (screenWidth * 0.06f).coerceIn(24.dp, 40.dp)
    val spacing = (screenHeight * 0.02f).coerceIn(16.dp, 32.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height((screenHeight * 0.05f).coerceIn(20.dp, 50.dp)))

        SignupHeader()
        Spacer(modifier = Modifier.height(spacing))

        WizardNameField(
            name = wizardName,
            onNameChange = onNameChange,
            error = nameError,
            enabled = !isLoading,
            isCheckingAvailability = isCheckingUsername,
            suggestions = usernameSuggestions
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
            enabled = !isLoading && !isCheckingUsername
        )

        Spacer(modifier = Modifier.height((spacing * 0.8f)))

        LoginRedirectButton(
            onClick = onLoginClick,
            enabled = !isLoading
        )

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
            onLoginClick = {},
            nameError = null,
            emailError = null,
            passwordError = null,
            isLoading = false,
            isPasswordVisible = false,
            onTogglePasswordVisibility = {},
            isCheckingUsername = false,
            usernameSuggestions = emptyList()
        )
    }
}