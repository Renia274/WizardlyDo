package com.example.wizardlydo.screens.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.WizardClass
import com.example.wizardlydo.data.models.CustomizationState
import com.example.wizardlydo.screens.customization.comps.GenderSelector
import com.example.wizardlydo.screens.customization.comps.HairColorSelector
import com.example.wizardlydo.screens.customization.comps.WizardPreview
import com.example.wizardlydo.screens.customization.comps.SkinSelector
import com.example.wizardlydo.screens.customization.comps.HairStyleSelector
import com.example.wizardlydo.screens.customization.comps.OutfitSelector
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
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

    // Add a LaunchedEffect that runs when the wizard class changes
    LaunchedEffect(wizardClass) {
        // Set default values based on wizard class
        val defaultOutfit = when (wizardClass) {
            WizardClass.CHRONOMANCER -> "Astronomer Robe"
            WizardClass.LUMINARI -> "Crystal Robe"
            WizardClass.DRACONIST -> "Flame Costume"
            WizardClass.MYSTWEAVER -> "Mystic Robe"
        }

        val defaultAccessory = when (wizardClass) {
            WizardClass.CHRONOMANCER -> "Time Glasses"
            WizardClass.LUMINARI -> "Light Mask"
            WizardClass.DRACONIST -> "Dragon Eyes"
            WizardClass.MYSTWEAVER -> "Arcane Monocle"
        }

        // Update the ViewModel with these default values
        viewModel.updateOutfit(defaultOutfit)

    }

    CustomizationContent(
        state = state,
        onGenderSelected = viewModel::updateGender,
        onSkinSelected = viewModel::updateSkin,
        onHairStyleSelected = viewModel::updateHairStyle,
        onHairColorSelected = viewModel::updateHairColor,
        onOutfitSelected = viewModel::updateOutfit,
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
    onSkinSelected: (String) -> Unit,
    onHairStyleSelected: (Int) -> Unit,
    onHairColorSelected: (String) -> Unit,
    onOutfitSelected: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Character preview with updated state
        WizardPreview(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        // Customization options
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            // Gender selection
            GenderSelector(
                selectedGender = state.gender,
                onGenderSelected = onGenderSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Skin selection
            SkinSelector(
                selectedSkin = state.skinColor,
                onSkinSelected = onSkinSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hair style selection
            HairStyleSelector(
                gender = state.gender,
                selectedStyle = state.hairStyle,
                onHairStyleSelected = onHairStyleSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hair color selection
            HairColorSelector(
                selectedColor = state.hairColor,
                onHairColorSelected = onHairColorSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Outfit selection based on wizard class
            OutfitSelector(
                wizardClass = state.wizardClass,
                selectedOutfit = state.outfit,
                onOutfitSelected = onOutfitSelected
            )

            Spacer(modifier = Modifier.height(16.dp))


        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save Customization", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomizationContentPreview() {

    WizardlyDoTheme {
        val sampleState = CustomizationState(
            wizardClass = WizardClass.CHRONOMANCER,
            gender = "Male",
            skinColor = "#FFDBAC",
            hairStyle = 2,
            hairColor = "#3A2D1E",
            outfit = "Astronomer Robe"
        )

        CustomizationContent(
            state = sampleState,
            onGenderSelected = {},
            onSkinSelected = {},
            onHairStyleSelected = {},
            onHairColorSelected = {},
            onOutfitSelected = {},
            onSave = {}
        )
    }
}

