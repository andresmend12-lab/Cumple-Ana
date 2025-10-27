package com.example.birthday.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@Composable
fun TimelineScreen(
    repository: CumpleRepository,
    onOpenActivity: (Int) -> Unit,
    onOpenAlbum: () -> Unit
) {
    val timelineStates by repository.observeTimelineState().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val colors = listOf(
                    Color(0xFFFFF9F2),
                    Color(0xFFFFD166).copy(alpha = 0.15f),
                    Color(0xFFFF6B6B).copy(alpha = 0.1f)
                )
                drawRect(Brush.verticalGradient(colors))
                val waveHeight = size.height / 8f
                val wavePaint = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color(0xFF06D6A0).copy(alpha = 0.12f))
                )
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
        if (timelineStates.isEmpty()) {
            Text(
                text = "Cargando actividades...",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "¡Feliz cumple, Ana!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                Button(
                    onClick = onOpenAlbum,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(id = R.string.view_album))
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(timelineStates, key = { it.activity.id }) { state ->
                        ActivityCard(
                            state = state,
                            onClick = {
                                if (state.isAvailable) {
                                    onOpenActivity(state.activity.id)
                                }
                            },
                            onSkipTimer = {
                                scope.launch {
                                    repository.skipWaitForActivity(state.activity.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
