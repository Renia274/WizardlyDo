package com.example.wizardlydo.comps

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp




class RhombusShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(
        Path().apply {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Rhombus points (diamond shape)
            moveTo(centerX, centerY - size.height / 2)  // Top point
            lineTo(centerX + size.width / 2, centerY)    // Right point
            lineTo(centerX, centerY + size.height / 2)   // Bottom point
            lineTo(centerX - size.width / 2, centerY)    // Left point
            close()
        }
    )
}

@Composable
fun RhombusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Blue,
    size: Dp = 64.dp,
    sparkleColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current

    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        )
    )

    Surface(
        shape = RhombusShape(),
        color = color,
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .rotate(rotation)
                .drawRhombus(density, size, color)
        ) {
            Sparkles(
                sparkleColor = sparkleColor,
                size = size
            )
        }
    }
}

private fun Modifier.drawRhombus(
    density: Density,
    size: Dp,
    color: Color
) = this.drawWithCache {
    val path = RhombusShape().createOutline(
        Size(size.toPx(), size.toPx()),
        LayoutDirection.Ltr,
        density
    ).path

    onDrawBehind {
        drawPath(path, color)
    }
}

@Composable
private fun Sparkles(
    sparkleColor: Color,
    size: Dp
) {
    val sparklePositions = listOf(
        Offset(0.5f, 0.2f),  // Top
        Offset(0.8f, 0.5f),  // Right
        Offset(0.5f, 0.8f),  // Bottom
        Offset(0.2f, 0.5f)   // Left
    )

    val infiniteTransition = rememberInfiniteTransition()
    val sizePx = with(LocalDensity.current) { size.toPx() }

    // Create animations in composable scope
    val alphas = List(sparklePositions.size) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2000
                    0f at 0
                    1f at 500
                    0f at 1000
                },
                initialStartOffset = StartOffset(index * 300)
            )
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        sparklePositions.forEachIndexed { index, position ->
            drawCircle(
                color = sparkleColor.copy(alpha = alphas[index].value),
                radius = sizePx * 0.08f,
                center = Offset(
                    x = sizePx * position.x,
                    y = sizePx * position.y
                )
            )
        }
    }
}

@Preview
@Composable
fun BlueRhombusPreview() {
    RhombusButton(
        onClick = {},
        color = Color(0xFF2196F3),
        size = 120.dp
    )
}

@Preview
@Composable
fun PurpleRhombusPreview() {
    RhombusButton(
        onClick = {},
        color = Color(0xFF9C27B0),
        sparkleColor = Color(0xFFFF80AB),
        size = 150.dp
    )
}