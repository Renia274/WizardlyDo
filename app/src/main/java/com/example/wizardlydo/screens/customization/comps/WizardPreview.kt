package com.example.wizardlydo.screens.customization.comps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.data.models.CustomizationState
import com.example.wizardlydo.wizardHelpers.getHairResourceId
import com.example.wizardlydo.wizardHelpers.getOutfitResourceId

// WizardPreview Customization based on wizard class
@Composable
fun WizardPreview(
    state: CustomizationState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(200.dp)
        ) {
            // Skin/Body
            val skinResourceId = when (state.skinColor) {
                "light" -> R.drawable.skin_f5a76e
                "medium" -> R.drawable.skin_ea8349
                "dark" -> R.drawable.skin_98461a
                "fantasy1" -> R.drawable.skin_0ff591
                "fantasy2" -> R.drawable.skin_800ed0
                else -> R.drawable.skin_f5a76e
            }

            Image(
                painter = painterResource(id = skinResourceId),
                contentDescription = "Character Body",
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
            )

            // Outfit
            val outfitResId = getOutfitResourceId(state.wizardClass, state.outfit, state.gender)
            if (outfitResId != 0) {
                Image(
                    painter = painterResource(id = outfitResId),
                    contentDescription = "Character Outfit",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                )
            }

            // Hair
            val hairResId = getHairResourceId(state.gender, state.hairStyle, state.hairColor)
            Image(
                painter = painterResource(id = hairResId),
                contentDescription = "Character Hair",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopCenter)
                    .offset(x = 10.dp, y = 73.dp)
            )
        }
    }
}
