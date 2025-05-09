package com.example.wizardlydo.wizardCustomization

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.example.wizardlydo.R
import com.example.wizardlydo.data.wizard.WizardClass

@Composable
fun OutfitSelector(
    wizardClass: WizardClass,
    selectedOutfit: String,
    onOutfitSelected: (String) -> Unit
) {
    val outfits = when (wizardClass) {
        WizardClass.CHRONOMANCER -> listOf(
            "Astronomer Robe" to R.drawable.broad_armor_special_snow
        )
        WizardClass.LUMINARI -> listOf(
            "Crystal Robe" to R.drawable.broad_armor_armoire_crystal_robe
        )
        WizardClass.DRACONIST -> listOf(
            "Flame Robe" to R.drawable.broad_armor_draconist,
            "Ram Fleece" to R.drawable.broad_armor_ram_fleece_robe
        )
        WizardClass.MYSTWEAVER -> listOf(
            "Mystic Robe" to R.drawable.broad_armor_special_pyromancer,
            "Ram Fleece" to R.drawable.broad_armor_ram_fleece_robe,
            "Rainbow Shirt" to R.drawable.broad_shirt_rainbow
        )
    }

    Column {
        Text("Class Outfit", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(outfits) { (name, resourceId) ->
                // Add debug logging when outfit is selected
                val isSelected = selectedOutfit == name

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(100.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            android.util.Log.d("OutfitDebug", "Selected outfit: '$name' for ${wizardClass.name}")
                            onOutfitSelected(name)
                        }
                        .padding(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = resourceId),
                        contentDescription = name,
                        modifier = Modifier.size(60.dp)
                    )
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