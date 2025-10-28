package com.example.birthday.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.DrawablePainter
import androidx.compose.ui.graphics.painter.Painter
import com.example.birthday.R
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.platform.LocalContext

object ActivityIcons {
    @Composable
    fun painterForId(id: Int): Painter {
        val context = LocalContext.current
        val drawableResId = iconFor(id)

        val drawable = remember(drawableResId, context) {
            AppCompatResources.getDrawable(context, drawableResId)
                ?: AppCompatResources.getDrawable(context, FALLBACK_ICON_ID)
        }?.mutate()

        val safeDrawable = requireNotNull(drawable) {
            "Unable to load activity icon for id=$id"
        }

        return remember(drawableResId, safeDrawable) {
            DrawablePainter(safeDrawable)
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
    private const val FALLBACK_ICON_ID: Int = R.mipmap.ic_regalo
}
