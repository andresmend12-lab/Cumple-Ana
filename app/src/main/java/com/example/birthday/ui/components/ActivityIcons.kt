package com.example.birthday.ui.components

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import com.example.birthday.R
import kotlin.math.roundToInt

object ActivityIcons {
    @Composable
    fun painterForId(id: Int): Painter {
        val context = LocalContext.current
        val drawableResId = iconFor(id)

        return remember(drawableResId, context) {
            val drawable = AppCompatResources.getDrawable(context, drawableResId)
                ?: AppCompatResources.getDrawable(context, FALLBACK_ICON_ID)
            val safeDrawable = requireNotNull(drawable) {
                "Unable to load activity icon for id=$id"
            }

            val width = safeDrawable.intrinsicWidth.takeIf { it > 0 } ?: DEFAULT_ICON_SIZE
            val height = safeDrawable.intrinsicHeight.takeIf { it > 0 } ?: DEFAULT_ICON_SIZE

            val scaledWidth = (width * ICON_SCALE_FACTOR).roundToInt().coerceAtLeast(width)
            val scaledHeight = (height * ICON_SCALE_FACTOR).roundToInt().coerceAtLeast(height)

            val bitmap = safeDrawable.toBitmap(
                width = scaledWidth,
                height = scaledHeight,
                config = Bitmap.Config.ARGB_8888
            )

            BitmapPainter(bitmap.asImageBitmap())
        }
    }

    @DrawableRes
    private fun iconFor(id: Int): Int = when (id) {
        1 -> R.mipmap.ic_corona_foreground
        2 -> R.mipmap.ic_desayuno_foreground
        3 -> R.mipmap.ic_relax_foreground
        4 -> R.mipmap.ic_cafe_foreground
        5 -> R.mipmap.ic_regalo
        6 -> R.mipmap.ic_perfume_foreground
        7 -> R.mipmap.ic_collar_foreground
        8 -> R.mipmap.ic_sushi_foreground
        else -> FALLBACK_ICON_ID
    }

    @DrawableRes
    private val FALLBACK_ICON_ID: Int = R.mipmap.ic_regalo

    private const val DEFAULT_ICON_SIZE: Int = 256
    private const val ICON_SCALE_FACTOR: Float = 2f
}
