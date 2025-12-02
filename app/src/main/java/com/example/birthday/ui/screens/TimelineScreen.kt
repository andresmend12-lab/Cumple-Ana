package com.example.birthday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onOpenAlbum: () -> Unit,
    isReviewMode: Boolean = false,
    onNavigateToFinal: () -> Unit
) {
    val timelineStates by repository.observeTimelineState().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // Solo redirigir si NO estamos revisando y todo está completo
    val allCompleted = remember(timelineStates) {
        timelineStates.isNotEmpty() && timelineStates.all { it.hasPhoto && it.activity.isCompleted }
    }

    LaunchedEffect(allCompleted, isReviewMode) {
        if (allCompleted && !isReviewMode) {
            onNavigateToFinal()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAlbum,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Rounded.Collections, contentDescription = "Álbum")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFF9F2), Color(0xFFFFE4E1))
                    )
                )
                .padding(padding)
        ) {
            if (timelineStates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "¡Feliz Cumple,",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Ana!",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tu día especial paso a paso.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

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
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    if (isReviewMode) {
                        item {
                            Button(
                                onClick = onNavigateToFinal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Text("Volver a la Pantalla Final")
                            }
                        }
                    }
                }
            }
        }
    }
}