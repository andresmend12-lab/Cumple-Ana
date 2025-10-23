package com.example.birthday.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FreeBreakfast
import androidx.compose.material.icons.rounded.Sushi
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector

object ActivityIcons {
    fun forId(id: Int): ImageVector = when (id) {
        1 -> Icons.Rounded.EmojiEvents
        2 -> Icons.Rounded.FreeBreakfast
        3 -> Icons.Rounded.VolunteerActivism
        4 -> Icons.Rounded.Coffee
        5 -> Icons.Rounded.Cake
        6 -> Icons.Rounded.Spa
        7 -> Icons.Rounded.Diamond
        8 -> Icons.Rounded.Sushi
        else -> Icons.Rounded.Celebration
    }
}
