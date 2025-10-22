package com.example.birthday.media

import android.content.Context
import android.net.Uri
import com.example.birthday.R

class StubVideoGenerator(
    private val context: Context,
    private val extraMessage: String? = null
) : VideoGenerator {
    override val isAvailable: Boolean = false
    override val availabilityMessage: String? = buildString {
        append(context.getString(R.string.video_generation_unavailable))
        if (!extraMessage.isNullOrBlank()) {
            append('\n')
            append(extraMessage)
        }
    }

    override suspend fun generateVideo(
        clips: List<VideoGenerator.PhotoClip>,
        musicUri: Uri?,
        overlayTitles: Boolean,
        displayName: String,
        onProgress: (Int) -> Unit
    ): Uri {
        throw UnsupportedOperationException(availabilityMessage)
    }
}
