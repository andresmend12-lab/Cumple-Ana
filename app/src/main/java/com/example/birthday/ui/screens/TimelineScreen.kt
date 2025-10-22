package com.example.birthday.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.birthday.R
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.ui.components.ActivityCard

private val ActivityIcons = mapOf(
    1 to R.drawable.ic_crown,
    2 to R.drawable.ic_breakfast,
    3 to R.drawable.ic_gift_box,
    4 to R.drawable.ic_coffee,
    5 to R.drawable.ic_cake,
    6 to R.drawable.ic_perfume,
    7 to R.drawable.ic_necklace,
    8 to R.drawable.ic_sushi
)

@Composable
fun TimelineScreen(
    repository: CumpleRepository,
    onOpenActivity: (Int) -> Unit,
    onShowMemories: () -> Unit
) {
    val activities by repository.observeActivities().collectAsState(initial = emptyList())
    val firstPendingOrder = activities.firstOrNull { !it.isCompleted }?.order
    val finalVideo by repository.observeFinalVideo().collectAsState(initial = null)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val colors = listOf(Color(0xFFFFF9F2), Color(0xFFFFD166).copy(alpha = 0.2f), Color(0xFFFF6B6B).copy(alpha = 0.1f))
                drawRect(Brush.verticalGradient(colors))
                val waveHeight = size.height / 8f
                val wavePaint = Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFF06D6A0).copy(alpha = 0.15f)))
                for (i in 0..5) {
                    drawRect(
                        brush = wavePaint,
                        topLeft = Offset(x = 0f, y = i * waveHeight),
                        size = androidx.compose.ui.geometry.Size(width = size.width, height = waveHeight / 2f)
                    )
                }
            }
            .padding(top = 24.dp)
    ) {
        if (activities.isEmpty()) {
            Text(
                text = "Cargando actividades...",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column {
                Text(
                    text = "¡Feliz cumple, Ana!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                if (finalVideo != null) {
                    Button(
                        onClick = onShowMemories,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(text = stringResource(id = R.string.view_memories))
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(activities, key = { _, item -> item.id }) { index, activity ->
                        val iconRes = ActivityIcons[activity.id] ?: R.drawable.ic_cake
                        val isLocked = if (activity.isCompleted) false else activity.order != firstPendingOrder
                        ActivityCard(
                            activity = activity,
                            iconRes = iconRes,
                            position = index + 1,
                            total = activities.size,
                            isLocked = isLocked,
                            onClick = {
                                if (!isLocked) onOpenActivity(activity.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
