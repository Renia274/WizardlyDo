package com.example.wizardlydo.screens.customization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.wizard.WizardClass
import com.example.wizardlydo.data.models.CustomizationState
import com.example.wizardlydo.screens.customization.comps.GenderSelector
import com.example.wizardlydo.screens.customization.comps.HairColorSelector
import com.example.wizardlydo.screens.customization.comps.HairStyleSelector
import com.example.wizardlydo.screens.customization.comps.OutfitSelector
import com.example.wizardlydo.screens.customization.comps.SkinSelector
import com.example.wizardlydo.screens.customization.comps.WizardPreview
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import com.example.wizardlydo.viewmodel.customization.CustomizationViewModel
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

    val state by viewModel.stateFlow.collectAsState()

    // Handle wizard class changes
    LaunchedEffect(wizardClass) {
        viewModel.updateOutfit(viewModel.getDefaultOutfit(wizardClass))
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Simple responsive values
    val padding = (screenWidth * 0.04f).coerceIn(16.dp, 32.dp)
    val spacing = (screenHeight * 0.02f).coerceIn(12.dp, 24.dp)
    val buttonHeight = (screenHeight * 0.07f).coerceIn(50.dp, 70.dp)

    val scrollState = rememberScrollState()
    var isSaveButtonVisible by remember { mutableStateOf(true) }

    // Track scroll to hide/show save button
    LaunchedEffect(scrollState) {
        var previousScrollPosition = 0
        snapshotFlow { scrollState.value }.collect { currentScrollPosition ->
            isSaveButtonVisible = currentScrollPosition < previousScrollPosition
            previousScrollPosition = currentScrollPosition
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = padding)
        ) {
            WizardPreview(
                state = state,
                modifier = Modifier.height((screenHeight * 0.35f).coerceIn(200.dp, 300.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(spacing))

                GenderSelector(
                    selectedGender = state.gender,
                    onGenderSelected = onGenderSelected
                )

                Spacer(modifier = Modifier.height(spacing))

                SkinSelector(
                    selectedSkin = state.skinColor,
                    onSkinSelected = onSkinSelected
                )

                Spacer(modifier = Modifier.height(spacing))

                HairStyleSelector(
                    gender = state.gender,
                    selectedStyle = state.hairStyle,
                    onHairStyleSelected = onHairStyleSelected
                )

                Spacer(modifier = Modifier.height(spacing))

                HairColorSelector(
                    selectedColor = state.hairColor,
                    onHairColorSelected = onHairColorSelected
                )

                Spacer(modifier = Modifier.height(spacing))

                OutfitSelector(
                    wizardClass = state.wizardClass,
                    selectedOutfit = state.outfit,
                    onOutfitSelected = onOutfitSelected
                )

                Spacer(modifier = Modifier.height(buttonHeight + spacing))
            }
        }

        AnimatedVisibility(
            visible = isSaveButtonVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight)
                    .padding(horizontal = padding, vertical = 8.dp)
            ) {
                Text(
                    text = "Save Customization",
                    style = MaterialTheme.typography.titleMedium
                )
            }
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
            hairStyle = 1,
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

