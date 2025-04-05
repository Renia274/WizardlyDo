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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.R
import com.example.wizardlydo.data.WizardClass
import com.example.wizardlydo.data.models.CustomizationState


/**
 * Character preview component showing the customized wizard
 */
@Composable
fun WizardPreview(state: CustomizationState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        // 1. Load skin based on skin color
        val skinResourceName = when (state.skinColor) {
            "light" -> "skin_f5a76e"  // Light skin
            "medium" -> "skin_ea8349" // Medium skin
            "dark" -> "skin_98461a"   // Dark skin
            "fantasy1" -> "skin_0ff591" // Mint green skin
            "fantasy2" -> "skin_800ed0" // Purple skin
            else -> "skin_f5a76e"     // Default light skin
        }

        val skinResId = getDrawableResourceId(skinResourceName)
        if (skinResId != 0) {
            Image(
                painter = painterResource(id = skinResId),
                contentDescription = "Character Skin",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 2. Load outfit based on class and selection
        val outfitResourceName = getOutfitResourceName(state.wizardClass, state.outfit, state.gender)
        val outfitResId = getDrawableResourceId(outfitResourceName)
        if (outfitResId != 0) {
            Image(
                painter = painterResource(id = outfitResId),
                contentDescription = "Character Outfit",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 3. Load hairstyle based on gender and selection
        val hairResourceName = getHairResourceName(state.gender, state.hairStyle, state.hairColor)
        val hairResId = getDrawableResourceId(hairResourceName)
        if (hairResId != 0) {
            Image(
                painter = painterResource(id = hairResId),
                contentDescription = "Character Hair",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 4. Load accessory based on class and selection
        val accessoryResourceName = getAccessoryResourceName(state.wizardClass, state.accessory)
        val accessoryResId = getDrawableResourceId(accessoryResourceName)
        if (accessoryResId != 0) {
            Image(
                painter = painterResource(id = accessoryResId),
                contentDescription = "Character Accessory",
                modifier = Modifier.align(Alignment.Center)
            )
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
        "broad_armor_armoire_blue_star" -> R.drawable.broad_armor_armoire_blue_star

        // Armor/Robes - Slim
        "slim_armor_armoire_spage_robe" -> R.drawable.slim_armor_armoire_spage_robe
        "slim_armor_special_pyromanmcersRobes" -> R.drawable.slim_armor_special_pyromancer
        "slim_armor_special_snowSovereignRobes" -> R.drawable.slim_armor_special_snow

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

        // Eyewear
        "creator_eyewear_special_blacktopframe" -> R.drawable.creator_eyewear_special_blacktopframe
        "creator_eyewear_special_bluetopframe" -> R.drawable.creator_eyewear_special_bluetopframe
        "creator_eyewear_special_greentopframe" -> R.drawable.creator_eyewear_special_greentopframe
        "creator_eyewear_special_pinktopframe" -> R.drawable.creator_eyewear_special_pinktopframe
        "creator_eyewear_special_redtopframe" -> R.drawable.creator_eyewear_special_redtopframe
        "creator_eyewear_special_whitetopframe" -> R.drawable.creator_eyewear_special_whitetopframe
        "creator_eyewear_special_yellowtopframe" -> R.drawable.creator_eyewear_special_yellowtopframe

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
        "creator_hair_bangs_3_black" -> R.drawable.creator_hair_bangs_3_black
        "creator_hair_bangs_3_blond" -> R.drawable.creator_hair_bangs_3_blond
        "creator_hair_bangs_3_brown" -> R.drawable.creator_hair_bangs_3_brown
        "creator_hair_bangs_3_red" -> R.drawable.creator_hair_bangs_3_red
        "creator_hair_bangs_3_white" -> R.drawable.creator_hair_bangs_3_white

        // Hair - Base styles
        "creator_hair_base_1_black" -> R.drawable.creator_hair_base_1_black
        "creator_hair_base_1_blond" -> R.drawable.creator_hair_base_1_blond
        "creator_hair_base_1_brown" -> R.drawable.creator_hair_base_1_brown
        "creator_hair_base_1_red" -> R.drawable.creator_hair_base_1_red
        "creator_hair_base_1_white" -> R.drawable.creator_hair_base_1_white
        "creator_hair_base_3_black" -> R.drawable.creator_hair_base_3_black
        "creator_hair_base_3_blond" -> R.drawable.creator_hair_base_3_blond
        "creator_hair_base_3_brown" -> R.drawable.creator_hair_base_3_brown
        "creator_hair_base_3_red" -> R.drawable.creator_hair_base_3_red
        "creator_hair_base_3_white" -> R.drawable.creator_hair_base_3_white

        // Hair - Flower styles
        "creator_hair_flower_1" -> R.drawable.creator_hair_flower_1
        "creator_hair_flower_2" -> R.drawable.creator_hair_flower_2
        "creator_hair_flower_3" -> R.drawable.creator_hair_flower_3
        "creator_hair_flower_4" -> R.drawable.creator_hair_flower_4
        "creator_hair_flower_5" -> R.drawable.creator_hair_flower_5
        "creator_hair_flower_6" -> R.drawable.creator_hair_flower_6

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

/**
 * Get the appropriate hair resource based on gender, style, and color
 */
private fun getHairResourceName(gender: String, hairStyle: Int, hairColor: String): String {
    val hairColorSuffix = when (hairColor) {
        "black" -> "black"
        "blond" -> "blond"
        "brown" -> "brown"
        "red" -> "red"
        "white" -> "white"
        else -> "black"
    }

    // Different hair styles based on gender
    return if (gender == "Male") {
        when (hairStyle) {
            0 -> "creator_hair_bangs_1_$hairColorSuffix"
            1 -> "creator_hair_bangs_2_$hairColorSuffix"
            2 -> "creator_hair_bangs_3_$hairColorSuffix"
            3 -> "creator_hair_base_1_$hairColorSuffix"
            4 -> "creator_hair_base_3_$hairColorSuffix"
            else -> "creator_hair_bangs_1_$hairColorSuffix"
        }
    } else {
        when (hairStyle) {
            0 -> "creator_hair_bangs_1_$hairColorSuffix"
            1 -> "creator_hair_bangs_2_$hairColorSuffix"
            2 -> "creator_hair_bangs_3_$hairColorSuffix"
            3 -> "creator_hair_base_1_$hairColorSuffix"
            4 -> "creator_hair_base_3_$hairColorSuffix"
            5 -> "creator_hair_flower_1"
            6 -> "creator_hair_flower_2"
            7 -> "creator_hair_flower_3"
            8 -> "creator_hair_flower_4"
            9 -> "creator_hair_flower_5"
            10 -> "creator_hair_flower_6"
            else -> "creator_hair_bangs_1_$hairColorSuffix"
        }
    }
}

/**
 * Get the appropriate accessory resource based on wizard class and selection
 */
private fun getAccessoryResourceName(wizardClass: WizardClass, accessory: String): String {
    return when (wizardClass) {
        WizardClass.CHRONOMANCER -> "creator_eyewear_special_bluetopframe"
        WizardClass.LUMINARI -> "creator_eyewear_special_yellowtopframe"
        WizardClass.DRACONIST -> "creator_eyewear_special_redtopframe"
        WizardClass.MYSTWEAVER -> "creator_eyewear_special_blacktopframe"
    }
}

/**
 * Get the appropriate outfit resource based on wizard class, selection, and gender
 */
private fun getOutfitResourceName(wizardClass: WizardClass, outfit: String, gender: String): String {
    val outfitType = if (gender == "Male") "broad" else "slim"

    return when (wizardClass) {
        WizardClass.CHRONOMANCER -> "${outfitType}_armor_armoire_astronomerStobe"
        WizardClass.LUMINARI -> "${outfitType}_armor_armoire_crystalCrescentRobes"
        WizardClass.DRACONIST -> "${outfitType}_armor_armoire_shootingStarCostume"
        WizardClass.MYSTWEAVER -> "${outfitType}_armor_special_pyromanmcersRobes"
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
fun HairStyleSelector(gender: String, selectedStyle: Int, onHairStyleSelected: (Int) -> Unit) {
    // Different hairstyles based on gender
    val hairStyles = if (gender == "Male") {
        listOf(
            "Bangs 1" to "creator_hair_bangs_1_black",
            "Bangs 2" to "creator_hair_bangs_2_black",
            "Bangs 3" to "creator_hair_bangs_3_black",
            "Base 1" to "creator_hair_base_1_black",
            "Base 3" to "creator_hair_base_3_black"
        )
    } else {
        listOf(
            "Bangs 1" to "creator_hair_bangs_1_black",
            "Bangs 2" to "creator_hair_bangs_2_black",
            "Bangs 3" to "creator_hair_bangs_3_black",
            "Base 1" to "creator_hair_base_1_black",
            "Base 3" to "creator_hair_base_3_black",
            "Flower 1" to "creator_hair_flower_1",
            "Flower 2" to "creator_hair_flower_2",
            "Flower 3" to "creator_hair_flower_3",
            "Flower 4" to "creator_hair_flower_4",
            "Flower 5" to "creator_hair_flower_5",
            "Flower 6" to "creator_hair_flower_6"
        )
    }

    Column {
        Text("Hair Style", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(hairStyles.size) { index ->
                val (styleName, resourceName) = hairStyles[index]
                val resourceId = getDrawableResourceId(resourceName)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (selectedStyle == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onHairStyleSelected(index) }
                        .padding(4.dp)
                ) {
                    if (resourceId != 0) {
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = styleName,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Text(
                        text = styleName,
                        style = MaterialTheme.typography.bodySmall
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
                color = Color.Black,
                selected = selectedColor == "black",
                onClick = { onHairColorSelected("black") }
            )
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
 * Accessory selection component - simplified for wizard classes
 */
@Composable
fun AccessorySelector(
    wizardClass: WizardClass,
    selectedAccessory: String,
    onAccessorySelected: (String) -> Unit
) {
    // Class-specific accessories
    val accessories = when (wizardClass) {
        WizardClass.CHRONOMANCER -> listOf("Time Glasses" to "creator_eyewear_special_bluetopframe")
        WizardClass.LUMINARI -> listOf("Light Mask" to "creator_eyewear_special_yellowtopframe")
        WizardClass.DRACONIST -> listOf("Dragon Eyes" to "creator_eyewear_special_redtopframe")
        WizardClass.MYSTWEAVER -> listOf("Arcane Monocle" to "creator_eyewear_special_blacktopframe")
    }

    Column {
        Text("Class Accessory", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            accessories.forEachIndexed { index, (name, resourceName) ->
                val resourceId = getDrawableResourceId(resourceName)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(100.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (selectedAccessory == name) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onAccessorySelected(name) }
                        .padding(4.dp)
                ) {
                    if (resourceId != 0) {
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = name,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Outfit selection component - based on wizard class
 */
@Composable
fun OutfitSelector(
    wizardClass: WizardClass,
    selectedOutfit: String,
    onOutfitSelected: (String) -> Unit
) {
    // Class-specific outfits
    val outfits = when (wizardClass) {
        WizardClass.CHRONOMANCER -> listOf(
            "Astronomer Robe" to "broad_armor_armoire_astronomerStobe"
        )
        WizardClass.LUMINARI -> listOf(
            "Crystal Robe" to "broad_armor_armoire_crystalCrescentRobes"
        )
        WizardClass.DRACONIST -> listOf(
            "Flame Costume" to "broad_armor_armoire_shootingStarCostume"
        )
        WizardClass.MYSTWEAVER -> listOf(
            "Mystic Robe" to "broad_armor_special_pyromanmcersRobes"
        )
    }

    Column {
        Text("Class Outfit", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            outfits.forEachIndexed { index, (name, resourceName) ->
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
                    }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall
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