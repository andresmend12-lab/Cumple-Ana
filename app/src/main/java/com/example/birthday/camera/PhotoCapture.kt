package com.example.birthday.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PhotoCapture(private val context: Context) {
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
    private var cameraProvider: ProcessCameraProvider? = null

    // Verificar si la cámara existe
    suspend fun hasCamera(lensFacing: Int): Boolean {
        return try {
            val provider = cameraProviderFuture.await(context)
            provider.hasCamera(CameraSelector.Builder().requireLensFacing(lensFacing).build())
        } catch (e: Exception) {
            false
        }
    }

    // Liberar recursos manualmente para evitar bloqueos en dispositivos reales
    fun unbind() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun bind(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        lensFacing: Int
    ): ImageCapture {
        val provider = cameraProviderFuture.await(context)
        this.cameraProvider = provider

        // Configuración de Preview
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // CAMBIO CRÍTICO: Eliminamos setCaptureMode explícito.
        // Dejamos que CameraX decida la mejor configuración para el OnePlus 12.
        val imageCapture = ImageCapture.Builder()
            .setTargetRotation(previewView.display.rotation)
            .build()

        // Fallback: Si no hay cámara frontal, usa la trasera
        val finalLensFacing = if (provider.hasCamera(CameraSelector.Builder().requireLensFacing(lensFacing).build())) {
            lensFacing
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        val selector = CameraSelector.Builder()
            .requireLensFacing(finalLensFacing)
            .build()

        try {
            // Importante: Desvincular todo antes de volver a vincular
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageCapture
    }

    @SuppressLint("RestrictedApi", "MissingPermission")
    suspend fun takePhoto(imageCapture: ImageCapture, fileName: String): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CumpleAna")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        return suspendCancellableCoroutine { continuation ->
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = outputFileResults.savedUri
                        if (savedUri != null) {
                            continuation.resume(savedUri)
                        } else {
                            continuation.resumeWithException(IllegalStateException("La URI de la foto es nula"))
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }

    fun finalizePendingPhoto(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            try {
                context.contentResolver.update(uri, values, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun discardPhoto(uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Extensión para facilitar el uso de Futures
private suspend fun <T> ListenableFuture<T>.await(context: Context): T = suspendCancellableCoroutine { cont ->
    addListener({
        try {
            cont.resume(get())
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }, ContextCompat.getMainExecutor(context))
}