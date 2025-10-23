package com.example.birthday.media

import android.content.Context
import android.net.Uri
import com.example.birthday.BuildConfig

interface VideoGenerator {
    val isAvailable: Boolean
    val availabilityMessage: String?

    suspend fun generateVideo(
        clips: List<PhotoClip>,
        musicUri: Uri?,
        overlayTitles: Boolean,
        displayName: String,
        onProgress: (Int) -> Unit
    ): Uri

    data class PhotoClip(val uri: Uri, val title: String)
}

fun createVideoGenerator(context: Context): VideoGenerator {
    if (BuildConfig.FFMPEG_ENABLED) {
        val generator = runCatching {
            val clazz = Class.forName("com.example.birthday.media.FfmpegVideoGenerator")
            val constructor = clazz.getConstructor(Context::class.java)
            constructor.newInstance(context) as VideoGenerator
        }.getOrNull()

        if (generator != null) {
            return generator
        }

        return StubVideoGenerator(
            context = context,
            extraMessage = "No se pudo inicializar ffmpeg-kit en esta compilación."
        )
    }

    return StubVideoGenerator(context)
}
