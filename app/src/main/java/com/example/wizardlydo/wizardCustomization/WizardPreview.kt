package com.example.wizardlydo.wizardCustomization

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.data.models.CustomizationState
import com.example.wizardlydo.data.wizard.WizardClass

// WizardPreview Customization based on wizard class
@Composable
fun WizardPreview(
    state: CustomizationState,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp)

    // Add debug logging
    android.util.Log.d("OutfitDebug", "WizardPreview - Class: ${state.wizardClass}, Outfit: '${state.outfit}', Gender: ${state.gender}")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Character Skin
                    Image(
                        painter = painterResource(id = getSkinResourceId(state.skinColor)),
                        contentDescription = "Character Skin",
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x=(-12).dp,y = (28).dp)
                    )

                    // For MYSTWEAVER
                    val outfitResourceId = if (state.wizardClass == WizardClass.MYSTWEAVER) {
                        when (state.outfit.trim()) {
                            "Ram Fleece" -> {
                                R.drawable.broad_armor_ram_fleece_robe
                            }
                            "Rainbow Shirt" -> {
                                R.drawable.broad_shirt_rainbow
                            }
                            "Mystic Robe" -> {
                                if (state.gender == "Male")
                                    R.drawable.broad_armor_special_pyromancer
                                else
                                    R.drawable.slim_armor_special_pyromancer
                            }
                            else -> {
                                R.drawable.broad_armor_ram_fleece_robe
                            }
                        }
                    } else {
                        //Classes: CHRONOMANCER, LUMINARI, DRACONIST
                        getOutfitResource(state.wizardClass, state.outfit, state.gender)
                    }


                    Image(
                        painter = painterResource(id = outfitResourceId),
                        contentDescription = "Character Outfit",
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x=(-12).dp,y = 30.dp)
                    )

                    // Character Hair
                    val hairResourceId = getHairResourceId(
                        gender = state.gender,
                        hairStyle = state.hairStyle,
                        hairColor = state.hairColor
                    )

                    Image(
                        painter = painterResource(id = hairResourceId),
                        contentDescription = "Character Hair",
                        modifier = Modifier
                            .size(42.dp)
                            .offset(y = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.wizardClass.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Customizing your character",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}