package com.example.birthday.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import com.example.birthday.R
import com.example.birthday.camera.PhotoCapture
import com.example.birthday.data.model.ActivityCompletionResult
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.ui.components.ActivityCelebrationDialog
import com.example.birthday.ui.components.ActivityCelebrationState
import com.example.birthday.ui.components.ActivityIcons
import com.example.birthday.ui.components.PhotoGrid
import com.example.birthday.ui.components.SkipWaitAccessIcon
import com.example.birthday.util.DateUtils
import com.example.birthday.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

@Composable
fun ActivityDetailScreen(
    activityId: Int,
    repository: CumpleRepository,
    onBack: () -> Unit,
    onCompleted: () -> Unit
) {
    val activity by repository.observeActivity(activityId).collectAsState(initial = null)
    val photos by repository.observePhotos(activityId).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var showCamera by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var pendingPhoto by remember { mutableStateOf<Uri?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var waitingUnlockAt by remember { mutableStateOf<ZonedDateTime?>(null) }
    var showPreviousIncomplete by remember { mutableStateOf(false) }
    var countdownText by remember { mutableStateOf<String?>(null) }
    var latestSavedPhoto by remember { mutableStateOf<Uri?>(null) }
    var celebrationState by remember { mutableStateOf<ActivityCelebrationState?>(null) }

    val photoCapture = remember { PhotoCapture(context) }
    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission.value = granted
        if (granted) {
            lensFacing = CameraSelector.LENS_FACING_FRONT
            showCamera = true
        }
    }

    val photoUris = photos.mapNotNull { runCatching { Uri.parse(it.uri) }.getOrNull() }
    val unlockAt = activity?.unlockAtEpochMillis?.let { Instant.ofEpochMilli(it).atZone(TimeUtils.zoneId) }

    LaunchedEffect(photoUris) {
        latestSavedPhoto = photoUris.lastOrNull()
    }

    LaunchedEffect(unlockAt, activity?.photoCompleted, activity?.isUnlocked) {
        countdownText = null
        val target = unlockAt
        if (activity?.photoCompleted == true && target != null && activity?.isUnlocked != true) {
            while (true) {
                val now = TimeUtils.now()
                if (now.isBefore(target)) {
                    countdownText = TimeUtils.formatDuration(Duration.between(now, target))
                    delay(1_000)
                } else {
                    countdownText = null
                    break
                }
            }
        }
    }

    LaunchedEffect(countdownText) {
        if (countdownText == null) {
            waitingUnlockAt = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFFFF8E72), Color(0xFFFFD166))))
                .padding(bottom = 32.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(16.dp)) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = ActivityIcons.painterForId(activity?.id ?: 0),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = activity?.title.orEmpty(),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activity?.description.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                if (hasCameraPermission.value) {
                    lensFacing = CameraSelector.LENS_FACING_FRONT
                    showCamera = true
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Text(text = stringResource(id = R.string.take_photo))
            }
            Text(
                text = stringResource(id = R.string.step_progress, activity?.order ?: 0, 8),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        latestSavedPhoto?.let { latest ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.latest_photo_preview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(28.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(latest),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (photoUris.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_photos_yet),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = stringResource(id = R.string.add_more_photos),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        } else {
            PhotoGrid(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(260.dp),
                photos = photoUris
            )
        }

        countdownText?.let { remaining ->
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.activity_wait_until,
                            TimeUtils.formatUnlockTime(unlockAt ?: TimeUtils.now())
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = remaining,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    SkipWaitAccessIcon(
                        onSkipConfirmed = {
                            coroutineScope.launch {
                                repository.skipWaitForActivity(activityId)
                                countdownText = null
                                waitingUnlockAt = null
                            }
                        },
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }

        if (showPreviousIncomplete) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = stringResource(id = R.string.activity_status_previous_incomplete),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        waitingUnlockAt?.let { target ->
            if (countdownText == null) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.activity_wait_until,
                            TimeUtils.formatUnlockTime(target)
                        ),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text(text = stringResource(id = R.string.back))
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = repository.tryCompleteActivity(activityId)
                        when (result) {
                            is ActivityCompletionResult.Completed -> {
                                showPreviousIncomplete = false
                                waitingUnlockAt = null
                                celebrationState = ActivityCelebrationState(result.isFinal)
                            }
                            is ActivityCompletionResult.WaitingTime -> {
                                showPreviousIncomplete = false
                                waitingUnlockAt = result.unlockAt
                            }
                            ActivityCompletionResult.PreviousIncomplete -> {
                                showPreviousIncomplete = true
                            }
                            ActivityCompletionResult.PhotoMissing -> {
                                // no-op, button disabled otherwise
                            }
                            ActivityCompletionResult.NotFound -> {
                                showPreviousIncomplete = false
                                waitingUnlockAt = null
                            }
                        }
                    }
                },
                enabled = activity?.photoCompleted == true
            ) {
                Text(text = stringResource(id = R.string.continue_button))
            }
        }
    }

    if (showCamera) {
        CameraCaptureDialog(
            photoCapture = photoCapture,
            lifecycleOwner = lifecycleOwner,
            imageCapture = imageCapture,
            onImageCapture = { imageCapture = it },
            onDismiss = {
                pendingPhoto?.let { photoCapture.discardPhoto(it) }
                pendingPhoto = null
                showCamera = false
                imageCapture = null
            },
            onTakePhoto = { capture ->
                coroutineScope.launch {
                    val name = DateUtils.generatePhotoName(activity?.order ?: 0)
                    try {
                        val uri = photoCapture.takePhoto(capture, name)
                        pendingPhoto = uri
                    } catch (t: Throwable) {
                        cameraError = t.localizedMessage
                    }
                }
            },
            pendingPhoto = pendingPhoto,
            onSavePhoto = { uri ->
                coroutineScope.launch {
                    activity?.let { act ->
                        photoCapture.finalizePendingPhoto(uri)
                        repository.addPhoto(
                            activityId = act.id,
                            uri = uri.toString(),
                            createdAt = System.currentTimeMillis()
                        )
                    }
                    pendingPhoto = null
                    showCamera = false
                    imageCapture = null
                    showPreviousIncomplete = false
                    latestSavedPhoto = uri
                }
            },
            onRetake = { uri ->
                photoCapture.discardPhoto(uri)
                pendingPhoto = null
            },
            lensFacing = lensFacing,
            onSwitchCamera = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            }
        )
    }

    celebrationState?.let { state ->
        ActivityCelebrationDialog(
            state = state,
            onConfirm = {
                celebrationState = null
                onCompleted()
            }
        )
    }

    cameraError?.let { message ->
        AlertDialog(
            onDismissRequest = { cameraError = null },
            confirmButton = {
                Button(onClick = { cameraError = null }) {
                    Text(text = "OK")
                }
            },
            title = { Text(text = stringResource(id = R.string.camera_error)) },
            text = { Text(text = message ?: "") }
        )
    }
}

@Composable
private fun CameraCaptureDialog(
    photoCapture: PhotoCapture,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    imageCapture: ImageCapture?,
    onImageCapture: (ImageCapture) -> Unit,
    onDismiss: () -> Unit,
    onTakePhoto: (ImageCapture) -> Unit,
    pendingPhoto: Uri?,
    onSavePhoto: (Uri) -> Unit,
    onRetake: (Uri) -> Unit,
    lensFacing: Int,
    onSwitchCamera: () -> Unit
) {
    val context = LocalContext.current
    val previewView = remember { androidx.camera.view.PreviewView(context) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(32.dp), tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (pendingPhoto == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        AndroidView(
                            factory = {
                                previewView.apply {
                                    scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = onSwitchCamera,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cameraswitch,
                                contentDescription = stringResource(id = R.string.switch_camera)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val capture = imageCapture
                            if (capture != null) {
                                onTakePhoto(capture)
                            }
                        },
                        enabled = imageCapture != null
                    ) {
                        Text(text = stringResource(id = R.string.take_photo))
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(pendingPhoto),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { onRetake(pendingPhoto) }) {
                            Text(text = stringResource(id = R.string.retake_photo))
                        }
                        Button(onClick = { onSavePhoto(pendingPhoto) }) {
                            Text(text = stringResource(id = R.string.save_photo))
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(previewView, lensFacing) {
        val capture = photoCapture.bind(previewView, lifecycleOwner, lensFacing)
        onImageCapture(capture)
    }
}
