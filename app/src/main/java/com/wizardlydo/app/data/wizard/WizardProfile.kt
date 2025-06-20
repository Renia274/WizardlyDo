package com.wizardlydo.app.data.wizard


import com.wizardlydo.app.providers.SignInProvider
import com.google.firebase.Timestamp

data class WizardProfile(
    val userId: String = "",
    val wizardName: String = "",
    val email: String = "",
    val signInProvider: SignInProvider = SignInProvider.EMAIL,
    val passwordHash: String = "",
    val level: Int = 1,
    val wizardClass: WizardClass = WizardClass.MYSTWEAVER,
    val health: Int = 100,
    val maxHealth: Int = calculateMaxHealth(1),
    val stamina: Int = calculateInitialStamina(),
    val maxStamina: Int = calculateMaxStamina(1),
    val experience: Int = 0,
    val gender: String = "",
    val skinColor: String = "",
    val hairStyle: String = "",
    val hairColor: String = "",
    val outfit: String = "",
    val totalTasksCompleted: Int = 0,
    val consecutiveTasksCompleted: Int = 0,
    val lastTaskCompleted: Timestamp? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val darkModeEnabled: Boolean = false,
    @Transient val isSelected: Boolean = false
) {
    companion object {
        private const val BASE_HP = 100
        private const val HP_PER_LEVEL = 20
        private const val MAX_LEVEL = 30
        private const val BASE_MAX_STAMINA = 100
        private const val STAMINA_INCREMENT_PER_LEVEL = 1
        private const val INITIAL_STAMINA_PERCENTAGE = 0.5

        fun calculateMaxHealth(level: Int): Int {
            val cappedLevel = level.coerceIn(1, MAX_LEVEL)
            return BASE_HP + ((cappedLevel - 1) * HP_PER_LEVEL)
        }

        fun calculateInitialStamina(): Int {
            return (BASE_MAX_STAMINA * INITIAL_STAMINA_PERCENTAGE).toInt()
        }

        fun calculateMaxStamina(level: Int): Int {
            val cappedLevel = level.coerceIn(1, MAX_LEVEL)
            return BASE_MAX_STAMINA + ((cappedLevel - 1) * STAMINA_INCREMENT_PER_LEVEL)
        }
    }
}
