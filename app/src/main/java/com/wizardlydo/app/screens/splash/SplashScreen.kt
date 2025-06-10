package com.wizardlydo.app.screens.splash

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wizardlydo.app.ui.theme.WizardlyDoTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navigateToWelcomeAuth: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        navigateToWelcomeAuth()
    }

    SplashContent()
}

@Composable
fun SplashContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val robeSwayAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val staffRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )



    val textColor by infiniteTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color(0xFFA080FF),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_color_animation"
    )

    val textFloatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 200), // Slightly delayed from character
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_float_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C1810)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = floatAnim.dp)
        ) {
            Canvas(modifier = Modifier.size(250.dp)) {
                val center = Offset(size.width / 2, size.height / 2)

                // Staff
                rotate(staffRotation, center) {
                    drawLine(
                        color = Color(0xFF8B4513),
                        start = Offset(center.x + 50f, center.y - 80f),
                        end = Offset(center.x + 50f, center.y + 80f),
                        strokeWidth = 8f
                    )
                }

                // Robe bottom with sway
                val robePath = Path().apply {
                    moveTo(center.x - 40f + robeSwayAnim, center.y + 30f)
                    quadraticTo(
                        center.x, center.y + 120f,
                        center.x + 40f + robeSwayAnim, center.y + 30f
                    )
                    lineTo(center.x + 60f + robeSwayAnim, center.y + 120f)
                    lineTo(center.x - 60f + robeSwayAnim, center.y + 120f)
                    close()
                }
                drawPath(robePath, Color(0xFF663399))

                // Hat
                val hatPath = Path().apply {
                    moveTo(center.x - 40f, center.y - 60f)
                    lineTo(center.x, center.y - 120f)
                    lineTo(center.x + 40f, center.y - 60f)
                    close()
                }
                drawPath(hatPath, Color(0xFF9370DB))

                // Face
                drawCircle(
                    color = Color(0xFFFFE4B5),
                    radius = 30f,
                    center = center.copy(y = center.y - 40f)
                )

                // Eyes
                drawCircle(
                    color = Color.Black,
                    radius = 4f,
                    center = center.copy(x = center.x - 10f, y = center.y - 45f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 4f,
                    center = center.copy(x = center.x + 10f, y = center.y - 45f)
                )

                // Beard
                val beardPath = Path().apply {
                    moveTo(center.x - 30f, center.y - 30f)
                    quadraticTo(
                        center.x, center.y,
                        center.x + 30f, center.y - 30f
                    )
                }
                drawPath(beardPath, Color.White)

                // Arms
                val leftArmPath = Path().apply {
                    moveTo(center.x - 40f, center.y - 20f)
                    quadraticTo(
                        center.x - 60f, center.y,
                        center.x - 50f, center.y + 30f
                    )
                }
                drawPath(leftArmPath, Color(0xFF663399))

                val rightArmPath = Path().apply {
                    moveTo(center.x + 40f, center.y - 20f)
                    quadraticTo(
                        center.x + 60f, center.y,
                        center.x + 50f, center.y + 30f
                    )
                }
                drawPath(rightArmPath, Color(0xFF663399))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "WizardlyDo",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xFF9370DB),
                        blurRadius = 3f,
                        offset = Offset(0f, 0f)
                    )
                ),
                modifier = Modifier.offset(y = textFloatAnim.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SplashContentPreview() {
    WizardlyDoTheme {
        SplashContent()
    }
}
