package com.example.birthday.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFB4A2),
                            Color(0xFFFFD166),
                            Color(0xFFFEE440)
                        )
                    )
                )
        ) {
            if (state.isFinal) {
                FireworksAnimation(modifier = Modifier.matchParentSize())
            } else {
                ConfettiAnimation(modifier = Modifier.matchParentSize())
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.9f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "gift")
                    val pulse by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 900, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "giftPulse"
                    )

                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(
                                if (state.isFinal) Icons.Rounded.Cake else Icons.Rounded.CardGiftcard
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(20.dp)
                                .scale(pulse)
                        )
                    }

                    val title = if (state.isFinal) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.headlineSmall
                    }

                    Text(
                        text = if (state.isFinal) {
                            stringResource(id = R.string.celebration_final_title)
                        } else {
                            stringResource(id = R.string.celebration_completed_title)
                        },
                        style = title.copy(fontWeight = FontWeight.ExtraBold)
                    )

                    Text(
                        text = if (state.isFinal) {
                            stringResource(id = R.string.celebration_final_message)
                        } else {
                            stringResource(id = R.string.celebration_completed_message)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    AnimatedVisibility(
                        visible = !state.isFinal,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Celebration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(id = R.string.celebration_tagline),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Button(onClick = onConfirm) {
                        Text(
                            text = if (state.isFinal) {
                                stringResource(id = R.string.celebration_final_cta)
                            } else {
                                stringResource(id = R.string.celebration_completed_cta)
                            }
                        )
                    }
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
            val drift = sin((playhead * 2f * PI.toFloat()) + piece.wobble) * 40f
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
                val angle = (2 * PI * index / sparks).toFloat()
                val end = Offset(
                    x = center.x + cos(angle) * radius,
                    y = center.y + sin(angle) * radius
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
