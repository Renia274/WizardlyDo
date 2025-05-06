package com.example.wizardlydo.data.inventory

import com.example.wizardlydo.R
import com.example.wizardlydo.data.wizard.WizardClass
import com.example.wizardlydo.room.inventory.InventoryItemEntity


enum class ItemType {
    OUTFIT,
    BACKGROUND,
    ACCESSORY,
    WEAPON
}

object InventoryItems {
    // Mystweaver outfits
    val mystweaverOutfits = listOf(
        InventoryItemEntity(
            id = "mystweaver_outfit_1",
            wizardId = "",
            itemId = "mystic_robe",
            itemType = ItemType.OUTFIT.toString(),
            isUnlocked = true,
            isEquipped = true,
            unlockLevel = 1,
            resourceId = R.drawable.broad_armor_special_pyromancer,
            name = "Mystic Robe",
            description = "Default Mystweaver outfit"
        ),
        InventoryItemEntity(
            id = "mystweaver_outfit_2",
            wizardId = "",
            itemId = "storm_cloak",
            itemType = ItemType.OUTFIT.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 5,
            resourceId = R.drawable.broad_armor_special_snow,
            name = "Storm Cloak",
            description = "Powerful wizard cloak"
        ),
        InventoryItemEntity(
            id = "mystweaver_outfit_3",
            wizardId = "",
            itemId = "crystal_armor",
            itemType = ItemType.OUTFIT.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 10,
            resourceId = R.drawable.broad_armor_armoire_crystal_robe,
            name = "Crystal Armor",
            description = "Rare crystal enchanted armor"
        )
    )

    val dragonistOutfits = listOf(
        InventoryItemEntity(
            id = "dragonist_outfit_2",
            wizardId = "",
            itemId = "dragon_scale",
            itemType = ItemType.OUTFIT.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 7,
            resourceId = R.drawable.broad_armor_armoire_ram_fleece,
            name = "Dragon Scale Armor",
            description = "Rare dragon scale armor"
        )
    )

    val luminariOutfits = listOf(
        InventoryItemEntity(
            id = "luminari_outfit_1",
            wizardId = "",
            itemId = "crystal_robe",
            itemType = ItemType.OUTFIT.toString(),
            isUnlocked = true,
            isEquipped = true,
            unlockLevel = 1,
            resourceId = R.drawable.broad_armor_armoire_crystal_robe,
            name = "Crystal Robe",
            description = "Default Luminari outfit"
        ),
        InventoryItemEntity(
            id = "luminari_outfit_2",
            wizardId = "",
            itemId = "light_armor",
            itemType = ItemType.OUTFIT.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 6,
            resourceId = R.drawable.broad_armor_special_snow,
            name = "Light Armor",
            description = "Glowing light enchanted armor"
        )
    )

    val chronomancerOutfits = listOf(
        InventoryItemEntity(
            id = "chronomancer_outfit_1",
            wizardId = "",
            itemId = "astronomer_robe",
            itemType = ItemType.OUTFIT.toString(),
            isUnlocked = true,
            isEquipped = true,
            unlockLevel = 1,
            resourceId = R.drawable.broad_armor_special_snow,
            name = "Astronomer Robe",
            description = "Default Chronomancer outfit"
        ),
        InventoryItemEntity(
            id = "chronomancer_outfit_2",
            wizardId = "",
            itemId = "time_cloak",
            itemType = ItemType.OUTFIT.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 8,
            resourceId = R.drawable.broad_armor_special_pyromancer,
            name = "Time Cloak",
            description = "Mystical time-manipulating cloak"
        )
    )


    val commonItems = listOf(
        InventoryItemEntity(
            id = "accessory_wizard_hat",
            wizardId = "",
            itemId = "wizard_hat",
            itemType = ItemType.ACCESSORY.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 15,
            resourceId = R.drawable.wizard_hat,
            name = "Wizard Hat",
            description = "Prestigious wizard's hat for accomplished mages"
        ),
        InventoryItemEntity(
            id = "weapon_golden_staff",
            wizardId = "",
            itemId = "golden_staff",
            itemType = ItemType.WEAPON.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 30,
            resourceId = R.drawable.golden_staff,
            name = "Golden Staff",
            description = "The ultimate staff of power for master wizards"
        )
    )

    val backgrounds = listOf(
        InventoryItemEntity(
            id = "background_1",
            wizardId = "",
            itemId = "forest_background",
            itemType = ItemType.BACKGROUND.toString(),
            isUnlocked = true,
            isEquipped = false,
            unlockLevel = 1,
            resourceId = R.drawable.background_forest,
            name = "Mystic Forest",
            description = "Default background"
        ),
        InventoryItemEntity(
            id = "background_2",
            wizardId = "",
            itemId = "castle_background",
            itemType = ItemType.BACKGROUND.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 3,
            resourceId = R.drawable.background_castle,
            name = "Ancient Castle",
            description = "Majestic castle background"
        ),
        InventoryItemEntity(
            id = "background_3",
            wizardId = "",
            itemId = "space_background",
            itemType = ItemType.BACKGROUND.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 5,
            resourceId = R.drawable.background_space,
            name = "Cosmic Space",
            description = "Mystical space background"
        ),
        InventoryItemEntity(
            id = "background_4",
            wizardId = "",
            itemId = "volcano_background",
            itemType = ItemType.BACKGROUND.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 7,
            resourceId = R.drawable.backgroud_volcano,
            name = "Volcanic Realm",
            description = "Dangerous volcanic area"
        ),
        InventoryItemEntity(
            id = "background_5",
            wizardId = "",
            itemId = "dungeon_background",
            itemType = ItemType.BACKGROUND.toString(),
            isUnlocked = false,
            isEquipped = false,
            unlockLevel = 9,
            resourceId = R.drawable.background_dungeon,
            name = "Dark Dungeon",
            description = "Mysterious dungeon background"
        )
    )

    fun getItemsForClass(wizardClass: WizardClass): List<InventoryItemEntity> {
        return when (wizardClass) {
            WizardClass.MYSTWEAVER -> mystweaverOutfits + backgrounds + commonItems
            WizardClass.DRACONIST -> dragonistOutfits + backgrounds + commonItems
            WizardClass.LUMINARI -> luminariOutfits + backgrounds + commonItems
            WizardClass.CHRONOMANCER -> chronomancerOutfits + backgrounds + commonItems
        }
    }
}