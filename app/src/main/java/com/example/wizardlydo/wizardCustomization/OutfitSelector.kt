package com.example.wizardlydo.wizardCustomization

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wizardlydo.R
import com.example.wizardlydo.data.wizard.WizardClass

@Composable
fun OutfitSelector(
    wizardClass: WizardClass,
    gender: String,
    selectedOutfit: String,
    onOutfitSelected: (String) -> Unit
) {
    val outfits = remember(wizardClass) {
        when (wizardClass) {
            WizardClass.MYSTWEAVER -> listOf(
                Triple("Mystic Robe", "mystic_robe", if (gender == "Male") R.drawable.mystweaver_robe_male else R.drawable.mystweaver_robe_female),
                Triple("Winter Coat", "winter_coat", if (gender == "Male") R.drawable.winter_coat_male else R.drawable.winter_coat_female),
                Triple("Casual Shirt", "casual_shirt", if (gender == "Male") R.drawable.casual_shirt_male else R.drawable.casual_shirt_female)
            )

            WizardClass.CHRONOMANCER -> listOf(
                Triple("Astronomer Robe", "astronomer_robe", if (gender == "Male") R.drawable.chronomancer_robe_male else R.drawable.chronomancer_robe_female),
                Triple("Winter Coat", "winter_coat", if (gender == "Male") R.drawable.winter_coat_male else R.drawable.winter_coat_female)
            )

            WizardClass.LUMINARI -> listOf(
                Triple("Crystal Robe", "crystal_robe", if (gender == "Male") R.drawable.luminari_robe_male else R.drawable.luminari_robe_female),
                Triple("Winter Coat", "winter_coat", if (gender == "Male") R.drawable.winter_coat_male else R.drawable.winter_coat_female)
            )

            WizardClass.DRACONIST -> listOf(
                Triple("Flame Robe", "flame_robe", if (gender == "Male") R.drawable.draconist_robe_male else R.drawable.draconist_robe_female),
                Triple("Winter Coat", "winter_coat", if (gender == "Male") R.drawable.winter_coat_male else R.drawable.winter_coat_female)
            )
        }
    }

    Column {
        Text(
            text = "Outfit",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(outfits) { index, (outfitName, outfitId, resourceId) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .clickable { onOutfitSelected(outfitName) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 2.dp,
                                color = if (selectedOutfit == outfitName)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(Color.White)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (resourceId != 0) {
                            Image(
                                painter = painterResource(id = resourceId),
                                contentDescription = outfitName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.Center
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Missing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = outfitName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}