package com.example.birthday.media

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Statistics
import com.arthenica.ffmpegkit.StatisticsCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.coroutines.resume

class FfmpegVideoGenerator(private val context: Context) : VideoGenerator {
    override val isAvailable: Boolean = true
    override val availabilityMessage: String? = null

    override suspend fun generateVideo(
        clips: List<VideoGenerator.PhotoClip>,
        musicUri: Uri?,
        overlayTitles: Boolean,
        displayName: String,
        onProgress: (Int) -> Unit
    ): Uri {
        require(clips.isNotEmpty()) { "No hay fotos para generar el vídeo" }

        val tempDir = File(context.cacheDir, "ffmpeg_temp").apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        val photoPaths = clips.mapIndexed { index, clip ->
            val file = File(tempDir, String.format(Locale.US, "img_%04d.jpg", index + 1))
            context.contentResolver.openInputStream(clip.uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("No se pudo leer la imagen")
            file.absolutePath
        }

        val musicPath = musicUri?.let { copyToTemp(it, tempDir, "music") }
        val photoDuration = 2.5
        val crossfade = 0.5
        val fps = 30
        val expectedDurationMs = ((photoDuration + (clips.size - 1) * (photoDuration - crossfade)) * 1000).toLong()

        val command = buildCommand(photoPaths, clips.map { it.title }, overlayTitles, musicPath, photoDuration, crossfade, fps)
        val outputFile = File(tempDir, "output.mp4")
        val session = suspendCancellableCoroutine<com.arthenica.ffmpegkit.Session> { continuation ->
            val statisticsCallback = StatisticsCallback { statistics: Statistics ->
                val progress = if (expectedDurationMs > 0) {
                    (statistics.time * 100 / expectedDurationMs).coerceIn(0, 100)
                } else {
                    0
                }
                onProgress(progress)
            }
            val ffmpegSession = FFmpegKit.executeAsync("$command \"${outputFile.absolutePath}\"", { completedSession ->
                continuation.resume(completedSession)
            }, { _ ->
                // optional logging
            }, statisticsCallback)
            continuation.invokeOnCancellation {
                ffmpegSession.cancel()
            }
        }

        if (!ReturnCode.isSuccess(session.returnCode)) {
            throw IllegalStateException("FFmpeg falló: ${session.failStackTrace ?: session.returnCode}")
        }

        val videoUri = saveToMediaStore(outputFile, displayName)
        onProgress(100)
        tempDir.deleteRecursively()
        return videoUri
    }

    private fun buildCommand(
        imagePaths: List<String>,
        titles: List<String>,
        overlayTitles: Boolean,
        musicPath: String?,
        photoDuration: Double,
        crossfade: Double,
        fps: Int
    ): String {
        val inputs = buildString {
            imagePaths.forEach { path ->
                append(" -loop 1 -t ${"%.2f".format(Locale.US, photoDuration)} -i \"$path\"")
            }
            musicPath?.let { append(" -i \"$it\"") }
        }
        val filterBuilder = StringBuilder()
        imagePaths.forEachIndexed { index, _ ->
            val title = titles.getOrNull(index)?.replace("'", "\\'") ?: ""
            filterBuilder.append("[$index:v]scale=1920:1080:force_original_aspect_ratio=decrease,")
            filterBuilder.append("pad=1920:1080:(ow-iw)/2:(oh-ih)/2,format=yuv420p,trim=duration=${"%.2f".format(Locale.US, photoDuration)},setpts=PTS-STARTPTS")
            if (overlayTitles && title.isNotEmpty()) {
                filterBuilder.append(",drawtext=text='$title':fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=56:fontcolor=white:shadowcolor=#80000000:shadowx=3:shadowy=3:x=(w-text_w)/2:y=0.12*h:enable='between(t,0,1.5)'")
            }
            filterBuilder.append("[v$index];")
        }
        var currentLabel = "v0"
        if (imagePaths.size == 1) {
            filterBuilder.append("[v0]fps=$fps[vout];")
        } else {
            for (i in 1 until imagePaths.size) {
                val nextLabel = "v$i"
                val outLabel = if (i == imagePaths.size - 1) "vout" else "vx$i"
                val offset = (photoDuration - crossfade) * i
                filterBuilder.append("[$currentLabel][$nextLabel]xfade=transition=crossfade:duration=${"%.2f".format(Locale.US, crossfade)}:offset=${"%.2f".format(Locale.US, offset)}[$outLabel];")
                currentLabel = outLabel
            }
            filterBuilder.append("[$currentLabel]fps=$fps[vout];")
        }
        val filter = filterBuilder.toString().trimEnd(';')

        val outputArgs = buildString {
            append(" -map [vout] -c:v libx264 -pix_fmt yuv420p -r $fps -movflags +faststart")
            if (musicPath != null) {
                val audioIndex = imagePaths.size
                append(" -map $audioIndex:a -c:a aac -shortest")
            }
        }
        return "-y$inputs -filter_complex \"$filter\"$outputArgs"
    }

    private fun copyToTemp(uri: Uri, dir: File, prefix: String): String {
        val mime = context.contentResolver.getType(uri)
        val extension = when (mime) {
            "audio/mpeg" -> ".mp3"
            "audio/aac" -> ".aac"
            "audio/mp4" -> ".m4a"
            else -> ".tmp"
        }
        val file = File(dir, "$prefix-${System.currentTimeMillis()}$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        } ?: error("No se pudo leer el audio")
        return file.absolutePath
    }

    private fun saveToMediaStore(file: File, displayName: String): Uri {
        val resolver: ContentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/CumpleAna")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values) ?: throw IllegalStateException("No se pudo crear el archivo")
        resolver.openOutputStream(uri)?.use { output ->
            file.inputStream().use { input ->
                input.copyTo(output)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val update = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
            resolver.update(uri, update, null, null)
        }
        return uri
    }
}
