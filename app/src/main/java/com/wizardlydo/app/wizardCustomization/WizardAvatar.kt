package com.wizardlydo.app.wizardCustomization

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.R
import com.wizardlydo.app.data.wizard.WizardClass
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.data.wizard.items.EquippedItems



@Composable
fun WizardAvatar(
    wizardResult: Result<WizardProfile?>?,
    equippedItems: EquippedItems?,
    modifier: Modifier = Modifier
) {
    val wizardProfile = wizardResult?.getOrNull()
    val error = wizardResult?.exceptionOrNull()
    val isWizardDead = wizardProfile?.health != null && wizardProfile.health <= 0

    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        when {
            wizardProfile != null && !isWizardDead -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Background layer - only show if equipped
                    equippedItems?.background?.let { background ->
                        Image(
                            painter = painterResource(id = background.resourceId),
                            contentDescription = "Background",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            alpha = 0.7f
                        )
                    }

                    // Skin/Body
                    Image(
                        painter = painterResource(id = getSkinResourceId(wizardProfile.skinColor)),
                        contentDescription = "Character Body",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                            .offset(x = (-10).dp, y = (-6).dp)
                    )

                    val outfitResourceId = when {
                        // Equipped items for wizard
                        equippedItems?.outfit != null -> equippedItems.outfit.resourceId

                        // MYSTWEAVER
                        wizardProfile.wizardClass == WizardClass.MYSTWEAVER -> {
                            getMystweaverOutfitResource(wizardProfile.outfit, wizardProfile.gender)
                        }

                        // CHRONOMANCER,LUMINARI,DRACONIST
                        else -> {
                            val (defaultMale, defaultFemale) = when (wizardProfile.wizardClass) {
                                WizardClass.CHRONOMANCER -> R.drawable.chronomancer_robe_male to R.drawable.chronomancer_robe_female
                                WizardClass.LUMINARI -> R.drawable.luminari_robe_male to R.drawable.luminari_robe_female
                                WizardClass.DRACONIST -> R.drawable.draconist_robe_male to R.drawable.draconist_robe_female
                                else -> R.drawable.chronomancer_robe_male to R.drawable.chronomancer_robe_female // fallback
                            }

                            when (wizardProfile.outfit) {
                                "winter_coat" -> genderSelect(wizardProfile.gender, R.drawable.winter_coat_male, R.drawable.winter_coat_female)
                                else -> genderSelect(wizardProfile.gender, defaultMale, defaultFemale)
                            }
                        }
                    }

                    Image(
                        painter = painterResource(id = outfitResourceId),
                        contentDescription = "Character Outfit",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                            .offset(x = (-10).dp, y = (-6).dp)
                    )

                    // Hair
                    Image(
                        painter = painterResource(
                            id = getHairResourceId(
                                wizardProfile.gender,
                                wizardProfile.hairStyle.toIntOrNull() ?: 0,
                                wizardProfile.hairColor
                            )
                        ),
                        contentDescription = "Character Hair",
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 20.dp)
                    )

                    // Accessory (Wizard Hat)
                    equippedItems?.accessory?.let { accessory ->
                        Image(
                            painter = painterResource(id = accessory.resourceId),
                            contentDescription = "Accessory",
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-14).dp)
                        )
                    }

                    // Weapon (Staff)
                    equippedItems?.weapon?.let { weapon ->
                        Image(
                            painter = painterResource(id = weapon.resourceId),
                            contentDescription = "Weapon",
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.CenterStart)
                                .offset(x = (-3).dp, y = (-10).dp)
                        )
                    }
                }
            }

            wizardProfile != null && isWizardDead -> {
                // Skeleton display when wizard is dead (HP <= 0)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    equippedItems?.background?.let { background ->
                        Image(
                            painter = painterResource(id = background.resourceId),
                            contentDescription = "Background",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            alpha = 0.3f
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.skeleton_face),
                        contentDescription = "Skeleton",
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.Center),
                        colorFilter = null
                    )
                }
            }

            error != null -> {
                Text(
                    text = "⚠",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.size(40.dp)
                )
            }

            else -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun genderSelect(gender: String, male: Int, female: Int) =
    if (gender == "Male") male else female
