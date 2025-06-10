package com.wizardlydo.app.screens.tasks.comps.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.room.inventory.InventoryItemEntity

@Composable
fun ItemDetailsDialog(
    item: InventoryItemEntity,
    onDismiss: () -> Unit,
    onEquip: () -> Unit,
    isEquipped: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column {
                Image(
                    painter = painterResource(id = item.resourceId),
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.description)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onEquip()
                    onDismiss()
                },
                enabled = !isEquipped
            ) {
                Text(if (isEquipped) "Equipped" else "Equip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
