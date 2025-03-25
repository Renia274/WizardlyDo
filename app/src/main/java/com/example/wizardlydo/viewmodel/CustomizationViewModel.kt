package com.example.wizardlydo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wizardlydo.WizardClass
import com.example.wizardlydo.data.WizardProfile
import com.example.wizardlydo.repository.WizardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class CustomizationViewModel(
    private val repository: WizardRepository,
    private val wizardClass: WizardClass
) : ViewModel() , KoinComponent {
    private val _state = MutableStateFlow(CustomizationState())
    val state = _state.asStateFlow()





    fun updateGender(gender: String) {
        _state.update { it.copy(gender = gender) }
    }

    fun updateColors(body: String, clothing: String, accessory: String) {
        _state.update {
            it.copy(
                bodyColor = body,
                clothingColor = clothing,
                accessoryColor = accessory
            )
        }
    }

    fun saveCustomization() = viewModelScope.launch {
        val current = _state.value
        try {
            val userId = repository.getCurrentUserId()
                ?: throw Exception("User not authenticated")

            val updated = WizardProfile(
                userId = userId,
                wizardClass = current.wizardClass,
                gender = current.gender,
                bodyColor = current.bodyColor,
                clothingColor = current.clothingColor,
                accessoryColor = current.accessoryColor
            )

            repository.updateWizard(updated) // Assume this throws on failure
            _state.update { it.copy(isSaved = true) }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message) }
        }
    }


}

data class CustomizationState(
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val gender: String = "Male",
    val bodyColor: String = "#FFD700",
    val clothingColor: String = "#2E0854",
    val accessoryColor: String = "#000000",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)