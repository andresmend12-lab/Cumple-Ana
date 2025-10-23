package com.example.birthday.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.example.birthday.R
import com.example.birthday.data.repo.CumpleRepository

@Composable
fun MemoriesScreen(
    repository: CumpleRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val finalVideo by repository.observeFinalVideo().collectAsState(initial = null)
    val photos by repository.observeAllPhotos().collectAsState(initial = emptyList())
    val activities by repository.observeActivities().collectAsState(initial = emptyList())
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF9F2), Color(0xFFFFD166).copy(alpha = 0.2f))))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text(text = stringResource(id = R.string.back))
            }
            if (finalVideo != null) {
                Button(onClick = { shareUri(context, Uri.parse(finalVideo!!.uri), "video/mp4") }) {
                    Text(text = stringResource(id = R.string.share))
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(id = R.string.memories_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        finalVideo?.let { video ->
            val player = remember(video.uri) {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(Uri.parse(video.uri)))
                    prepare()
                    playWhenReady = false
                }
            }
            DisposableEffect(player) {
                onDispose { player.release() }
            }
            Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 4.dp) {
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            this.player = player
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(id = R.string.photos_section_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        activities.forEach { activity ->
            val activityPhotos = photos.filter { it.activityId == activity.id }
            if (activityPhotos.isNotEmpty()) {
                Text(text = activity.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(activityPhotos) { photo ->
                        val uri = Uri.parse(photo.uri)
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.White, shape = RoundedCornerShape(24.dp))
                                .clickable { selectedPhoto = uri }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }

    selectedPhoto?.let { uri ->
        PhotoFullScreen(uri = uri, onDismiss = { selectedPhoto = null })
    }
}

@Composable
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
                }
            }
        }
    }
}

private fun shareUri(context: android.content.Context, uri: Uri, mimeType: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
}
