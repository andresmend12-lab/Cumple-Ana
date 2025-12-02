package com.example.birthday.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.birthday.R
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.ui.components.ConfettiCanvas
import com.example.birthday.ui.components.FullScreenImageDialog // Importante
import com.example.birthday.util.TimeUtils
import kotlinx.coroutines.delay
import java.time.Duration

@Composable
fun FinalCelebrationScreen(
    repository: CumpleRepository,
    onOpenYearGallery: (Int) -> Unit,
    onSeeActivities: () -> Unit
) {
    var remainingTime by remember { mutableStateOf(Duration.ZERO) }
    val galleries by repository.observePhotosByYear().collectAsState(initial = emptyMap())

    val currentYear = remember { TimeUtils.now().year }
    val currentPhotos = galleries[currentYear] ?: emptyList()

    // Estado para el visor de fotos
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            remainingTime = Duration.between(TimeUtils.now(), TimeUtils.getNextBirthday())
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ConfettiCanvas(
            modifier = Modifier.fillMaxSize(),
            colors = listOf(Color(0xFFFFD166).copy(alpha=0.3f), Color(0xFFFF6B6B).copy(alpha=0.3f), Color(0xFF118AB2).copy(alpha=0.3f))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Contador
            item {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.next_birthday_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = TimeUtils.formatDuration(remainingTime),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.next_birthday_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // 2. Fotos del Año Actual (Pulsables)
            if (currentPhotos.isNotEmpty()) {
                item {
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Recuerdos de $currentYear",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(currentPhotos) { photo ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clickable {
                                            // Abrir visor
                                            runCatching { Uri.parse(photo.uri) }.getOrNull()?.let {
                                                selectedPhotoUri = it
                                            }
                                        }
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(Uri.parse(photo.uri)),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Botón Repasar
            item {
                OutlinedButton(
                    onClick = onSeeActivities,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Repasar Actividades y Regalos")
                }
            }

            // 4. Galerías Pasadas
            item {
                Text(
                    text = "Cápsula del Tiempo",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }

            val pastYears = galleries.keys.filter { it < currentYear }.sortedDescending()

            if (pastYears.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aquí se guardarán tus próximos cumpleaños.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(pastYears) { year ->
                    YearlyGalleryCard(
                        year = year,
                        photoCount = galleries[year]?.size ?: 0,
                        onClick = { onOpenYearGallery(year) }
                    )
                }
            }
        }
    }

    // Diálogo de Foto a Pantalla Completa
    selectedPhotoUri?.let { uri ->
        FullScreenImageDialog(
            photoUri = uri,
            onDismiss = { selectedPhotoUri = null }
        )
    }
}

@Composable
fun YearlyGalleryCard(year: Int, photoCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cumpleaños $year",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$photoCount recuerdos guardados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}