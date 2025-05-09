package com.example.wizardlydo.screens.tasks.comps.inventory

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
import com.example.wizardlydo.data.wizard.WizardProfile
import com.example.wizardlydo.data.wizard.items.EquippedItems
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.stats.RevivalProgressSection
import com.example.wizardlydo.wizardCustomization.WizardAvatar
import com.example.wizardlydo.screens.tasks.comps.taskScreensComps.stats.StatBar
import androidx.compose.ui.text.style.TextAlign
import com.example.wizardlydo.viewmodel.tasks.TaskViewModel

@Composable
fun BasicCharacterStatsSection(
    wizardProfile: WizardProfile,
    modifier: Modifier = Modifier,
    equippedItems: EquippedItems? = null,
    taskViewModel: TaskViewModel? = LocalTaskViewModel.current
) {
    val isWizardDead = wizardProfile.health <= 0
    val hasBackground = equippedItems?.background != null

    val primaryColor = if (hasBackground) Color.White else MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = if (hasBackground) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariantColor = if (hasBackground) {
        Color.Black.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val animatedHealth by animateIntAsState(
        targetValue = wizardProfile.health,
        animationSpec = tween(durationMillis = 500),
        label = "health"
    )

    val animatedStamina by animateIntAsState(
        targetValue = wizardProfile.stamina,
        animationSpec = tween(durationMillis = 500),
        label = "stamina"
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
                        wizardResult = Result.success(wizardProfile),
                        equippedItems = equippedItems,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wizardProfile.wizardName,
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
                                text = wizardProfile.wizardClass.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Level ${wizardProfile.level}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isWizardDead)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else
                                primaryColor,
                            fontWeight = FontWeight.Bold
                        )
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
                        maxValue = wizardProfile.maxHealth,
                        color = when {
                            isWizardDead -> Color.Gray
                            hasBackground -> Color.Red
                            else -> Color(0xFFE53935)
                        },
                        textColor = if (hasBackground) Color.White else Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    StatBar(
                        label = "Stamina",
                        value = animatedStamina,
                        maxValue = wizardProfile.maxStamina,
                        color = when {
                            isWizardDead -> Color.Gray
                            hasBackground -> Color.Green
                            else -> Color(0xFF43A047)
                        },
                        textColor = if (hasBackground) Color.White else Color.Black
                    )
                }

                // Show revival progress if the wizard is dead
                if (isWizardDead) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val revivalProgress = if (taskViewModel != null) {
                        taskViewModel.getRevivalProgress()
                    } else {
                        // Fallback if viewModel isn't available
                        Pair(wizardProfile.consecutiveTasksCompleted, 3)
                    }

                    RevivalProgressSection(
                        tasksCompleted = revivalProgress.first,
                        tasksNeededForRevival = revivalProgress.second,
                        textColor = if (hasBackground) Color.White else Color.Black
                    )
                }
            }
        }
    }
}