package com.example.wizardlydo.screens.customization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.WizardClass
import com.example.wizardlydo.screens.customization.comps.ColorPickers
import com.example.wizardlydo.screens.customization.comps.GenderSelector
import com.example.wizardlydo.screens.customization.comps.WizardPreview
import com.example.wizardlydo.viewmodel.CustomizationState
import com.example.wizardlydo.viewmodel.CustomizationViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun CustomizationScreen(
    wizardClass: WizardClass,
    onComplete: () -> Unit
) {
    val viewModel: CustomizationViewModel = koinViewModel(parameters = {
        parametersOf(wizardClass)
    })

    val state by viewModel.state.collectAsState()

    CustomizationContent(
        state = state,
        onGenderSelected = viewModel::updateGender,
        onColorsChanged = { body, clothing, accessory ->
            viewModel.updateColors(body, clothing, accessory)
        },
        onSave = {
            viewModel.saveCustomization()
            onComplete()
        }
    )
}


@Composable
fun CustomizationContent(
    state: CustomizationState,
    onGenderSelected: (String) -> Unit,
    onColorsChanged: (body: String, clothing: String, accessory: String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        WizardPreview(state = state)

        Spacer(modifier = Modifier.height(24.dp))


        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {

            GenderSelector(
                selectedGender = state.gender,
                onGenderSelected = onGenderSelected
            )

            Spacer(modifier = Modifier.height(24.dp))


            ColorPickers(
                state = state,
                onColorsChanged = onColorsChanged
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp))
        {
            Text("Save Customization", style = MaterialTheme.typography.titleMedium)
        }
    }
}

