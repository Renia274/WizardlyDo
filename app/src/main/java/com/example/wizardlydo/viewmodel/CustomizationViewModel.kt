package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.data.WizardClass
import com.example.wizardlydo.data.models.CustomizationState
import com.example.wizardlydo.repository.wizard.WizardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class CustomizationViewModel(
    private val repository: WizardRepository,
    wizardClass: WizardClass
) : ViewModel(), KoinComponent {
    private val _state = MutableStateFlow(
        CustomizationState(
            wizardClass = wizardClass,
            outfit = getDefaultOutfit(wizardClass)
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Try to load current customization if user is already logged in
            try {
                val userId = repository.getCurrentUserId()
                if (userId != null) {
                    val profile = repository.getWizardProfile(userId).getOrNull()
                    profile?.let {
                        _state.update { state ->
                            state.copy(
                                gender = profile.gender,
                                skinColor = profile.skinColor,
                                hairStyle = profile.hairStyle,
                                hairColor = profile.hairColor,
                                outfit = profile.outfit
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Fall back to defaults if loading fails
                _state.update { it.copy(error = "Failed to load current customization") }
            }
        }
    }

    private fun getDefaultAccessory(wizardClass: WizardClass): String {
        return when (wizardClass) {
            WizardClass.CHRONOMANCER -> "Time Glasses"
            WizardClass.LUMINARI -> "Light Mask"
            WizardClass.DRACONIST -> "Dragon Eyes"
            WizardClass.MYSTWEAVER -> "Arcane Monocle"
        }
    }

    private fun getDefaultOutfit(wizardClass: WizardClass): String {
        return when (wizardClass) {
            WizardClass.CHRONOMANCER -> "Astronomer Robe"
            WizardClass.LUMINARI -> "Crystal Robe"
            WizardClass.DRACONIST -> "Flame Costume"
            WizardClass.MYSTWEAVER -> "Mystic Robe"
        }
    }

    fun updateGender(gender: String) {
        _state.update { it.copy(gender = gender) }
    }

    fun updateSkin(skin: String) {
        _state.update { it.copy(skinColor = skin) }
    }

    fun updateHairStyle(style: Int) {
        _state.update { it.copy(hairStyle = style) }
    }

    fun updateHairColor(color: String) {
        _state.update { it.copy(hairColor = color) }
    }

    fun updateOutfit(outfit: String) {
        _state.update { it.copy(outfit = outfit) }
    }

    fun saveCustomization() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        val current = _state.value
        try {
            val userId = repository.getCurrentUserId()
                ?: throw Exception("User not authenticated")

            // Using the specific customization update method
            repository.updateWizardCustomization(
                userId = userId,
                gender = current.gender,
                skinColor = current.skinColor,
                hairStyle = current.hairStyle,
                hairColor = current.hairColor,
                outfit = current.outfit,
                accessory = getDefaultAccessory(current.wizardClass)
            ).onSuccess {
                _state.update { it.copy(isLoading = false, isSaved = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }
}