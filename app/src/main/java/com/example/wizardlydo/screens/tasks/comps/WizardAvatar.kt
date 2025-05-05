package com.example.wizardlydo.screens.tasks.comps

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
import com.example.wizardlydo.data.wizard.getHairResourceId
import com.example.wizardlydo.data.wizard.getOutfitResourceId
import com.example.wizardlydo.data.wizard.getSkinResourceId
import com.example.wizardlydo.data.wizard.items.EquippedItems
import com.example.wizardlydo.data.wizard.WizardProfile

@Composable
fun WizardAvatar(
    wizardResult: Result<WizardProfile?>?,
    equippedItems: EquippedItems?,
    modifier: Modifier = Modifier
) {
    val wizardProfile = wizardResult?.getOrNull()
    val error = wizardResult?.exceptionOrNull()

    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        when {
            wizardProfile != null -> {
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

                    // Outfit (equipped from inventory or default)
                    if (equippedItems?.outfit != null) {
                        Image(
                            painter = painterResource(id = equippedItems.outfit.resourceId),
                            contentDescription = "Character Outfit",
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.Center)
                                .offset(x = (-10).dp, y = (-6).dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                id = getOutfitResourceId(
                                    wizardProfile.wizardClass,
                                    wizardProfile.outfit,
                                    wizardProfile.gender
                                )
                            ),
                            contentDescription = "Character Outfit",
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.Center)
                                .offset(x = (-10).dp, y = (-6).dp)
                        )
                    }

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