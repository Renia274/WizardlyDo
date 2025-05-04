package com.example.wizardlydo.comps

import com.example.wizardlydo.R
import com.example.wizardlydo.data.WizardClass

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
