package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MathPrimaryDark,
    onPrimary = MathOnPrimaryDark,
    primaryContainer = MathPrimaryContainerDark,
    onPrimaryContainer = MathOnPrimaryContainerDark,
    secondary = MathSecondaryDark,
    onSecondary = MathOnSecondaryDark,
    secondaryContainer = MathSecondaryContainerDark,
    onSecondaryContainer = MathOnSecondaryContainerDark,
    tertiary = MathTertiaryDark,
    background = MathBackgroundDark,
    surface = MathSurfaceDark,
    surfaceVariant = MathSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = MathPrimaryLight,
    onPrimary = MathOnPrimaryLight,
    primaryContainer = MathPrimaryContainerLight,
    onPrimaryContainer = MathOnPrimaryContainerLight,
    secondary = MathSecondaryLight,
    onSecondary = MathOnSecondaryLight,
    secondaryContainer = MathSecondaryContainerLight,
    onSecondaryContainer = MathOnSecondaryContainerLight,
    tertiary = MathTertiaryLight,
    background = MathBackgroundLight,
    surface = MathSurfaceLight,
    surfaceVariant = MathSurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
