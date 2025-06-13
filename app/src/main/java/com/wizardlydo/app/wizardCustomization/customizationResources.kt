package com.wizardlydo.app.wizardCustomization

import com.wizardlydo.app.R

fun getSkinResourceId(skinColor: String): Int {
    return when (skinColor) {
        "light" -> R.drawable.skin_light
        "medium" -> R.drawable.skin_medium
        "dark" -> R.drawable.skin_dark
        "fantasy1" -> R.drawable.skin_fantasy1
        "fantasy2" -> R.drawable.skin_fantasy2
        else -> R.drawable.skin_light
    }
}



fun getHairResourceId(gender: String, hairStyle: Int, hairColor: String): Int {
    val colorSuffix = when (hairColor) {
        "blond" -> "blond"
        "brown" -> "brown"
        "red" -> "red"
        "white" -> "white"
        else -> "white"
    }

    return when (hairStyle) {
        0 -> when (colorSuffix) {
            "blond" -> R.drawable.hair_style_1_blond
            "brown" -> R.drawable.hair_style_1_brown
            "red" -> R.drawable.hair_style_1_red
            "white" -> R.drawable.hair_style_1_white
            else -> R.drawable.hair_style_1_white
        }
        1 -> when (colorSuffix) {
            "blond" -> R.drawable.hair_style_2_blond
            "brown" -> R.drawable.hair_style_2_brown
            "red" -> R.drawable.hair_style_2_red
            "white" -> R.drawable.hair_style_2_white
            else -> R.drawable.hair_style_2_white
        }
        2 -> when (colorSuffix) {
            "blond" -> R.drawable.hair_style_3_blond
            "brown" -> R.drawable.hair_style_3_brown
            "red" -> R.drawable.hair_style_3_red
            "white" -> R.drawable.hair_style_3_white
            else -> R.drawable.hair_style_3_white
        }
        else -> R.drawable.hair_style_1_white
    }
}



fun getMystweaverOutfitResource(outfit: String, gender: String): Int {
    val trimmedOutfit = outfit.trim()
    val isMale = gender == "Male"

    return when {
        trimmedOutfit.equals("Winter Coat", ignoreCase = true) -> {
            if (isMale) R.drawable.winter_coat_male
            else R.drawable.winter_coat_female
        }
        trimmedOutfit.equals("Casual Shirt", ignoreCase = true) -> {
            if (isMale) R.drawable.casual_shirt_male
            else R.drawable.casual_shirt_female
        }
        trimmedOutfit.equals("Mystic Robe", ignoreCase = true) -> {
            if (isMale) R.drawable.mystweaver_robe_male
            else R.drawable.mystweaver_robe_female
        }
        else -> {
            // If unknown outfit, default to Winter Coat
            if (isMale) R.drawable.winter_coat_male
            else R.drawable.winter_coat_female
        }
    }
}
