package com.wizardlydo.app.comps.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = calculatePasswordStrength(password)
    val color = when {
        strength > 80 -> MaterialTheme.colorScheme.primary
        strength > 60 -> Color(0xFF4CAF50) // Green
        strength > 30 -> Color(0xFFFFC107) // Yellow
        else -> MaterialTheme.colorScheme.error
    }

    val strengthText = when {
        strength > 80 -> "Strong"
        strength > 60 -> "Good"
        strength > 30 -> "Fair"
        else -> "Weak"
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password Strength:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = strengthText,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { strength / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

private fun calculatePasswordStrength(password: String): Int {
    if (password.isBlank()) return 0

    var score = 0

    if (password.length >= 12) score += 25
    else if (password.length >= 8) score += 15
    else score += 5


    if (password.any { it.isDigit() }) score += 15
    if (password.any { it.isLowerCase() }) score += 15
    if (password.any { it.isUpperCase() }) score += 15
    if (password.any { !it.isLetterOrDigit() }) score += 15


    val hasRepeatedChars = password.zipWithNext().any { it.first == it.second }
    if (hasRepeatedChars) score -= 10

    val commonPatterns = listOf("123", "abc", "qwerty", "password", "admin")
    if (commonPatterns.any { password.lowercase().contains(it) }) score -= 15

    return score.coerceIn(0, 100)
}
