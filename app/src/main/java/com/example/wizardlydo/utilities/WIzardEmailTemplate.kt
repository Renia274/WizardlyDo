package com.example.wizardlydo.utilities

class WizardEmailTemplates {
    companion object {
        fun getDamageEmailTemplate(damage: Int, currentHealth: Int, maxHealth: Int, tasks: List<String>): String {
            val healthPercentage = (currentHealth * 100) / maxHealth
            val healthColor = when {
                healthPercentage < 20 -> "#FF0000" // Red
                healthPercentage < 50 -> "#FFA500" // Orange
                else -> "#008000" // Green
            }

            return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                    }
                    .header {
                        background-color: #6a0dad;
                        color: white;
                        padding: 15px;
                        text-align: center;
                        border-radius: 5px 5px 0 0;
                    }
                    .content {
                        padding: 20px;
                        background-color: #f9f3ff;
                        border-left: 1px solid #ddd;
                        border-right: 1px solid #ddd;
                    }
                    .health-bar {
                        background-color: #e0e0e0;
                        border-radius: 8px;
                        margin: 15px 0;
                        overflow: hidden;
                    }
                    .health-fill {
                        background-color: ${healthColor};
                        color: white;
                        text-align: center;
                        padding: 5px 0;
                        width: ${healthPercentage}%;
                    }
                    .task-list {
                        background-color: white;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 15px 0;
                    }
                    .task-item {
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .task-item:last-child {
                        border-bottom: none;
                    }
                    .footer {
                        background-color: #f3f3f3;
                        padding: 15px;
                        text-align: center;
                        font-size: 12px;
                        color: #777;
                        border-radius: 0 0 5px 5px;
                        border: 1px solid #ddd;
                        border-top: none;
                    }
                    .wizard-image {
                        text-align: center;
                        margin: 20px 0;
                        font-size: 50px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Your Wizard Has Taken Damage!</h1>
                </div>
                <div class="content">
                    <div class="wizard-image">🧙‍♂️</div>
                    <p>Oh no! Your wizard has taken <strong>${damage} points of damage</strong> due to uncompleted tasks!</p>
                    
                    <h3>Current Health: ${currentHealth}/${maxHealth}</h3>
                    <div class="health-bar">
                        <div class="health-fill">${healthPercentage}%</div>
                    </div>
                    
                    <p>Complete these tasks to prevent further damage:</p>
                    
                    <div class="task-list">
                        ${tasks.joinToString("") { "<div class=\"task-item\">• $it</div>" }}
                    </div>
                    
                    <p>Stay on top of your tasks to keep your wizard healthy and strong!</p>
                </div>
                <div class="footer">
                    <p>This is an automated message from WizardlyDo. Please do not reply to this email.</p>
                    <p>To modify your notification settings, visit the Settings page in the WizardlyDo app.</p>
                </div>
            </body>
            </html>
            """
        }

        fun getCriticalHealthTemplate(damage: Int, currentHealth: Int, maxHealth: Int, tasks: List<String>): String {
            val healthPercentage = (currentHealth * 100) / maxHealth

            return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                    }
                    .header {
                        background-color: #ff0000;
                        color: white;
                        padding: 15px;
                        text-align: center;
                        border-radius: 5px 5px 0 0;
                    }
                    .content {
                        padding: 20px;
                        background-color: #fff0f0;
                        border-left: 1px solid #ddd;
                        border-right: 1px solid #ddd;
                    }
                    .health-bar {
                        background-color: #e0e0e0;
                        border-radius: 8px;
                        margin: 15px 0;
                        overflow: hidden;
                    }
                    .health-fill {
                        background-color: #ff0000;
                        color: white;
                        text-align: center;
                        padding: 5px 0;
                        width: ${healthPercentage}%;
                    }
                    .task-list {
                        background-color: white;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 15px 0;
                    }
                    .task-item {
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .task-item:last-child {
                        border-bottom: none;
                    }
                    .footer {
                        background-color: #f3f3f3;
                        padding: 15px;
                        text-align: center;
                        font-size: 12px;
                        color: #777;
                        border-radius: 0 0 5px 5px;
                        border: 1px solid #ddd;
                        border-top: none;
                    }
                    .wizard-image {
                        text-align: center;
                        margin: 20px 0;
                        font-size: 50px;
                    }
                    .urgent {
                        font-size: 1.2em;
                        color: #ff0000;
                        font-weight: bold;
                        text-align: center;
                        border: 2px dashed #ff0000;
                        padding: 10px;
                        margin: 15px 0;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>⚠️ CRITICAL HEALTH WARNING ⚠️</h1>
                </div>
                <div class="content">
                    <div class="wizard-image">🧙‍♂️</div>
                    
                    <div class="urgent">
                        YOUR WIZARD IS IN CRITICAL DANGER!
                    </div>
                    
                    <p>Your wizard has taken <strong>${damage} points of damage</strong> and is now at critical health levels!</p>
                    
                    <h3>Current Health: ${currentHealth}/${maxHealth}</h3>
                    <div class="health-bar">
                        <div class="health-fill">${healthPercentage}%</div>
                    </div>
                    
                    <p><strong>Complete these tasks IMMEDIATELY to prevent your wizard from dying:</strong></p>
                    
                    <div class="task-list">
                        ${tasks.joinToString("") { "<div class=\"task-item\">• $it</div>" }}
                    </div>
                    
                    <p>If your wizard's health reaches zero, you'll need to revive them and may lose progress!</p>
                </div>
                <div class="footer">
                    <p>This is an automated message from WizardlyDo. Please do not reply to this email.</p>
                    <p>To modify your notification settings, visit the Settings page in the WizardlyDo app.</p>
                </div>
            </body>
            </html>
            """
        }

        fun getPerishedWizardTemplate(tasks: List<String>): String {
            return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #f7f7f7;
                    }
                    .header {
                        background-color: #000000;
                        color: white;
                        padding: 15px;
                        text-align: center;
                        border-radius: 5px 5px 0 0;
                    }
                    .content {
                        padding: 20px;
                        background-color: #fff;
                        border-left: 1px solid #ddd;
                        border-right: 1px solid #ddd;
                        text-align: center;
                    }
                    .task-list {
                        background-color: #f8f8f8;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 15px 0;
                        text-align: left;
                    }
                    .task-item {
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .task-item:last-child {
                        border-bottom: none;
                    }
                    .footer {
                        background-color: #f3f3f3;
                        padding: 15px;
                        text-align: center;
                        font-size: 12px;
                        color: #777;
                        border-radius: 0 0 5px 5px;
                        border: 1px solid #ddd;
                        border-top: none;
                    }
                    .wizard-image {
                        text-align: center;
                        margin: 20px 0;
                        font-size: 80px;
                    }
                    .revive-button {
                        display: inline-block;
                        background-color: #f44336;
                        color: white;
                        padding: 14px 20px;
                        margin: 20px 0;
                        border: none;
                        border-radius: 4px;
                        font-size: 16px;
                        font-weight: bold;
                        text-decoration: none;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>💀 YOUR WIZARD HAS PERISHED! 💀</h1>
                </div>
                <div class="content">
                    <div class="wizard-image">⚰️</div>
                    
                    <h2>Your Wizard Has Run Out of Health!</h2>
                    <p>Your wizard's health has reached zero due to too many overdue tasks. Your wizard needs to be revived to continue your journey.</p>
                    
                    <a href="#" class="revive-button">REVIVE YOUR WIZARD</a>
                    
                    <p>These incomplete tasks were the cause:</p>
                    
                    <div class="task-list">
                        ${tasks.joinToString("") { "<div class=\"task-item\">• $it</div>" }}
                    </div>
                    
                    <p>Complete your tasks regularly to keep your wizard healthy and earn experience points!</p>
                </div>
                <div class="footer">
                    <p>This is an automated message from WizardlyDo. Please do not reply to this email.</p>
                    <p>To modify your notification settings, visit the Settings page in the WizardlyDo app.</p>
                </div>
            </body>
            </html>
            """
        }

        fun getTaskReminderTemplate(taskNames: List<String>, daysUntilDue: Int): String {
            val timeText = if (daysUntilDue == 1) "tomorrow" else "in $daysUntilDue days"

            return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                    }
                    .header {
                        background-color: #6a0dad;
                        color: white;
                        padding: 15px;
                        text-align: center;
                        border-radius: 5px 5px 0 0;
                    }
                    .content {
                        padding: 20px;
                        background-color: #f9f3ff;
                        border-left: 1px solid #ddd;
                        border-right: 1px solid #ddd;
                    }
                    .task-list {
                        background-color: white;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 15px 0;
                    }
                    .task-item {
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .task-item:last-child {
                        border-bottom: none;
                    }
                    .footer {
                        background-color: #f3f3f3;
                        padding: 15px;
                        text-align: center;
                        font-size: 12px;
                        color: #777;
                        border-radius: 0 0 5px 5px;
                        border: 1px solid #ddd;
                        border-top: none;
                    }
                    .wizard-image {
                        text-align: center;
                        margin: 20px 0;
                        font-size: 50px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>WizardlyDo Task Reminder</h1>
                </div>
                <div class="content">
                    <div class="wizard-image">🧙‍♂️</div>
                    <p>Greetings, Wizard!</p>
                    <p>Your magical tasks require attention! The following tasks are due <strong>${timeText}</strong>:</p>
                    
                    <div class="task-list">
                        ${taskNames.joinToString("") { "<div class=\"task-item\">• $it</div>" }}
                    </div>
                    
                    <p>Complete these tasks to prevent your wizard from taking damage and to gain precious experience points!</p>
                    <p>May your spellcasting be swift and accurate.</p>
                </div>
                <div class="footer">
                    <p>This is an automated message from WizardlyDo. Please do not reply to this email.</p>
                    <p>To modify your notification settings, visit the Settings page in the WizardlyDo app.</p>
                </div>
            </body>
            </html>
            """
        }

        // Overload for single task reminder - this matches the signature in existing code
        fun getTaskReminderTemplate(taskTitle: String, daysUntilDue: Int, description: String): String {
            return getTaskReminderTemplate(listOf(taskTitle), daysUntilDue)
        }
    }
}