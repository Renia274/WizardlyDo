package com.example.wizardlydo.screens.customization.comps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.wizardlydo.WizardClass
import com.example.wizardlydo.viewmodel.CustomizationState


@Composable
 fun WizardPreview(state: CustomizationState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bodyRadius = size.minDimension / 4
            val centerX = size.width / 2
            val bodyY = size.height / 2 + if (state.gender == "Female") 20f else 0f

            // Body
            drawCircle(
                color = Color(android.graphics.Color.parseColor(state.bodyColor)),
                center = Offset(centerX, bodyY),
                radius = bodyRadius
            )

            // Clothing
            drawPath(
                path = Path().apply {
                    moveTo(centerX - 60f, bodyY)
                    lineTo(centerX - 60f, bodyY + 120f)
                    lineTo(centerX + 60f, bodyY + 120f)
                    lineTo(centerX + 60f, bodyY)
                    close()
                },
                color = Color(android.graphics.Color.parseColor(state.clothingColor))
            )

          
            when(state.wizardClass) {
                WizardClass.CHRONOMANCER -> drawChronomancerElements(centerX, bodyY, state)
                WizardClass.LUMINARI -> drawLuminariElements(centerX, bodyY, state)
                WizardClass.DRACONIST -> drawDraconistElements(centerX, bodyY, state)
                WizardClass.MYSTWEAVER -> drawMystweaverElements(centerX, bodyY, state)
            }
        }
    }
}

// Drawing functions for each class
 fun DrawScope.drawChronomancerElements(centerX: Float, bodyY: Float, state: CustomizationState) {
    // Clock face
    drawCircle(
        color = Color(android.graphics.Color.parseColor(state.accessoryColor)),
        center = Offset(centerX, bodyY - 80f),
        radius = 30f
    )
    // Clock hands
    drawLine(
        color = Color.White,
        start = Offset(centerX, bodyY - 80f),
        end = Offset(centerX + 20f, bodyY - 100f),
        strokeWidth = 4f
    )
}

 fun DrawScope.drawLuminariElements(centerX: Float, bodyY: Float, state: CustomizationState) {
    // Halo
    drawCircle(
        color = Color.Yellow.copy(alpha = 0.4f),
        center = Offset(centerX, bodyY - 100f),
        radius = 40f,
        blendMode = BlendMode.Plus
    )
}

 fun DrawScope.drawDraconistElements(centerX: Float, bodyY: Float, state: CustomizationState) {
    // Dragon scales
    repeat(8) { i ->
        drawCircle(
            color = Color(android.graphics.Color.parseColor(state.accessoryColor)),
            center = Offset(centerX - 40f + i * 20f, bodyY + 60f),
            radius = 8f
        )
    }
}

 fun DrawScope.drawMystweaverElements(centerX: Float, bodyY: Float, state: CustomizationState) {
    // Mystic pattern
    drawPath(
        path = Path().apply {
            moveTo(centerX - 40f, bodyY - 40f)
            quadraticTo(centerX, bodyY - 60f, centerX + 40f, bodyY - 40f)
        },
        color = Color(android.graphics.Color.parseColor(state.accessoryColor)),
        style = Stroke(width = 4f)
    )
}

@Composable
fun GenderSelector(selectedGender: String, onGenderSelected: (String) -> Unit) {
    Column {
        Text("Gender", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenderChip("Male", selectedGender == "Male") { onGenderSelected("Male") }
            GenderChip("Female", selectedGender == "Female") { onGenderSelected("Female") }
        }
    }
}

@Composable
fun GenderChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ColorPickers(
    state: CustomizationState,
    onColorsChanged: (body: String, clothing: String, accessory: String) -> Unit
) {
    Column {
        ColorPicker(
            label = "Body Color",
            color = state.bodyColor,
            onColorChange = { onColorsChanged(it, state.clothingColor, state.accessoryColor) }
        )
        ColorPicker(
            label = "Clothing Color",
            color = state.clothingColor,
            onColorChange = { onColorsChanged(state.bodyColor, it, state.accessoryColor) }
        )
        ColorPicker(
            label = "Accessory Color",
            color = state.accessoryColor,
            onColorChange = { onColorsChanged(state.bodyColor, state.clothingColor, it) }
        )
    }
}

@Composable
 fun ColorPicker(label: String, color: String, onColorChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    var rgbValues by remember { mutableStateOf(IntArray(3)) }

    // Parse initial color
    LaunchedEffect(color) {
        try {
            val c = android.graphics.Color.parseColor(color)
            rgbValues = intArrayOf(
                android.graphics.Color.red(c),
                android.graphics.Color.green(c),
                android.graphics.Color.blue(c)
            )
        } catch (e: Exception) {
            rgbValues = intArrayOf(0, 0, 0)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(android.graphics.Color.parseColor(color)))
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button({
                    val hex = String.format("#%02X%02X%02X", rgbValues[0], rgbValues[1], rgbValues[2])
                    onColorChange(hex)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Button({ showPicker = false }) { Text("Cancel") }
            },
            text = {
                Column {
                    Text("Choose Color", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color(rgbValues[0], rgbValues[1], rgbValues[2]))
                    )

                    // RGB sliders
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Red: ${rgbValues[0]}")
                        Slider(
                            value = rgbValues[0].toFloat(),
                            onValueChange = { rgbValues = rgbValues.copyOf().apply { set(0, it.toInt()) } },
                            valueRange = 0f..255f
                        )

                        Text("Green: ${rgbValues[1]}")
                        Slider(
                            value = rgbValues[1].toFloat(),
                            onValueChange = { rgbValues = rgbValues.copyOf().apply { set(1, it.toInt()) } },
                            valueRange = 0f..255f
                        )

                        Text("Blue: ${rgbValues[2]}")
                        Slider(
                            value = rgbValues[2].toFloat(),
                            onValueChange = { rgbValues = rgbValues.copyOf().apply { set(2, it.toInt()) } },
                            valueRange = 0f..255f
                        )
                    }
                }
            }
        )
    }
}




