package com.wizardlydo.app.viewmodel.customization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wizardlydo.app.data.models.CustomizationState
import com.wizardlydo.app.data.wizard.WizardClass
import com.wizardlydo.app.repository.wizard.WizardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CustomizationViewModel(
    private val repository: WizardRepository,
    wizardClass: WizardClass
) : ViewModel() {

    private val state = MutableStateFlow(
        CustomizationState(
            wizardClass = wizardClass,
            outfit = getDefaultOutfit(wizardClass)
        )
    )
    val stateFlow = state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val userId = repository.getCurrentUserId()
                if (userId != null) {
                    val profile = repository.getWizardProfile(userId).getOrNull()
                    profile?.let {
                        state.update { current ->
                            current.copy(
                                gender = profile.gender,
                                skinColor = profile.skinColor,
                                hairStyle = profile.hairStyle.toInt(),
                                hairColor = profile.hairColor,
                                outfit = profile.outfit
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                state.update { it.copy(error = "Failed to load current customization") }
            }
        }
    }


    fun getDefaultOutfit(wizardClass: WizardClass): String {
        return when (wizardClass) {
            WizardClass.CHRONOMANCER -> "astronomer_robe"
            WizardClass.LUMINARI -> "crystal_robe"
            WizardClass.DRACONIST -> "flame_robe"
            WizardClass.MYSTWEAVER -> "mystic_robe"
        }
    }

    fun updateGender(gender: String) {
        state.update { it.copy(gender = gender) }
    }

    fun updateSkin(skin: String) {
        state.update { it.copy(skinColor = skin) }
    }

    fun updateHairStyle(style: Int) {
        state.update { it.copy(hairStyle = style) }
    }

    fun updateHairColor(color: String) {
        state.update { it.copy(hairColor = color) }
    }

    fun updateOutfit(outfit: String) {
        state.update { it.copy(outfit = outfit) }
    }

    fun saveCustomization() = viewModelScope.launch {
        state.update { it.copy(isLoading = true, error = null) }

        val current = state.value
        try {
            val userId = repository.getCurrentUserId()
                ?: throw Exception("User not authenticated")

            repository.updateWizardCustomization(
                userId = userId,
                gender = current.gender,
                skinColor = current.skinColor,
                hairStyle = current.hairStyle,
                hairColor = current.hairColor,
                outfit = current.outfit,
            ).onSuccess {
                state.update { it.copy(isLoading = false, isSaved = true) }
            }.onFailure { error ->
                state.update { it.copy(isLoading = false, error = error.message) }
            }
        } catch (e: Exception) {
            state.update { it.copy(isLoading = false, error = e.message) }
        }
    }
}
