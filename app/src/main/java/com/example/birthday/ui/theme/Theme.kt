package com.example.birthday.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralVariant,
    onPrimaryContainer = PrimaryText,
    secondary = SunnyYellow,
    onSecondary = PrimaryText,
    tertiary = MintyGreen,
    onTertiary = Color.White,
    background = WarmIvory,
    onBackground = PrimaryText,
    surface = WarmIvory,
    onSurface = PrimaryText,
    outline = BoldBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralVariant,
    onPrimary = Color.Black,
    secondary = SunnyYellow,
    tertiary = MintyGreen,
    outline = BoldBlue
)

@Composable
fun CumpleAnaTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CumpleAnaTypography,
        shapes = CumpleAnaShapes,
        content = content
    )
}
