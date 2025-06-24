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

    return when (hairStyle) {
        0 -> {
            val colorSuffix = when (hairColor) {
                "blond" -> "blond"
                "brown" -> "brown"
                "red" -> "red"
                "white" -> "white"
                else -> "white"
            }

            val resourceId = when (colorSuffix) {
                "blond" -> R.drawable.hair_style_1_blond
                "brown" -> R.drawable.hair_style_1_brown
                "red" -> R.drawable.hair_style_1_red
                "white" -> R.drawable.hair_style_1_white
                else -> R.drawable.hair_style_1_white
            }

            resourceId
        }
        1 -> {
            val resourceId = if (gender == "Male") {
                R.drawable.hair_style_spikes
            } else {
                val colorSuffix = when (hairColor) {
                    "blond" -> "blond"
                    "brown" -> "brown"
                    "red" -> "red"
                    "white" -> "white"
                    else -> "white"
                }

                val femaleResourceId = when (colorSuffix) {
                    "blond" -> R.drawable.hair_style_2_blond
                    "brown" -> R.drawable.hair_style_2_brown
                    "red" -> R.drawable.hair_style_2_red
                    "white" -> R.drawable.hair_style_2_white
                    else -> R.drawable.hair_style_2_white
                }

                femaleResourceId
            }

            resourceId
        }
        else -> {
            R.drawable.hair_style_1_white // Default fallback
        }
    }
}


