package com.example.wizardlydo.utilities

class WizardEmailTemplates {
    companion object {
        fun getDamageEmailTemplate(damage: Int, currentHealth: Int, maxHealth: Int, tasks: List<String>): String {
            val healthPercentage = (currentHealth * 100) / maxHealth
            val healthStatus = when {
                healthPercentage < 20 -> "CRITICAL"
                healthPercentage < 50 -> "LOW"
                else -> "STABLE"
            }

            return """
            ✨ WIZARDLYDO DAMAGE ALERT ✨
            
            🧙‍♂️ Your wizard has taken $damage points of damage!
            
            HEALTH STATUS: $healthStatus
            Current Health: $currentHealth/$maxHealth (${healthPercentage}%)
            
            ⚠️ Overdue Tasks:
            ${tasks.joinToString("\n") { "• $it" }}
            
            Complete these tasks to prevent further damage to your wizard!
            
            Stay magical,
            The WizardlyDo Team
            
            ---
            To change notification settings, visit the app settings.
            """.trimIndent()
        }

        fun getCriticalHealthTemplate(damage: Int, currentHealth: Int, maxHealth: Int, tasks: List<String>): String {
            return """
            ‼️ WIZARDLYDO CRITICAL ALERT ‼️
            
            🚨 YOUR WIZARD IS IN DANGER! 🚨
            
            Your wizard has taken $damage damage and is now at CRITICAL health levels!
            Current Health: $currentHealth/$maxHealth
            
            ⚠️ URGENT TASKS:
            ${tasks.joinToString("\n") { "• $it" }}
            
            Complete these tasks IMMEDIATELY to save your wizard!
            
            With urgent concern,
            The WizardlyDo Team
            
            ---
            To change notification settings, visit the app settings.
            """.trimIndent()
        }

        fun getPerishedWizardTemplate(tasks: List<String>): String {
            return """
            💀 WIZARDLYDO URGENT NOTICE 💀
            
            YOUR WIZARD HAS PERISHED!
            
            Due to uncompleted tasks, your wizard's health reached zero.
            
            Failed Tasks:
            ${tasks.joinToString("\n") { "• $it" }}
            
            Revive your wizard in the app to continue your journey!
            
            In sorrow,
            The WizardlyDo Team
            
            ---
            To change notification settings, visit the app settings.
            """.trimIndent()
        }

        fun getTaskReminderTemplate(taskTitle: String, daysUntilDue: Int, description: String): String {
            val timeText = when (daysUntilDue) {
                0 -> "today"
                1 -> "tomorrow"
                else -> "in $daysUntilDue days"
            }

            return """
            ✉️ WIZARDLYDO TASK REMINDER ✉️
            
            Friendly reminder about your task:
            
            📌 $taskTitle
            ${description.takeIf { it.isNotBlank() }?.let { "\nℹ️ $it" } ?: ""}
            
            Due: $timeText
            
            Complete this task to keep your wizard healthy and earn rewards!
            
            Happy spellcasting,
            The WizardlyDo Team
            
            ---
            To change notification settings, visit the app settings.
            """.trimIndent()
        }
    }
}