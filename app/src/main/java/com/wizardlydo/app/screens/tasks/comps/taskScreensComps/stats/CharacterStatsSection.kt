package com.wizardlydo.app.screens.tasks.comps.taskScreensComps.stats

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.data.wizard.WizardProfile
import com.wizardlydo.app.data.wizard.items.EquippedItems
import com.wizardlydo.app.viewmodel.tasks.TaskViewModel
import com.wizardlydo.app.wizardCustomization.WizardAvatar

val LocalTaskViewModel = staticCompositionLocalOf<TaskViewModel?> { null }

@Composable
fun CharacterStatsSection(
    wizardResult: Result<WizardProfile?>?,
    modifier: Modifier = Modifier,
    health: Int,
    maxHealth: Int,
    stamina: Int,
    maxStamina: Int,
    experience: Int,
    level: Int,
    equippedItems: EquippedItems? = null,
    taskViewModel: TaskViewModel? = LocalTaskViewModel.current
) {
    val wizardProfile = wizardResult?.getOrNull()
    val isWizardDead = health <= 0
    val hasBackground = equippedItems?.background != null
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val primaryColor = if (hasBackground || isSystemInDarkTheme) {
        Color.White
    } else {
        MaterialTheme.colorScheme.primary
    }

    val surfaceVariantColor = if (hasBackground) {
        Color.Black.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val onSurfaceVariantColor = if (hasBackground || isSystemInDarkTheme) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

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
            containerColor = if (isWizardDead)
                surfaceVariantColor.copy(alpha = 0.7f)
            else
                surfaceVariantColor
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
                    alpha = if (isWizardDead) 0.3f else 0.6f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                surfaceVariantColor.copy(alpha = 0.7f)
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
                                color = if (isWizardDead)
                                    MaterialTheme.colorScheme.error
                                else
                                    primaryColor
                            )

                            if (isWizardDead) {
                                Text(
                                    text = "DEFEATED",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = wizard.wizardClass.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurfaceVariantColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Level ${wizard.level}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isWizardDead)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                else
                                    primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Death message when HP is zero
                if (isWizardDead) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your wizard has been defeated!",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Complete tasks to restore health and continue your journey.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    StatBar(
                        label = "HP",
                        value = animatedHealth,
                        maxValue = maxHealth,
                        color = when {
                            isWizardDead -> Color.Gray
                            hasBackground || isSystemInDarkTheme -> Color.Red
                            else -> Color(0xFFE53935)
                        })
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    StatBar(
                        label = "Stamina",
                        value = animatedStamina,
                        maxValue = maxStamina,
                        color = when {
                            isWizardDead -> Color.Gray
                            hasBackground || isSystemInDarkTheme -> Color.Green
                            else -> Color(0xFF43A047)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                CompactLevelProgressSection(
                    level = level,
                    experience = animatedExp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!isWizardDead) {
                    XPBasedTaskProgressSection(
                        level = level,
                        experience = animatedExp,
                        taskViewModel = taskViewModel,
                        wizardProfile = wizardProfile
                    )
                } else {
                    val revivalProgress = taskViewModel?.getRevivalProgress() ?: Pair(wizardProfile?.consecutiveTasksCompleted ?: 0, 3)

                    RevivalProgressSection(
                        tasksCompleted = revivalProgress.first,
                        tasksNeededForRevival = revivalProgress.second
                    )
                }
            }
        }
    }
}

@Composable
fun XPBasedTaskProgressSection(
    level: Int,
    experience: Int,
    taskViewModel: TaskViewModel? = LocalTaskViewModel.current,
    wizardProfile: WizardProfile? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    // Calculate tasks required for current level
    val tasksRequiredForLevel = when (level) {
        in 1..4 -> 10
        in 5..8 -> 15
        in 9..14 -> 20
        in 15..19 -> 25
        in 20..24 -> 30
        in 25..29 -> 35
        else -> 40
    }

    // Calculate XP per task for current level
    val expPerLevel = 1000
    val expPerTask = if (tasksRequiredForLevel > 0) expPerLevel / tasksRequiredForLevel else 0

    // Calculate tasks completed based on current XP
    val tasksCompleted = if (expPerTask > 0) {
        (experience / expPerTask).coerceAtMost(tasksRequiredForLevel)
    } else {
        0
    }

    // Calculate progress
    val taskProgress = if (tasksRequiredForLevel > 0) {
        tasksCompleted.toFloat() / tasksRequiredForLevel.toFloat()
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Show toast when card is clicked
                wizardProfile?.let { profile ->
                    taskViewModel?.showXPProgressToast(profile)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task Set Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Info icon to indicate clickable
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tap for XP details",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { taskProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = textColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$tasksCompleted/$tasksRequiredForLevel completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )

                Text(
                    text = "${experience}/${expPerLevel} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap for XP details • Complete all tasks to level up",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}