package com.example.wizardlydo.screens.signupsigin.comps

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeMessage(s: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_animation")


    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_animation"
    )


    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )


    val colorAnim by infiniteTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color(0xFFE6E6FF),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_animation"
    )

    Text(
        text = s,
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        color = colorAnim,
        fontSize = (MaterialTheme.typography.headlineLarge.fontSize.value * scaleAnim).sp,
        modifier = Modifier
            .padding(bottom = 48.dp)
            .offset(y = floatAnim.dp)
    )
}
