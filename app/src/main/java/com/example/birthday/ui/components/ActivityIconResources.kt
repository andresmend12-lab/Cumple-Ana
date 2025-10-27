package com.example.birthday.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BreakfastDining
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.RamenDining
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.birthday.R

@Composable
fun activityIconPainter(title: String): Painter {
    return when (title) {
        "Coronación" -> rememberVectorPainter(Icons.Rounded.EmojiEvents)
        "Desayuno" -> rememberVectorPainter(Icons.Rounded.BreakfastDining)
        "Cuídate" -> rememberVectorPainter(Icons.Rounded.SelfImprovement)
        "Que rico el cafesito" -> rememberVectorPainter(Icons.Rounded.LocalCafe)
        "Una nunca es suficiente" -> rememberVectorPainter(Icons.Rounded.Cake)
        "Siempre divina" -> painterResource(R.drawable.ic_perfume)
        "El dorado te queda bien" -> rememberVectorPainter(Icons.Rounded.Diamond)
        "Acabar de la mejor forma" -> rememberVectorPainter(Icons.Rounded.RamenDining)
        else -> rememberVectorPainter(Icons.Rounded.Star)
    }
}

@Composable
fun ActivityHeader(title: String) {
    val painter = activityIconPainter(title)
    val safeTitle = if (title.isBlank()) "la actividad" else title
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = "Ilustración de $safeTitle",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp)
        )
    }
}
