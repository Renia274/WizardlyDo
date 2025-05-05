package com.example.wizardlydo.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.comps.items.EquippedItems
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.data.inventory.ItemType
import com.example.wizardlydo.repository.inventory.InventoryRepository
import com.example.wizardlydo.repository.wizard.WizardRepository
import com.example.wizardlydo.room.inventory.InventoryItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed class InventoryUiState {
    data object Loading : InventoryUiState()
    data class Success(val items: List<InventoryItemEntity>) : InventoryUiState()
    data class Error(val message: String) : InventoryUiState()
}


@KoinViewModel
class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val wizardRepository: WizardRepository
) : ViewModel() {

    val uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val uiStateFlow: StateFlow<InventoryUiState> = uiState.asStateFlow()

    private val currentWizard = MutableStateFlow<WizardProfile?>(null)
    val currentWizardFlow: StateFlow<WizardProfile?> = currentWizard.asStateFlow()

    private val equippedItems = MutableStateFlow(EquippedItems())
    val equippedItemsFlow: StateFlow<EquippedItems> = equippedItems.asStateFlow()

    init {
        loadWizardProfile()
    }

    private fun loadWizardProfile() {
        viewModelScope.launch {
            try {
                val userId = wizardRepository.getCurrentUserId()
                if (userId != null) {
                    wizardRepository.getWizardProfile(userId).onSuccess { wizard ->
                        wizard?.let {
                            currentWizard.value = it
                            checkAndInitializeInventory(it)
                            loadInventory(it)
                        } ?: run {
                            uiState.value = InventoryUiState.Error("Wizard profile not found")
                        }
                    }.onFailure { error ->
                        uiState.value = InventoryUiState.Error("Failed to load wizard: ${error.message}")
                    }
                } else {
                    uiState.value = InventoryUiState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Error loading wizard profile", e)
                uiState.value = InventoryUiState.Error("Error: ${e.message}")
            }
        }
    }

    private suspend fun checkAndInitializeInventory(wizard: WizardProfile) {
        try {
            val existingItems = inventoryRepository.getInventoryItems(wizard.userId).first()
            if (existingItems.isEmpty()) {
                inventoryRepository.initializeInventory(wizard)
            }
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error initializing inventory", e)
            uiState.value = InventoryUiState.Error("Failed to initialize inventory")
        }
    }

    private fun loadInventory(wizard: WizardProfile) {
        viewModelScope.launch {
            try {
                // Auto-unlock items based on level
                inventoryRepository.autoUnlockItemsByLevel(wizard.userId, wizard.level)

                inventoryRepository.getInventoryItems(wizard.userId).collect { items ->
                    // Update equipped items state
                    val equippedOutfit = items.firstOrNull {
                        it.itemType == ItemType.OUTFIT.toString() && it.isEquipped
                    }
                    val equippedBackground = items.firstOrNull {
                        it.itemType == ItemType.BACKGROUND.toString() && it.isEquipped
                    }

                    equippedItems.value = EquippedItems(
                        outfit = equippedOutfit,
                        background = equippedBackground
                    )

                    uiState.value = InventoryUiState.Success(items)
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Error loading inventory", e)
                uiState.value = InventoryUiState.Error("Failed to load inventory")
            }
        }
    }

    fun equipItem(itemId: String) {
        val wizard = currentWizard.value ?: return

        viewModelScope.launch {
            try {
                inventoryRepository.equipItem(wizard.userId, itemId)

                // Only update wizard outfit for outfit items, not backgrounds
                val outfitIds = listOf(
                    "mystic_robe", "storm_cloak", "crystal_armor",
                    "flame_costume", "dragon_scale", "crystal_robe",
                    "light_armor", "astronomer_robe", "time_cloak"
                )

                if (itemId in outfitIds) {
                    updateWizardOutfit(itemId)
                }

                loadInventory(wizard)
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Error equipping item", e)
                uiState.value = InventoryUiState.Error("Failed to equip item")
            }
        }
    }

    private suspend fun updateWizardOutfit(itemId: String) {
        val wizard = currentWizard.value ?: return

        val outfitName = when (itemId) {
            "mystic_robe" -> "Mystic Robe"
            "storm_cloak" -> "Storm Cloak"
            "crystal_armor" -> "Crystal Armor"
            "flame_costume" -> "Flame Costume"
            "dragon_scale" -> "Dragon Scale Armor"
            "crystal_robe" -> "Crystal Robe"
            "light_armor" -> "Light Armor"
            "astronomer_robe" -> "Astronomer Robe"
            "time_cloak" -> "Time Cloak"
            else -> return
        }

        try {
            wizardRepository.updateWizardCustomization(
                userId = wizard.userId,
                gender = wizard.gender,
                skinColor = wizard.skinColor,
                hairStyle = wizard.hairStyle.toInt(),
                hairColor = wizard.hairColor,
                outfit = outfitName
            )
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error updating wizard outfit", e)
            uiState.value = InventoryUiState.Error("Failed to update outfit")
        }
    }

    fun retryLoadWizardProfile() {
        loadWizardProfile()
    }

    // Helper function to check if an item is equipped
    fun isItemEquipped(itemId: String): Boolean {
        val currentState = uiState.value
        return if (currentState is InventoryUiState.Success) {
            currentState.items.any { it.itemId == itemId && it.isEquipped }
        } else false
    }

    // Helper function to get equipped items of a specific type
    fun getEquippedItemOfType(itemType: String): InventoryItemEntity? {
        val currentState = uiState.value
        return if (currentState is InventoryUiState.Success) {
            currentState.items.firstOrNull { it.itemType == itemType && it.isEquipped }
        } else null
    }
}