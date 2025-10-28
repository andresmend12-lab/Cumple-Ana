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
        1 -> R.drawable.ic_crown
        2 -> R.drawable.ic_breakfast
        3 -> R.drawable.ic_relax
        4 -> R.drawable.ic_coffee
        5 -> R.drawable.ic_cake
        6 -> R.drawable.ic_perfume
        7 -> R.drawable.ic_necklace
        8 -> R.drawable.ic_sushi
        else -> R.drawable.ic_gift_box
    }
}
