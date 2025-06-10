package com.wizardlydo.app.screens.signup.comps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.data.wizard.WizardClass

@Composable
fun WizardClassSelector(
    selectedClass: WizardClass,
    onClassSelected: (WizardClass) -> Unit,
    enabled: Boolean
) {
    Column {
        Text("Choose your class:", style = MaterialTheme.typography.bodyMedium)
        WizardClass.entries.forEach { wizardClass ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onClassSelected(wizardClass) }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                RadioButton(
                    selected = wizardClass == selectedClass,
                    onClick = { onClassSelected(wizardClass) },
                    enabled = enabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(wizardClass.title, style = MaterialTheme.typography.bodyLarge)
                    Text(wizardClass.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
