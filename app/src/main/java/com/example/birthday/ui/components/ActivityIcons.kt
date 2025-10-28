package com.example.birthday.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.birthday.R

object ActivityIcons {
    @Composable
    fun painterForId(id: Int): Painter = painterResource(id = iconFor(id))

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
        else -> R.mipmap.ic_regalo
    }
}
