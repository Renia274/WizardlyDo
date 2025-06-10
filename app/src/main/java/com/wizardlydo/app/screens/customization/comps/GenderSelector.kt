package com.wizardlydo.app.screens.customization.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun GenderSelector(selectedGender: String, onGenderSelected: (String) -> Unit) {
    Column {
        Text("Gender", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            SelectionChip(
                text = "Male",
                selected = selectedGender == "Male",
                onClick = { onGenderSelected("Male") }
            )
            SelectionChip(
                text = "Female",
                selected = selectedGender == "Female",
                onClick = { onGenderSelected("Female") }
            )
        }
    }
}
