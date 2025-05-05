package com.example.wizardlydo.screens.customization.comps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wizardlydo.R
import com.example.wizardlydo.data.wizard.getHairResourceId
import com.example.wizardlydo.data.wizard.WizardClass
import com.example.wizardlydo.data.models.CustomizationState

// WizardPreview Customization
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

/**
 * Gender selection component
 */
@Composable
fun GenderSelector(selectedGender: String, onGenderSelected: (String) -> Unit) {
    Column {
        Text("Gender", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            SelectionChip(
                text = "Male",
                selected = selectedGender == "Male",
                onClick = { onGenderSelected("Male") }
            )
            SelectionChip(
                text = "Female",
                selected = selectedGender == "Female",
                onClick = { onGenderSelected("Female") }
            )
        }
    }
}

/**
 * Skin color selection component
 */
@Composable
fun SkinSelector(selectedSkin: String, onSkinSelected: (String) -> Unit) {
    Column {
        Text("Skin Color", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Standard skin tones
            ColorChip(
                color = Color(0xFFF5A76E), // Light skin
                selected = selectedSkin == "light",
                onClick = { onSkinSelected("light") }
            )
            ColorChip(
                color = Color(0xFFEA8349), // Medium skin
                selected = selectedSkin == "medium",
                onClick = { onSkinSelected("medium") }
            )
            ColorChip(
                color = Color(0xFF98461A), // Dark skin
                selected = selectedSkin == "dark",
                onClick = { onSkinSelected("dark") }
            )

            // Fantasy skin colors
            ColorChip(
                color = Color(0xFF0FF591), // Mint green
                selected = selectedSkin == "fantasy1",
                onClick = { onSkinSelected("fantasy1") }
            )
            ColorChip(
                color = Color(0xFF800ED0), // Purple
                selected = selectedSkin == "fantasy2",
                onClick = { onSkinSelected("fantasy2") }
            )
        }
    }
}

/**
 * Hair style selection component
 */
@Composable
fun HairStyleSelector(
    gender: String,
    selectedStyle: Int,
    onHairStyleSelected: (Int) -> Unit
) {
    // Define hair style options based on gender with explicit resource mapping
    val hairStyles = remember(gender) {
        if (gender == "Male") {
            listOf(
                Triple("Short 1", R.drawable.creator_hair_bangs_1_black, Color.White),
                Triple("Short 2", R.drawable.creator_hair_bangs_2_black, Color.White),
                Triple("Short 3", R.drawable.creator_hair_bangs_3_black, Color.White)
            )
        } else {
            listOf(
                Triple("Wavy", R.drawable.creator_hair_bangs_1_white, Color.White),
                Triple("Classic", R.drawable.creator_hair_bangs_2_blond, Color.White)
            )
        }
    }

    Column {
        Text(
            text = "Hair Style",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(hairStyles) { index, (styleName, resourceId, bgColor) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .clickable { onHairStyleSelected(index) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 2.dp,
                                color = if (selectedStyle == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(bgColor)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (resourceId != 0) {
                            Image(
                                painter = painterResource(id = resourceId),
                                contentDescription = styleName,
                                modifier = Modifier.fillMaxSize(),
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
                        text = styleName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Hair color selection component
 */
@Composable
fun HairColorSelector(selectedColor: String, onHairColorSelected: (String) -> Unit) {
    Column {
        Text("Hair Color", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            ColorChip(
                color = Color(0xFFFBEC5D), // Blond
                selected = selectedColor == "blond",
                onClick = { onHairColorSelected("blond") }
            )
            ColorChip(
                color = Color(0xFF8B4513), // Brown
                selected = selectedColor == "brown",
                onClick = { onHairColorSelected("brown") }
            )
            ColorChip(
                color = Color(0xFFFF4500), // Red
                selected = selectedColor == "red",
                onClick = { onHairColorSelected("red") }
            )
            ColorChip(
                color = Color(0xFFF5F5F5), // White
                selected = selectedColor == "white",
                onClick = { onHairColorSelected("white") }
            )
        }
    }
}

/**
 * Outfit selection component with expanded options for each class
 */
@Composable
fun OutfitSelector(
    wizardClass: WizardClass,
    selectedOutfit: String,
    onOutfitSelected: (String) -> Unit
) {
    // Expanded class-specific outfits
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

/**
 * Reusable selection chip component for gender selection
 */
@Composable
fun SelectionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Reusable color chip component for color selection
 */
@Composable
fun ColorChip(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(
                width = 2.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(color)
        )
    }
}

/**
 * Get outfit resource ID directly
 */
fun getOutfitResourceId(wizardClass: WizardClass, outfit: String, gender: String): Int {
    val outfitType = if (gender == "Male") "broad" else "slim"

    return when (wizardClass) {
        WizardClass.CHRONOMANCER -> when (outfit) {
            "Astronomer Robe" -> if (gender == "Male")
                R.drawable.broad_armor_special_snow
            else
                R.drawable.slim_armor_special_snow
            "Thunder Cloak" -> if (gender == "Male")
                R.drawable.broad_shirt_thunder
            else
                R.drawable.slim_shirt_thunder
            else -> if (gender == "Male")
                R.drawable.broad_armor_special_snow
            else
                R.drawable.slim_armor_special_snow
        }

        WizardClass.LUMINARI -> when (outfit) {
            "Crystal Robe" -> if (gender == "Male")
                R.drawable.broad_armor_armoire_crystal_robe
            else
                R.drawable.slim_armor_armoire_crystal_robe
            "Blue Shirt" -> if (gender == "Male")
                R.drawable.broad_shirt_blue
            else
                R.drawable.broad_shirt_blue
            else -> if (gender == "Male")
                R.drawable.broad_armor_armoire_crystal_robe
            else
                R.drawable.slim_armor_armoire_crystal_robe
        }

        WizardClass.DRACONIST -> when (outfit) {
            "Flame Costume" -> if (gender == "Male")
                R.drawable.broad_armor_armoire_barrister_robe
            else
                R.drawable.slim_armor_armoire_barrister_robe
            "Ram Fleece" -> if (gender == "Male")
                R.drawable.broad_armor_armoire_ram_fleece
            else
                R.drawable.broad_armor_armoire_ram_fleece // Use male version for female
            "Black Shirt" -> if (gender == "Male")
                R.drawable.broad_shirt_black
            else
                R.drawable.broad_shirt_black // Use male version for female
            "Rainbow Shirt" -> if (gender == "Male")
                R.drawable.broad_shirt_rainbow
            else
                R.drawable.broad_shirt_rainbow // Use male version for female
            else -> if (gender == "Male")
                R.drawable.broad_armor_armoire_barrister_robe
            else
                R.drawable.slim_armor_armoire_barrister_robe
        }

        WizardClass.MYSTWEAVER -> when (outfit) {
            "Mystic Robe" -> if (gender == "Male")
                R.drawable.broad_armor_special_pyromancer
            else
                R.drawable.slim_armor_special_pyromancer
            "Blue Shirt" -> if (gender == "Male")
                R.drawable.broad_shirt_blue
            else
                R.drawable.broad_shirt_blue // Use male version for female
            else -> if (gender == "Male")
                R.drawable.broad_armor_special_pyromancer
            else
                R.drawable.slim_armor_special_pyromancer
        }
    }
}

/**
 * Helper function to get the drawable resource ID by name
 */
private fun getDrawableResourceId(name: String): Int {
    return when (name) {
        // Armor/Robes - Broad
        "broad_armor_armoire_alchemist" -> R.drawable.broad_armor_armoire_alchemist
        "broad_armor_armoire_astronomer_robe" -> R.drawable.broad_armor_armoire_astronomer_robe
        "broad_armor_armoire_barrister_robe" -> R.drawable.broad_armor_armoire_barrister_robe
        "broad_armor_armoire_blue_moon" -> R.drawable.broad_armor_armoire_blue_moon
        "broad_armor_armoire_crystal_robe" -> R.drawable.broad_armor_armoire_crystal_robe
        "broad_armor_armoire_ram_fleece" -> R.drawable.broad_armor_armoire_ram_fleece
        "broad_armor_armoire_chrono" -> R.drawable.broad_armor_armoire_chrono
        "broad_armor_special_snow" -> R.drawable.broad_armor_special_snow
        "broad_armor_special_pyromancer" -> R.drawable.broad_armor_special_pyromancer

        // Armor/Robes - Slim
        "slim_armor_armoire_spage_robe" -> R.drawable.slim_armor_armoire_spage_robe
        "slim_armor_special_pyromancer" -> R.drawable.slim_armor_special_pyromancer
        "slim_armor_special_snow" -> R.drawable.slim_armor_special_snow
        "slim_armor_armoire_crystal_robe" -> R.drawable.slim_armor_armoire_crystal_robe
        "slim_armor_armoire_chrono" -> R.drawable.slim_armor_armoire_chrono
        "slim_armor_armoire_barrister_robe" -> R.drawable.slim_armor_armoire_barrister_robe

        // Shirts - Broad
        "broad_shirt_black" -> R.drawable.broad_shirt_black
        "broad_shirt_blue" -> R.drawable.broad_shirt_blue
        "broad_shirt_horizon" -> R.drawable.broad_shirt_horizon
        "broad_shirt_rainbow" -> R.drawable.broad_shirt_rainbow
        "broad_shirt_thunder" -> R.drawable.broad_shirt_thunder
        "creator_broad_shirt_black" -> R.drawable.creator_broad_shirt_black
        "creator_broad_shirt_blue" -> R.drawable.creator_broad_shirt_blue
        "creator_broad_shirt_green" -> R.drawable.creator_broad_shirt_green
        "creator_broad_shirt_pink" -> R.drawable.creator_broad_shirt_pink
        "creator_broad_shirt_white" -> R.drawable.creator_broad_shirt_white
        "creator_broad_shirt_yellow" -> R.drawable.creator_broad_shirt_yellow

        // Shirts - Slim
        "creator_slim_shirt_black" -> R.drawable.creator_slim_shirt_black
        "creator_slim_shirt_blue" -> R.drawable.creator_slim_shirt_blue
        "creator_slim_shirt_green" -> R.drawable.creator_slim_shirt_green
        "creator_slim_shirt_pink" -> R.drawable.creator_slim_shirt_pink
        "creator_slim_shirt_white" -> R.drawable.creator_slim_shirt_white
        "creator_slim_shirt_yellow" -> R.drawable.creator_slim_shirt_yellow
        "slim_shirt_cross" -> R.drawable.slim_shirt_cross
        "slim_shirt_thunder" -> R.drawable.slim_shirt_thunder
        "slim_shirt_zombie" -> R.drawable.slim_shirt_zombie

        // Hair - Bangs styles
        "creator_hair_bangs_1_black" -> R.drawable.creator_hair_bangs_1_black
        "creator_hair_bangs_1_blond" -> R.drawable.creator_hair_bangs_1_blond
        "creator_hair_bangs_1_brown" -> R.drawable.creator_hair_bangs_1_brown
        "creator_hair_bangs_1_red" -> R.drawable.creator_hair_bangs_1_red
        "creator_hair_bangs_1_white" -> R.drawable.creator_hair_bangs_1_white
        "creator_hair_bangs_2_black" -> R.drawable.creator_hair_bangs_2_black
        "creator_hair_bangs_2_blond" -> R.drawable.creator_hair_bangs_2_blond
        "creator_hair_bangs_2_brown" -> R.drawable.creator_hair_bangs_2_brown
        "creator_hair_bangs_2_red" -> R.drawable.creator_hair_bangs_2_red
        "creator_hair_bangs_2_white" -> R.drawable.creator_hair_bangs_2_white
        "creator_hair_bangs_3_blond" -> R.drawable.creator_hair_bangs_3_blond
        "creator_hair_bangs_3_brown" -> R.drawable.creator_hair_bangs_3_brown
        "creator_hair_bangs_3_red" -> R.drawable.creator_hair_bangs_3_red
        "creator_hair_bangs_3_white" -> R.drawable.creator_hair_bangs_3_white

        // Skins - Regular
        "skin_ea8349" -> R.drawable.skin_ea8349
        "skin_ea8349_sleep" -> R.drawable.skin_ea8349_sleep
        "skin_f5a76e" -> R.drawable.skin_f5a76e
        "skin_f5a76e_sleep" -> R.drawable.skin_f5a76e_sleep
        "skin_91553" -> R.drawable.skin_915533
        "skin_98461a" -> R.drawable.skin_98461a
        "skin_98461a_sleep" -> R.drawable.skin_98461a_sleep

        // Skins - Fantasy
        "skin_0ff591" -> R.drawable.skin_0ff591
        "skin_0ff591_sleep" -> R.drawable.skin_0ff591_sleep
        "skin_800ed0" -> R.drawable.skin_800ed0
        "skin_800ed0_sleep" -> R.drawable.skin_800ed0_sleep

        // potion
        "potion" -> R.drawable.potion

        else -> 0 // Default or fallback
    }
}