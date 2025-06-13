package com.wizardlydo.app.screens.customization.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SaveTickButton(
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            isClicked = true
            showConfirmDialog = true
        },
        modifier = modifier
            .offset(x = (-6).dp)
            .background(
                color = if (isClicked) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = if (isClicked) 0.dp else 2.dp,
                color = if (isClicked) Color.Transparent else MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .size(48.dp)
    ) {
        if (isClicked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Save Customization",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                isClicked = false
            },
            title = {
                Text(
                    text = "Save Customization",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you satisfied with your character customization? This will save your changes.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onSave()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        isClicked = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}