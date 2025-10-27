package com.example.birthday.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.birthday.R

object ActivityIcons {
    @Composable
    fun painterForId(id: Int): Painter = when (id) {
        1 -> painterResource(id = R.mipmap.ic_corona_foreground)
        2 -> painterResource(id = R.mipmap.ic_desayuno_foreground)
        3 -> painterResource(id = R.mipmap.ic_relax_foreground)
        4 -> painterResource(id = R.mipmap.ic_cafe_foreground)
        5 -> painterResource(id = R.mipmap.ic_regalo)
        6 -> painterResource(id = R.mipmap.ic_perfume_foreground)
        7 -> painterResource(id = R.mipmap.ic_collar_foreground)
        8 -> painterResource(id = R.mipmap.ic_sushi_foreground)
        else -> painterResource(id = R.mipmap.ic_regalo)
    }
}
