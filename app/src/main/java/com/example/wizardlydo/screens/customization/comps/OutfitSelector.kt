package com.example.wizardlydo.screens.customization.comps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wizardlydo.data.wizard.WizardClass
import com.example.wizardlydo.wizardHelpers.getDrawableResourceId

@Composable
fun OutfitSelector(
    wizardClass: WizardClass,
    selectedOutfit: String,
    onOutfitSelected: (String) -> Unit
) {
    val outfits = when (wizardClass) {
        WizardClass.CHRONOMANCER -> listOf(
            "Astronomer Robe" to "broad_armor_special_snow",
            "Thunder Cloak" to "broad_shirt_thunder"
        )
        WizardClass.LUMINARI -> listOf(
            "Crystal Robe" to "broad_armor_armoire_crystal_robe",
            "Blue Shirt" to "broad_shirt_blue"
        )
        WizardClass.DRACONIST -> listOf(
            "Flame Costume" to "broad_armor_armoire_barrister_robe",
            "Ram Fleece" to "broad_armor_armoire_ram_fleece",
            "Black Shirt" to "broad_shirt_black",
            "Rainbow Shirt" to "broad_shirt_rainbow"
        )
        WizardClass.MYSTWEAVER -> listOf(
            "Mystic Robe" to "broad_armor_special_pyromancer",
            "Blue Shirt" to "broad_shirt_blue"
        )
    }

    Column {
        Text("Class Outfit", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(outfits) { (name, resourceName) ->
                val resourceId = getDrawableResourceId(resourceName)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(100.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (selectedOutfit == name) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onOutfitSelected(name) }
                        .padding(4.dp)
                ) {
                    if (resourceId != 0) {
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = name,
                            modifier = Modifier.size(60.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
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
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}