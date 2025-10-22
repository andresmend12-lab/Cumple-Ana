package com.example.birthday.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.random.Random

@Composable
fun ConfettiCanvas(
    modifier: Modifier = Modifier,
    colors: List<Color>
) {
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti-progress"
    )

    val particles = remember {
        List(60) {
            ConfettiParticle(
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                size = Random.nextFloat().coerceIn(0.02f, 0.08f),
                color = colors.random()
            )
        }
    }

    Canvas(modifier = modifier.background(Brush.radialGradient(colors))) {
        val width = size.width
        val height = size.height
        particles.forEachIndexed { index, particle ->
            val offsetProgress = (progress.value + index * 0.02f) % 1f
            val x = particle.startX * width
            val y = (particle.startY + offsetProgress) % 1f * height
            val confettiSize = particle.size * width
            withTransform({
                translate(left = x, top = y)
                rotate(offsetProgress * 360f)
            }) {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(-confettiSize / 2f, -confettiSize / 6f),
                    size = androidx.compose.ui.geometry.Size(confettiSize, confettiSize / 3f)
                )
            }
        }
    }
}

data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val color: Color
)
