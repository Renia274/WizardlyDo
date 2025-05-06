package com.example.wizardlydo.screens.customization.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HairColorSelector(selectedColor: String, onHairColorSelected: (String) -> Unit) {
    Column {
        Text("Hair Color", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            ColorChip(
                color = Color(0xFFFBEC5D), // Blond
                selected = selectedColor == "blond",
                onClick = { onHairColorSelected("blond") }
            )
            ColorChip(
                color = Color(0xFF8B4513), // Brown
                selected = selectedColor == "brown",
                onClick = { onHairColorSelected("brown") }
            )
            ColorChip(
                color = Color(0xFFFF4500), // Red
                selected = selectedColor == "red",
                onClick = { onHairColorSelected("red") }
            )
            ColorChip(
                color = Color(0xFFF5F5F5), // White
                selected = selectedColor == "white",
                onClick = { onHairColorSelected("white") }
            )
        }
    }
}