package com.example.birthday.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.birthday.R
import com.example.birthday.camera.PhotoCapture
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.ui.components.CameraCaptureDialog
import com.example.birthday.ui.components.FullScreenImageDialog
import com.example.birthday.ui.components.PhotoGrid
import com.example.birthday.util.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun YearlyGalleryScreen(
    year: Int,
    repository: CumpleRepository,
    onBack: () -> Unit
) {
    val photos by repository.observePhotosForYear(year).collectAsState(initial = emptyList())
    // Convertimos a URIs para uso en lógica
    val photoUris = remember(photos) { photos.mapNotNull { runCatching { Uri.parse(it.uri) }.getOrNull() } }

    var showCamera by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val photoCapture = remember { PhotoCapture(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) showCamera = true
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        showCamera = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Añadir foto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9F2))
                .padding(padding)
        ) {
            // Header con Botón de Compartir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Espaciado para empujar el botón compartir
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                    Text(
                        text = stringResource(id = R.string.gallery_header, year),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Botón Compartir TODO (Visible solo si hay fotos)
                if (photoUris.isNotEmpty()) {
                    IconButton(
                        onClick = { ShareUtils.shareImages(context, photoUris) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Compartir Álbum",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (photos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(id = R.string.gallery_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                PhotoGrid(
                    photos = photos,
                    onRemove = { photoEntity ->
                        coroutineScope.launch {
                            try {
                                val uri = Uri.parse(photoEntity.uri)
                                photoCapture.discardPhoto(uri)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            repository.deletePhoto(photoEntity)
                        }
                    },
                    onClick = { photoEntity ->
                        runCatching { Uri.parse(photoEntity.uri) }.getOrNull()?.let {
                            selectedPhotoUri = it
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showCamera) {
        CameraCaptureDialog(
            photoCapture = photoCapture,
            lifecycleOwner = lifecycleOwner,
            imageCapture = imageCapture,
            onImageCapture = { imageCapture = it },
            onDismiss = { showCamera = false },
            onTakePhoto = { capture ->
                coroutineScope.launch {
                    val name = "Gallery_${year}_${System.currentTimeMillis()}.jpg"
                    try {
                        val uri = photoCapture.takePhoto(capture, name)
                        photoCapture.finalizePendingPhoto(uri)
                        repository.addPhoto(0, uri.toString(), System.currentTimeMillis())
                        showCamera = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            pendingPhoto = null,
            onSavePhoto = {},
            onRetake = {},
            lensFacing = CameraSelector.LENS_FACING_BACK,
            onSwitchCamera = {}
        )
    }

    selectedPhotoUri?.let { uri ->
        FullScreenImageDialog(
            photoUri = uri,
            onDismiss = { selectedPhotoUri = null }
        )
    }
}