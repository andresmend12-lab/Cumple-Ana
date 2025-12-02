package com.example.birthday.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas // Importación que faltaba
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.birthday.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Immutable
data class ActivityCelebrationState(val isFinal: Boolean)

@Composable
fun ActivityCelebrationDialog(
    state: ActivityCelebrationState,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onConfirm,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFF0F5), Color(0xFFE6E6FA))
                    )
                )
        ) {
            if (state.isFinal) {
                FireworksAnimation(modifier = Modifier.fillMaxSize())
            } else {
                ConfettiAnimation(modifier = Modifier.fillMaxSize())
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (state.isFinal) Icons.Rounded.EmojiEvents else Icons.Rounded.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .scale(scale),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (state.isFinal) stringResource(R.string.celebration_final_title)
                        else stringResource(R.string.celebration_completed_title),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (state.isFinal) stringResource(R.string.celebration_final_message)
                        else stringResource(R.string.celebration_completed_message),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = if (state.isFinal) stringResource(R.string.celebration_final_cta)
                        else stringResource(R.string.celebration_completed_cta),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfettiAnimation(modifier: Modifier = Modifier) {
    val colors = remember {
        listOf(
            Color(0xFFFF6B6B),
            Color(0xFFFFD166),
            Color(0xFF06D6A0),
            Color(0xFF118AB2),
            Color(0xFF8338EC)
        )
    }
    val pieces = remember {
        val random = Random(0xC0FFEE)
        List(36) {
            ConfettiPiece(
                xFactor = random.nextFloat(),
                offset = random.nextFloat(),
                size = 24f + random.nextFloat() * 18f,
                color = colors[random.nextInt(colors.size)],
                wobble = random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing)
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        pieces.forEach { piece ->
            val playhead = (progress + piece.offset) % 1f
            val y = height * playhead
            val angle = (playhead * 2f * PI.toFloat()) + piece.wobble
            val drift = sin(angle.toDouble()).toFloat() * 40f
            val x = (width * piece.xFactor + drift).coerceIn(0f, width)
            val alpha = 1f - playhead

            withTransform({
                translate(left = x, top = y - piece.size / 2f)
                rotate(degrees = (playhead * 360f) + piece.wobble * 45f)
            }) {
                drawRoundRect(
                    color = piece.color.copy(alpha = alpha.coerceIn(0.2f, 1f)),
                    topLeft = Offset(x = -piece.size / 2f, y = -piece.size / 2f),
                    size = Size(width = piece.size, height = piece.size * 1.6f),
                    cornerRadius = CornerRadius(piece.size / 3f)
                )
            }
        }
    }
}

@Composable
private fun FireworksAnimation(modifier: Modifier = Modifier) {
    val bursts = remember {
        val random = Random(0xF11EF1FE.toInt())
        val palette = listOf(
            Color(0xFFFF6B6B),
            Color(0xFFFFD166),
            Color(0xFF06D6A0),
            Color(0xFF118AB2),
            Color(0xFF9D4EDD)
        )
        List(6) {
            FireworkBurst(
                xFactor = 0.2f + random.nextFloat() * 0.6f,
                yFactor = 0.2f + random.nextFloat() * 0.5f,
                color = palette[random.nextInt(palette.size)],
                offset = random.nextFloat()
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "fireworks")
    val base by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing)
        ),
        label = "fireworksProgress"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        bursts.forEach { burst ->
            val playhead = (base + burst.offset) % 1f
            val center = Offset(width * burst.xFactor, height * burst.yFactor)
            val radius = playhead * (size.minDimension / 2.5f)
            val alpha = (1f - playhead).coerceIn(0f, 1f)
            if (alpha <= 0f) return@forEach

            drawCircle(
                color = burst.color.copy(alpha = 0.3f * alpha),
                radius = radius,
                center = center
            )

            val sparks = 12
            repeat(sparks) { index ->
                val angleFraction = index.toFloat() / sparks.toFloat()
                val angle = 2f * PI.toFloat() * angleFraction
                val cosAngle = cos(angle.toDouble()).toFloat()
                val sinAngle = sin(angle.toDouble()).toFloat()
                val end = Offset(
                    x = center.x + cosAngle * radius,
                    y = center.y + sinAngle * radius
                )
                val stroke = 6f * alpha
                drawLine(
                    color = burst.color.copy(alpha = alpha),
                    start = center,
                    end = end,
                    strokeWidth = stroke
                )
            }
        }
    }
}

private data class ConfettiPiece(
    val xFactor: Float,
    val offset: Float,
    val size: Float,
    val color: Color,
    val wobble: Float
)

private data class FireworkBurst(
    val xFactor: Float,
    val yFactor: Float,
    val color: Color,
    val offset: Float
)