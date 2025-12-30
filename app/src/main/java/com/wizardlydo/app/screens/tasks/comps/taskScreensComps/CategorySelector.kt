package com.wizardlydo.app.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.data.tasks.getTaskCategories

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit
) {
    val taskCategories = remember { getTaskCategories() }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            taskCategories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category.name,
                    onClick = { onCategorySelected(category.name) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = category.iconRes),
                                contentDescription = category.name,
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedCategory == category.name) Color.White else category.color
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = category.color,
                        selectedLabelColor = Color.White,
                        containerColor = category.color.copy(alpha = 0.1f),
                        labelColor = category.color
                    ),
                    border = if (selectedCategory == category.name) {
                        BorderStroke(2.dp, category.color)
                    } else {
                        BorderStroke(1.dp, category.color.copy(alpha = 0.3f))
                    }
                )
            }
        }
    }
}