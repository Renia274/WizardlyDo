package com.example.wizardlydo.data.wizard

enum class WizardClass(
    val title: String,
    val description: String,
    val bodyColor: String,
    val clothingColor: String,
    val weaponColor: String
) {
    CHRONOMANCER(
        "Chronomancer",
        "Master of time and temporal magic",
        "#FFD700",  // Gold
        "#2E0854",  // Deep purple
        "#2E86C1"   // Cerulean blue
    ),
    LUMINARI(
        "Luminari",
        "Wielder of pure light energy",
        "#F4D03F",  // Saffron
        "#21618C",  // Navy blue
        "#F4D03F"   // Yellow
    ),
    DRACONIST(
        "Draconist",
        "Dragon-kin with fiery power",
        "#C0392B",  // Red
        "#2C3E50",  // Charcoal
        "#C0392B"   // Red
    ),
    MYSTWEAVER(
        "Mystweaver",
        "Manipulator of arcane forces",
        "#8E44AD",  // Purple
        "#1A5276",  // Dark blue
        "#8E44AD"   // Purple
    );
}