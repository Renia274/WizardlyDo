package com.wizardlydo.app.screens.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.R
import com.wizardlydo.app.data.wizard.WizardClass
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.data.wizard.items.EquippedItems
import com.wizardlydo.app.data.wizard.items.ItemTypes
import com.wizardlydo.app.room.inventory.InventoryItemEntity
import com.wizardlydo.app.screens.tasks.comps.inventory.BasicCharacterStatsSection
import com.wizardlydo.app.screens.tasks.comps.inventory.InventoryItemsSection
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import com.wizardlydo.app.viewmodel.inventory.InventoryUiState
import com.wizardlydo.app.viewmodel.inventory.InventoryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val currentWizard by viewModel.currentWizardFlow.collectAsState()
    val equippedItems by viewModel.equippedItemsFlow.collectAsState()
    val uiState by viewModel.uiStateFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        InventoryContent(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            wizardProfile = currentWizard,
            onEquipItem = viewModel::equipItem,
            equippedItems = equippedItems
        )
    }
}

@Composable
fun InventoryContent(
    modifier: Modifier = Modifier,
    uiState: InventoryUiState,
    wizardProfile: WizardProfile?,
    onEquipItem: (String) -> Unit,
    equippedItems: EquippedItems
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        wizardProfile?.let { wizard ->
            BasicCharacterStatsSection(
                wizardProfile = wizard,
                modifier = Modifier.padding(16.dp),
                equippedItems = equippedItems
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is InventoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is InventoryUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is InventoryUiState.Success -> {
                    InventoryItemsSection(
                        items = uiState.items,
                        wizardLevel = wizard.level,
                        onEquipItem = onEquipItem
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryContentPreview() {
    WizardlyDoTheme {
        val sampleWizard = WizardProfile(
            userId = "preview-user",
            wizardName = "Eldoria",
            wizardClass = WizardClass.MYSTWEAVER,
            level = 5,
            health = 90,
            maxHealth = 180,  // Level 5: 100 + (4 × 20) = 180
            stamina = 75,
            maxStamina = 104,  // Level 5: 100 + (4 × 1) = 104
            experience = 450,
            totalTasksCompleted = 15,
            gender = "Female",
            skinColor = "light",
            hairColor = "purple",
            hairStyle = "1",
            outfit = "Mystic Robe"
        )


        val sampleEquippedItems = EquippedItems(
            outfit = InventoryItemEntity(
                id = "outfit1",
                wizardId = "preview-user",
                itemId = "mystic_robe",
                itemType = ItemTypes.OUTFIT,
                isUnlocked = true,
                isEquipped = true,
                unlockLevel = 1,
                resourceId = R.drawable.mystweaver_robe_male,
                name = "Mystic Robe",
                description = "Default Mystweaver outfit"
            ),
            background = InventoryItemEntity(
                id = "bg1",
                wizardId = "preview-user",
                itemId = "forest_background",
                itemType = ItemTypes.BACKGROUND,
                isUnlocked = true,
                isEquipped = true,
                unlockLevel = 1,
                resourceId = R.drawable.background_forest,
                name = "Mystic Forest",
                description = "Default background"
            )
        )

        val sampleItems = listOf(
            // Outfits
            InventoryItemEntity(
                id = "outfit1",
                wizardId = "preview-user",
                itemId = "mystic_robe",
                itemType = ItemTypes.OUTFIT,
                isUnlocked = true,
                isEquipped = true,
                unlockLevel = 1,
                resourceId = R.drawable.mystweaver_robe_male,
                name = "Mystic Robe",
                description = "Default Mystweaver outfit"
            ),
            InventoryItemEntity(
                id = "outfit2",
                wizardId = "preview-user",
                itemId = "storm_cloak",
                itemType = ItemTypes.OUTFIT,
                isUnlocked = true,
                isEquipped = false,
                unlockLevel = 5,
                resourceId = R.drawable.chronomancer_robe_male,
                name = "Storm Cloak",
                description = "Powerful wizard cloak"
            ),
            InventoryItemEntity(
                id = "outfit3",
                wizardId = "preview-user",
                itemId = "crystal_armor",
                itemType = ItemTypes.OUTFIT,
                isUnlocked = false,
                isEquipped = false,
                unlockLevel = 10,
                resourceId = R.drawable.luminari_robe_male,
                name = "Crystal Armor",
                description = "Rare crystal enchanted armor"
            ),
            // Backgrounds
            InventoryItemEntity(
                id = "bg1",
                wizardId = "preview-user",
                itemId = "forest_background",
                itemType = ItemTypes.BACKGROUND,
                isUnlocked = true,
                isEquipped = true,
                unlockLevel = 1,
                resourceId = R.drawable.background_forest,
                name = "Mystic Forest",
                description = "Default background"
            ),
            InventoryItemEntity(
                id = "bg2",
                wizardId = "preview-user",
                itemId = "castle_background",
                itemType = ItemTypes.BACKGROUND,
                isUnlocked = true,
                isEquipped = false,
                unlockLevel = 3,
                resourceId = R.drawable.background_castle,
                name = "Ancient Castle",
                description = "Majestic castle background"
            ),
            InventoryItemEntity(
                id = "bg3",
                wizardId = "preview-user",
                itemId = "space_background",
                itemType = ItemTypes.BACKGROUND,
                isUnlocked = false,
                isEquipped = false,
                unlockLevel = 5,
                resourceId = R.drawable.background_space,
                name = "Cosmic Space",
                description = "Mystical space background"
            )
        )

        InventoryContent(
            uiState = InventoryUiState.Success(sampleItems),
            wizardProfile = sampleWizard,
            onEquipItem = {},
            equippedItems = sampleEquippedItems
        )
    }
}
