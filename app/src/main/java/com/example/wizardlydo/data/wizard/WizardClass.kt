package com.example.wizardlydo.data.wizard

enum class WizardClass(
    val title: String,
    val description: String,

) {
    CHRONOMANCER(
        "Chronomancer",
        "Master of time and temporal magic",

    ),
    LUMINARI(
        "Luminari",
        "Wielder of pure light energy",

    ),
    DRACONIST(
        "Draconist",
        "Dragon-kin with fiery power",

    ),
    MYSTWEAVER(
        "Mystweaver",
        "Manipulator of arcane forces",
    );
}