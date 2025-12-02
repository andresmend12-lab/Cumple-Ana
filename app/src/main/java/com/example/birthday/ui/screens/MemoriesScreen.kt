package com.example.birthday.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
<<<<<<< HEAD
=======
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
>>>>>>> 9a89eb76a4ea0be0e8bbac98c011fc775f729272
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.birthday.R
import com.example.birthday.data.model.ActivityEntity
import com.example.birthday.data.repo.CumpleRepository
<<<<<<< HEAD
import com.example.birthday.ui.components.ActivityIcons
=======
import com.example.birthday.gate.TimeGate
import java.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
>>>>>>> 9a89eb76a4ea0be0e8bbac98c011fc775f729272

@Composable
fun MemoriesScreen(
    repository: CumpleRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val photos by repository.observeAllPhotos().collectAsState(initial = emptyList())
    val activities by repository.observeActivities().collectAsState(initial = emptyList())
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var selectedActivityId by remember { mutableStateOf<Int?>(null) }

    val sortedActivities = remember(activities) { activities.sortedBy { it.order } }
    val groupedPhotos = remember(photos) { photos.groupBy { it.activityId } }
    val totalPhotos = photos.size
    val latestPhotoUri = remember(photos) {
        photos.maxByOrNull { it.createdAt }?.uri?.let { runCatching { Uri.parse(it) }.getOrNull() }
    }
    val allUris = remember(photos) { photos.mapNotNull { runCatching { Uri.parse(it.uri) }.getOrNull() } }
    val activitiesWithPhotos = remember(sortedActivities, groupedPhotos) {
        sortedActivities.filter { groupedPhotos[it.id]?.isNotEmpty() == true }
    }
    val displayedActivities = remember(selectedActivityId, activitiesWithPhotos) {
        if (selectedActivityId == null) activitiesWithPhotos else activitiesWithPhotos.filter { it.id == selectedActivityId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF9F2),
                        Color(0xFFFFD166).copy(alpha = 0.12f),
                        Color(0xFFFF6B6B).copy(alpha = 0.08f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                MemoriesTopBar(
                    onBack = onBack,
                    canShare = allUris.isNotEmpty(),
                    onShareAll = { shareMultiple(context, allUris) }
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(id = R.string.memories_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = stringResource(id = R.string.photos_section_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (totalPhotos == 0) {
                item {
                    EmptyMemoriesState()
                }
            } else {
                latestPhotoUri?.let { uri ->
                    item {
                        LatestMemoryHighlight(
                            totalPhotos = totalPhotos,
                            uri = uri,
                            onClick = { selectedPhoto = uri }
                        )
                    }
                }

                if (activitiesWithPhotos.size > 1) {
                    item {
                        MemoriesFilterRow(
                            activities = activitiesWithPhotos,
                            grouped = groupedPhotos,
                            selectedActivityId = selectedActivityId,
                            totalPhotos = totalPhotos,
                            onSelect = { id -> selectedActivityId = id }
                        )
                    }
                }

                displayedActivities.forEach { activity ->
                    val uris = groupedPhotos[activity.id]
                        ?.sortedBy { it.createdAt }
                        ?.mapNotNull { runCatching { Uri.parse(it.uri) }.getOrNull() }
                        ?: emptyList()
                    if (uris.isNotEmpty()) {
                        item {
                            MemoryActivityCard(
                                activity = activity,
                                uris = uris,
                                onShare = { shareMultiple(context, uris) },
                                onPhotoClick = { selectedPhoto = it }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        NextBirthdayCountdown()
    }

    selectedPhoto?.let { uri ->
        MemoryPhotoDialog(
            uri = uri,
            onDismiss = { selectedPhoto = null },
            onShare = { shareSingle(context, uri) },
            onOpen = { openUri(context, uri) }
        )
    }
}

@Composable
<<<<<<< HEAD
private fun MemoriesTopBar(onBack: () -> Unit, canShare: Boolean, onShareAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.filledTonalButtonColors()
        ) {
            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.back))
        }
        if (canShare) {
            Button(onClick = onShareAll) {
                Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.memories_share_all))
            }
=======
private fun NextBirthdayCountdown() {
    val remainingFlow = remember { MutableStateFlow(Duration.ZERO) }
    val remaining by remainingFlow.collectAsState()

    LaunchedEffect(Unit) {
        TimeGate.nextBirthdayCountdownFlow().collectLatest { duration ->
            remainingFlow.value = duration
        }
    }

    val safeDuration = if (remaining.isNegative) Duration.ZERO else remaining
    val totalSeconds = safeDuration.seconds
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(id = R.string.next_birthday_title), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(
                    id = R.string.next_birthday_countdown,
                    days,
                    hours,
                    minutes,
                    seconds
                ),
                style = MaterialTheme.typography.titleMedium
            )
>>>>>>> 9a89eb76a4ea0be0e8bbac98c011fc775f729272
        }
    }
}

@Composable
<<<<<<< HEAD
private fun LatestMemoryHighlight(totalPhotos: Int, uri: Uri, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = stringResource(id = R.string.memories_latest_title),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(id = R.string.memories_latest_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(id = R.string.memories_section_photos, totalPhotos),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
=======
private fun PhotoFullScreen(uri: Uri, onDismiss: () -> Unit) {
    val context = LocalContext.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(32.dp), tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { shareUri(context, uri, "image/jpeg") }) {
                        Text(text = stringResource(id = R.string.share))
                    }
                    Button(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.back))
                    }
>>>>>>> 9a89eb76a4ea0be0e8bbac98c011fc775f729272
                }
            }
        }
    }
}

@Composable
private fun MemoriesFilterRow(
    activities: List<ActivityEntity>,
    grouped: Map<Int, List<com.example.birthday.data.model.PhotoEntity>>,
    selectedActivityId: Int?,
    totalPhotos: Int,
    onSelect: (Int?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FilterChip(
                selected = selectedActivityId == null,
                onClick = { onSelect(null) },
                label = {
                    Text(
                        text = "${stringResource(id = R.string.memories_filter_all)} ($totalPhotos)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Celebration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            )
        }

        items(activities) { activity ->
            val count = grouped[activity.id]?.size ?: 0
            FilterChip(
                selected = selectedActivityId == activity.id,
                onClick = { onSelect(activity.id) },
                label = {
                    Text(
                        text = "${activity.title} ($count)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = ActivityIcons.painterForId(activity.id),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(2.dp)
                            .size(28.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun MemoryActivityCard(
    activity: ActivityEntity,
    uris: List<Uri>,
    onShare: () -> Unit,
    onPhotoClick: (Uri) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Icon(
                            painter = ActivityIcons.painterForId(activity.id),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = activity.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = stringResource(id = R.string.memories_section_photos, uris.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onShare) {
                    Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uris) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .heightIn(min = 140.dp)
                            .width(140.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onPhotoClick(uri) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMemoriesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Icon(
                painter = ActivityIcons.painterForId(1),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }
        Text(
            text = stringResource(id = R.string.memories_empty_cta),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun MemoryPhotoDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp),
                    contentScale = ContentScale.Crop
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.share))
                    }
                    Button(
                        onClick = onOpen,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(text = stringResource(id = R.string.memories_open_external))
                    }
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(text = stringResource(id = R.string.back))
                }
            }
        }
    }
}

private fun shareSingle(context: Context, uri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
    }
}

private fun shareMultiple(context: Context, uris: List<Uri>) {
    if (uris.isEmpty()) return
    val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
    }
}

private fun openUri(context: Context, uri: Uri) {
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(viewIntent, context.getString(R.string.memories_open_external)))
    }
}
