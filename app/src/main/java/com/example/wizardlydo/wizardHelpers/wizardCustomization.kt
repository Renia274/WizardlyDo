package com.example.wizardlydo.wizardHelpers

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
        "black" -> "black"
        "blond" -> "blond"
        "brown" -> "brown"
        "red" -> "red"
        "white" -> "white"
        else -> "black"
    }

    return if (gender == "Male") {
        when (hairStyle) {
            0 -> when (colorSuffix) {
                "black" -> R.drawable.creator_hair_bangs_1_black
                "blond" -> R.drawable.creator_hair_bangs_1_blond
                "brown" -> R.drawable.creator_hair_bangs_1_brown
                "red" -> R.drawable.creator_hair_bangs_1_red
                "white" -> R.drawable.creator_hair_bangs_1_white
                else -> R.drawable.creator_hair_bangs_1_black
            }
            1 -> when (colorSuffix) {
                "black" -> R.drawable.creator_hair_bangs_2_black
                "blond" -> R.drawable.creator_hair_bangs_2_blond
                "brown" -> R.drawable.creator_hair_bangs_2_brown
                "red" -> R.drawable.creator_hair_bangs_2_red
                "white" -> R.drawable.creator_hair_bangs_2_white
                else -> R.drawable.creator_hair_bangs_2_black
            }
            2 -> when (colorSuffix) {
                "black" -> R.drawable.creator_hair_bangs_3_black
                "blond" -> R.drawable.creator_hair_bangs_3_blond
                "brown" -> R.drawable.creator_hair_bangs_3_brown
                "red" -> R.drawable.creator_hair_bangs_3_red
                "white" -> R.drawable.creator_hair_bangs_3_white
                else -> R.drawable.creator_hair_bangs_3_black
            }
            else -> R.drawable.creator_hair_bangs_1_black
        }
    } else { // Female
        when (hairStyle) {
            0 -> when (colorSuffix) { // Wavy
                "black" -> R.drawable.creator_hair_bangs_1_black
                "blond" -> R.drawable.creator_hair_bangs_1_blond
                "brown" -> R.drawable.creator_hair_bangs_1_brown
                "red" -> R.drawable.creator_hair_bangs_1_red
                "white" -> R.drawable.creator_hair_bangs_1_white
                else -> R.drawable.creator_hair_bangs_1_white
            }
            1 -> when (colorSuffix) { // Classic
                "black" -> R.drawable.creator_hair_bangs_2_black
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
 * Helper function to get the drawable resource ID by name
 */
fun getDrawableResourceId(name: String): Int {
    return when (name) {

        // Armor/Robes - Broad

        "broad_armor_armoire_crystal_robe" -> R.drawable.broad_armor_armoire_crystal_robe
        "broad_armor_armoire_ram_fleece" -> R.drawable.broad_armor_armoire_ram_fleece
        "broad_armor_special_snow" -> R.drawable.broad_armor_special_snow
        "broad_armor_special_pyromancer" -> R.drawable.broad_armor_special_pyromancer

        // Armor/Robes - Slim

        "slim_armor_special_pyromancer" -> R.drawable.slim_armor_special_pyromancer
        "slim_armor_special_snow" -> R.drawable.slim_armor_special_snow
        "slim_armor_armoire_crystal_robe" -> R.drawable.slim_armor_armoire_crystal_robe


        // Shirts - Broad

        "broad_shirt_blue" -> R.drawable.broad_shirt_blue
        "broad_shirt_rainbow" -> R.drawable.broad_shirt_rainbow
        "broad_shirt_thunder" -> R.drawable.broad_shirt_thunder


        // Shirts - Slim

        "creator_slim_shirt_blue" -> R.drawable.creator_slim_shirt_blue


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



        else -> 0
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
                R.drawable.broad_shirt_thunder
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
            "Ram Fleece" -> if (gender == "Male")
                R.drawable.broad_armor_armoire_ram_fleece
            else
                R.drawable.broad_armor_armoire_ram_fleece
            "Rainbow Shirt" -> if (gender == "Male")
                R.drawable.broad_shirt_rainbow
            else
                R.drawable.broad_shirt_rainbow
            else -> if (gender == "Male")
                R.drawable.broad_armor_draconist
            else
                R.drawable.slim_armor_draconist
        }

        WizardClass.MYSTWEAVER -> when (outfit) {
            "Mystic Robe" -> if (gender == "Male")
                R.drawable.broad_armor_special_pyromancer
            else
                R.drawable.slim_armor_special_pyromancer
            "Blue Shirt" -> if (gender == "Male")
                R.drawable.broad_shirt_blue
            else
                R.drawable.broad_shirt_blue
            else -> if (gender == "Male")
                R.drawable.broad_armor_special_pyromancer
            else
                R.drawable.slim_armor_special_pyromancer
        }
    }
}



