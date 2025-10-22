package com.example.birthday.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.birthday.R
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.media.VideoGenerator
import com.example.birthday.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun GenerateVideoScreen(
    repository: CumpleRepository,
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    val photos by repository.observeAllPhotos().collectAsState(initial = emptyList())
    val activities by repository.observeActivities().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val videoGenerator = remember { VideoGenerator(context) }

    var musicUri by remember { mutableStateOf<Uri?>(null) }
    var overlayTitles by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf<GenerationStatus>(GenerationStatus.Idle) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val musicPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                }
            }
            musicUri = uri
        }
    }

    Surface(modifier = Modifier.padding(16.dp), tonalElevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.video_params_title), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = stringResource(id = R.string.video_resolution))
            Text(text = stringResource(id = R.string.video_fps))
            Text(text = stringResource(id = R.string.video_duration))
            Text(text = stringResource(id = R.string.video_crossfade))

            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(id = R.string.title_overlay_label))
                Switch(checked = overlayTitles, onCheckedChange = { overlayTitles = it })
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = musicUri?.lastPathSegment ?: stringResource(id = R.string.no_music))
                Button(onClick = { musicPicker.launch(arrayOf("audio/*")) }) {
                    Text(text = stringResource(id = R.string.select_music))
                }
            }

            if (status is GenerationStatus.InProgress) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                Text(text = stringResource(id = R.string.video_generation_in_progress), modifier = Modifier.padding(top = 8.dp))
            }
            if (status is GenerationStatus.Success) {
                Text(text = stringResource(id = R.string.video_generation_success), color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 8.dp))
            }
            if (status is GenerationStatus.Error && errorMessage != null) {
                Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.padding(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onBack) {
                    Text(text = stringResource(id = R.string.back))
                }
                Button(
                    onClick = {
                        if (photos.isEmpty()) {
                            errorMessage = stringResource(id = R.string.add_photos_before_video)
                            status = GenerationStatus.Error
                            return@Button
                        }
                        status = GenerationStatus.InProgress
                        progress = 0
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val clips = photos.map { photo ->
                                    val title = activities.firstOrNull { it.id == photo.activityId }?.title ?: ""
                                    VideoGenerator.PhotoClip(uri = Uri.parse(photo.uri), title = title)
                                }
                                val displayName = DateUtils.generateVideoName()
                                val uri = videoGenerator.generateVideo(
                                    clips = clips,
                                    musicUri = musicUri,
                                    overlayTitles = overlayTitles,
                                    displayName = displayName,
                                    onProgress = { progress = it }
                                )
                                repository.storeFinalVideo(uri.toString(), System.currentTimeMillis())
                                status = GenerationStatus.Success
                                onFinished()
                            } catch (t: Throwable) {
                                status = GenerationStatus.Error
                                errorMessage = t.localizedMessage
                            }
                        }
                    },
                    enabled = status !is GenerationStatus.InProgress
                ) {
                    Text(text = stringResource(id = R.string.generate_video))
                }
            }
        }
    }
}

enum class GenerationStatus { Idle, InProgress, Success, Error }
