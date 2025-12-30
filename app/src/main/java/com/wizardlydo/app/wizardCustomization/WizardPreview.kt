package com.wizardlydo.app.wizardCustomization

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.R
import com.wizardlydo.app.models.CustomizationState
import com.wizardlydo.app.data.wizard.WizardClass

// WizardPreview Customization based on wizard class
@Composable
fun WizardPreview(
    state: CustomizationState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Character Skin
                    Image(
                        painter = painterResource(id = getSkinResourceId(state.skinColor)),
                        contentDescription = "Character Skin",
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-12).dp, y = 28.dp)
                    )

                    val outfitResourceId = run {
                        val (defaultMale, defaultFemale) = when (state.wizardClass) {
                            WizardClass.MYSTWEAVER -> R.drawable.mystweaver_robe_male to R.drawable.mystweaver_robe_female
                            WizardClass.CHRONOMANCER -> R.drawable.chronomancer_robe_male to R.drawable.chronomancer_robe_female
                            WizardClass.LUMINARI -> R.drawable.luminari_robe_male to R.drawable.luminari_robe_female
                            WizardClass.DRACONIST -> R.drawable.draconist_robe_male to R.drawable.draconist_robe_female
                        }

                        when (state.outfit.trim().lowercase()) {
                            "winter_coat" -> if (state.gender == "Male") R.drawable.winter_coat_male else R.drawable.winter_coat_female
                            "casual_shirt" -> if (state.gender == "Male") R.drawable.casual_shirt_male else R.drawable.casual_shirt_female
                            "mystic_robe" -> if (state.gender == "Male") R.drawable.mystweaver_robe_male else R.drawable.mystweaver_robe_female
                            "astronomer_robe" -> if (state.gender == "Male") R.drawable.chronomancer_robe_male else R.drawable.chronomancer_robe_female
                            "crystal_robe" -> if (state.gender == "Male") R.drawable.luminari_robe_male else R.drawable.luminari_robe_female
                            "flame_robe" -> if (state.gender == "Male") R.drawable.draconist_robe_male else R.drawable.draconist_robe_female
                            else -> if (state.gender == "Male") defaultMale else defaultFemale // fallback to class default
                        }
                    }

                    Image(
                        painter = painterResource(id = outfitResourceId),
                        contentDescription = "Character Outfit",
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-12).dp, y = 30.dp)
                    )

                    // Character Hair - simplified system
                    val hairResourceId = getHairResourceId(
                        gender = state.gender,
                        hairStyle = state.hairStyle,
                        hairColor = state.hairColor
                    )

                    // Always show hair (no special cases)
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