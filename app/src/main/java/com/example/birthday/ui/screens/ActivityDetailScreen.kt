package com.example.birthday.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.birthday.R
import com.example.birthday.camera.PhotoCapture
import com.example.birthday.data.model.ActivityCompletionResult
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.ui.components.*
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
    var celebrationState by remember { mutableStateOf<ActivityCelebrationState?>(null) }

    // Nuevo estado para el visor de pantalla completa
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

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

    val unlockAt = activity?.unlockAtEpochMillis?.let { Instant.ofEpochMilli(it).atZone(TimeUtils.zoneId) }

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
        if (countdownText == null) waitingUnlockAt = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back), tint = Color.White)
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = ActivityIcons.painterForId(activity?.id ?: 0),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(140.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Info Actividad
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = activity?.title.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activity?.description.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 24.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Grid de Fotos con opción de borrado y visor
                PhotoGrid(
                    photos = photos,
                    onRemove = { photoEntity ->
                        coroutineScope.launch {
                            // 1. Intentar borrar archivo físico
                            try {
                                val uri = Uri.parse(photoEntity.uri)
                                photoCapture.discardPhoto(uri)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            // 2. Borrar de la base de datos
                            repository.deletePhoto(photoEntity)
                        }
                    },
                    // AGREGADO: Manejador para abrir la foto en grande
                    onClick = { photoEntity ->
                        runCatching { Uri.parse(photoEntity.uri) }.getOrNull()?.let {
                            selectedPhotoUri = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                // Botón Foto
                Button(
                    onClick = {
                        if (hasCameraPermission.value) {
                            lensFacing = CameraSelector.LENS_FACING_FRONT
                            showCamera = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.take_photo))
                }

                // Cuenta atrás
                countdownText?.let { remaining ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.activity_wait_until,
                                    TimeUtils.formatUnlockTime(unlockAt ?: TimeUtils.now())
                                ),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = remaining,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
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
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Botón Continuar (Solo si hay fotos)
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
                                    // No debería ocurrir si el botón está disabled
                                }
                                ActivityCompletionResult.NotFound -> {
                                    showPreviousIncomplete = false
                                    waitingUnlockAt = null
                                }
                            }
                        }
                    },
                    enabled = photos.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(text = stringResource(id = R.string.continue_button))
                }
            }
        }
    }

    // Diálogo de cámara
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

    // Visor de imagen en grande
    selectedPhotoUri?.let { uri ->
        FullScreenImageDialog(
            photoUri = uri,
            onDismiss = { selectedPhotoUri = null }
        )
    }

    cameraError?.let { message ->
        AlertDialog(
            onDismissRequest = { cameraError = null },
            confirmButton = { Button(onClick = { cameraError = null }) { Text("OK") } },
            title = { Text(text = stringResource(id = R.string.camera_error)) },
            text = { Text(text = message ?: "") }
        )
    }
}