package com.example.birthday.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.birthday.R

object ActivityIcons {
    @Composable
    fun painterForId(id: Int): Painter = when (id) {
        1 -> painterResource(id = R.drawable.activity_corona)
        2 -> painterResource(id = R.drawable.activity_breakfast)
        3 -> painterResource(id = R.drawable.activity_relax)
        4 -> painterResource(id = R.drawable.activity_coffee)
        5 -> painterResource(id = R.drawable.activity_gift)
        6 -> painterResource(id = R.drawable.activity_perfume)
        7 -> painterResource(id = R.drawable.activity_necklace)
        8 -> painterResource(id = R.drawable.activity_sushi)
        else -> painterResource(id = R.drawable.activity_gift)
    }
}
