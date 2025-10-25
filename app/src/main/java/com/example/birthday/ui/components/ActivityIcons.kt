package com.example.birthday.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FreeBreakfast
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.example.birthday.R

object ActivityIcons {
    @Composable
    fun painterForId(id: Int): Painter = when (id) {
        1 -> rememberVectorPainter(Icons.Rounded.EmojiEvents)
        2 -> rememberVectorPainter(Icons.Rounded.FreeBreakfast)
        3 -> rememberVectorPainter(Icons.Rounded.VolunteerActivism)
        4 -> rememberVectorPainter(Icons.Rounded.Coffee)
        5 -> rememberVectorPainter(Icons.Rounded.Cake)
        6 -> rememberVectorPainter(Icons.Rounded.Spa)
        7 -> rememberVectorPainter(Icons.Rounded.Diamond)
        8 -> painterResource(id = R.drawable.ic_sushi)
        else -> rememberVectorPainter(Icons.Rounded.Celebration)
    }
}
