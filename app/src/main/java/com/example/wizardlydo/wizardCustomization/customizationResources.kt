package com.example.wizardlydo.wizardCustomization

import com.example.wizardlydo.R
import com.example.wizardlydo.data.wizard.WizardClass

fun getSkinResourceId(skinColor: String): Int {
    return when (skinColor) {
        "light" -> R.drawable.skin_f5a76e
        "medium" -> R.drawable.skin_ea8349
        "dark" -> R.drawable.skin_98461a
        "fantasy1" -> R.drawable.skin_0ff591
        "fantasy2" -> R.drawable.skin_800ed0
        else -> R.drawable.skin_f5a76e
    }
}



fun getHairResourceId(gender: String, hairStyle: Int, hairColor: String): Int {
    val colorSuffix = when (hairColor) {
        "blond" -> "blond"
        "brown" -> "brown"
        "red" -> "red"
        "white" -> "white"
        else -> "black"
    }

    return if (gender == "Male") {
        when (hairStyle) {
            0 -> when (colorSuffix) {

                "blond" -> R.drawable.creator_hair_bangs_1_blond
                "brown" -> R.drawable.creator_hair_bangs_1_brown
                "red" -> R.drawable.creator_hair_bangs_1_red
                "white" -> R.drawable.creator_hair_bangs_1_white
                else ->  R.drawable.creator_hair_bangs_1_white
            }
            1 -> when (colorSuffix) {
                "blond" -> R.drawable.creator_hair_bangs_2_blond
                "brown" -> R.drawable.creator_hair_bangs_2_brown
                "red" -> R.drawable.creator_hair_bangs_2_red
                "white" -> R.drawable.creator_hair_bangs_2_white
                else -> R.drawable.creator_hair_bangs_1_white
            }
            2 -> when (colorSuffix) {
                "blond" -> R.drawable.creator_hair_bangs_3_blond
                "brown" -> R.drawable.creator_hair_bangs_3_brown
                "red" -> R.drawable.creator_hair_bangs_3_red
                else -> R.drawable.creator_hair_bangs_1_white
            }
            else ->R.drawable.creator_hair_bangs_1_white
        }
    } else { // Female
        when (hairStyle) {
            0 -> when (colorSuffix) { // Wavy
                "blond" -> R.drawable.creator_hair_bangs_1_blond
                "brown" -> R.drawable.creator_hair_bangs_1_brown
                "red" -> R.drawable.creator_hair_bangs_1_red
                "white" -> R.drawable.creator_hair_bangs_1_white
                else -> R.drawable.creator_hair_bangs_1_white
            }
            1 -> when (colorSuffix) { // Classic
                "blond" -> R.drawable.creator_hair_bangs_2_blond
                "brown" -> R.drawable.creator_hair_bangs_2_brown
                "red" -> R.drawable.creator_hair_bangs_2_red
                "white" -> R.drawable.creator_hair_bangs_2_white
                else -> R.drawable.creator_hair_bangs_1_blond
            }
            else -> R.drawable.creator_hair_bangs_1_white
        }
    }
}



/**
 * Get outfit resource
 */

fun getOutfitResource(wizardClass: WizardClass, outfit: String, gender: String): Int {
    val isMale = gender == "Male"
    val normalizedOutfit = outfit.trim()

    return when (wizardClass) {
        WizardClass.MYSTWEAVER -> {
            // For MYSTWEAVER, explicitly handle each case
            when {
                normalizedOutfit.equals("Mystic Robe", ignoreCase = true) -> {
                    if (isMale) R.drawable.broad_armor_special_pyromancer
                    else R.drawable.slim_armor_special_pyromancer
                }

                normalizedOutfit.equals("Ram Fleece", ignoreCase = true) -> {
                    if (isMale) R.drawable.broad_armor_ram_fleece_robe
                    else R.drawable.slim_armor_ram_fleece_robe
                }

                normalizedOutfit.equals("Rainbow Shirt", ignoreCase = true) -> {
                    if (isMale) R.drawable.broad_shirt_rainbow
                    else R.drawable.slim_shirt_rainbow
                }

                normalizedOutfit.isEmpty() -> {
                    if (isMale) R.drawable.broad_armor_ram_fleece_robe
                    else R.drawable.slim_armor_ram_fleece_robe
                }

                else -> {
                    if (isMale) R.drawable.broad_armor_ram_fleece_robe
                    else R.drawable.slim_armor_ram_fleece_robe
                }
            }
        }

        WizardClass.CHRONOMANCER -> when (normalizedOutfit) {
            "Astronomer Robe" -> if (isMale)
                R.drawable.broad_armor_special_snow
            else
                R.drawable.slim_armor_special_snow


            else -> if (isMale)
                R.drawable.broad_armor_special_snow
            else
                R.drawable.slim_armor_special_snow
        }

        WizardClass.LUMINARI -> when (normalizedOutfit) {
            "Crystal Robe" -> if (isMale)
                R.drawable.broad_armor_armoire_crystal_robe
            else
                R.drawable.slim_armor_armoire_crystal_robe


            else -> if (isMale)
                R.drawable.broad_armor_armoire_crystal_robe
            else
                R.drawable.slim_armor_armoire_crystal_robe
        }

        WizardClass.DRACONIST -> when (normalizedOutfit) {
            "Flame Robe" -> if (isMale)
                R.drawable.broad_armor_draconist
            else
                R.drawable.slim_armor_draconist

            "Ram Fleece" -> if (isMale)
                R.drawable.broad_armor_ram_fleece_robe
            else
                R.drawable.slim_armor_ram_fleece_robe

            else -> if (isMale)
                R.drawable.broad_armor_ram_fleece_robe
            else
                R.drawable.slim_armor_ram_fleece_robe
        }
    }
}


fun getMystweaverOutfitResource(outfit: String, gender: String): Int {
    val trimmedOutfit = outfit.trim()
    val isMale = gender == "Male"

    return when {
        trimmedOutfit.equals("Ram Fleece", ignoreCase = true) -> {
            if (isMale) R.drawable.broad_armor_ram_fleece_robe
            else R.drawable.slim_armor_ram_fleece_robe
        }
        trimmedOutfit.equals("Rainbow Shirt", ignoreCase = true) -> {
            if (isMale) R.drawable.broad_shirt_rainbow
            else R.drawable.slim_shirt_rainbow
        }
        trimmedOutfit.equals("Mystic Robe", ignoreCase = true) -> {
            if (isMale) R.drawable.broad_armor_special_pyromancer
            else R.drawable.slim_armor_special_pyromancer
        }
        else -> {
            // If unknown outfit, default to Ram Fleece
            if (isMale) R.drawable.broad_armor_ram_fleece_robe
            else R.drawable.slim_armor_ram_fleece_robe
        }
    }
}