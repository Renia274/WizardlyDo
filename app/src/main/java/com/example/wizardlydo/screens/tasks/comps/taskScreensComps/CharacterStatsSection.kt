package com.example.wizardlydo.screens.tasks.comps.taskScreensComps

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.data.wizard.items.EquippedItems
import com.example.wizardlydo.data.wizard.WizardProfile
import com.example.wizardlydo.screens.tasks.comps.WizardAvatar
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.stats.CompactLevelProgressSection
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.stats.StatBar
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.stats.TaskProgressSection

@Composable
fun CharacterStatsSection(
    wizardResult: Result<WizardProfile?>?,
    modifier: Modifier = Modifier,
    health: Int,
    maxHealth: Int,
    stamina: Int,
    maxStamina: Int,
    experience: Int,
    tasksCompleted: Int,
    totalTasksForLevel: Int,
    equippedItems: EquippedItems? = null
) {
    val wizardProfile = wizardResult?.getOrNull()

    val animatedHealth by animateIntAsState(
        targetValue = health,
        animationSpec = tween(durationMillis = 500),
        label = "health"
    )

    val animatedStamina by animateIntAsState(
        targetValue = stamina,
        animationSpec = tween(durationMillis = 500),
        label = "stamina"
    )

    val animatedExp by animateIntAsState(
        targetValue = experience,
        animationSpec = tween(durationMillis = 500),
        label = "experience"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            equippedItems?.background?.let { bg ->
                Image(
                    painter = painterResource(id = bg.resourceId),
                    contentDescription = "Background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WizardAvatar(
                        wizardResult = wizardResult,
                        equippedItems = equippedItems,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    wizardProfile?.let { wizard ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wizard.wizardName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = wizard.wizardClass.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Level ${wizard.level}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                StatBar(
                    label = "HP",
                    value = animatedHealth,
                    maxValue = maxHealth,
                    color = Color(0xFFE53935),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                StatBar(
                    label = "Stamina",
                    value = animatedStamina,
                    maxValue = maxStamina,
                    color = Color(0xFF43A047),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                CompactLevelProgressSection(
                    level = wizardProfile?.level ?: 1,
                    experience = animatedExp,
                    tasksCompleted = tasksCompleted,
                    totalTasksForLevel = totalTasksForLevel
                )

                Spacer(modifier = Modifier.height(8.dp))

                TaskProgressSection(
                    tasksCompleted = tasksCompleted,
                    totalTasksForLevel = totalTasksForLevel,
                    maxHealth = maxHealth
                )
            }
        }
    }
}