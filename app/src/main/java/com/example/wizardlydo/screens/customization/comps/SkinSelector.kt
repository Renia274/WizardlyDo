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
fun SkinSelector(selectedSkin: String, onSkinSelected: (String) -> Unit) {
    Column {
        Text("Skin Color", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Standard skin tones
            ColorChip(
                color = Color(0xFFF5A76E), // Light skin
                selected = selectedSkin == "light",
                onClick = { onSkinSelected("light") }
            )
            ColorChip(
                color = Color(0xFFEA8349), // Medium skin
                selected = selectedSkin == "medium",
                onClick = { onSkinSelected("medium") }
            )
            ColorChip(
                color = Color(0xFF98461A), // Dark skin
                selected = selectedSkin == "dark",
                onClick = { onSkinSelected("dark") }
            )

            // Fantasy skin tones
            ColorChip(
                color = Color(0xFF0FF591),
                selected = selectedSkin == "fantasy1",
                onClick = { onSkinSelected("fantasy1") }
            )
            ColorChip(
                color = Color(0xFF800ED0),
                selected = selectedSkin == "fantasy2",
                onClick = { onSkinSelected("fantasy2") }
            )
        }
    }
}