package com.example.birthday.ui.components

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.example.birthday.R
import com.example.birthday.camera.PhotoCapture

@Composable
fun CameraCaptureDialog(
    photoCapture: PhotoCapture,
    lifecycleOwner: LifecycleOwner,
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
    val previewView = remember { PreviewView(context) }

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
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
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
                        enabled = imageCapture != null,
                        modifier = Modifier.fillMaxWidth()
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