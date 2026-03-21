package com.mentalgym.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeuralPurple,
    onPrimary = Color.White,
    primaryContainer = NeuralPurpleDark,
    onPrimaryContainer = NeuralPurpleLight,
    
    secondary = CognitiveTeal,
    onSecondary = BackgroundDark,
    secondaryContainer = CognitiveTealDark,
    onSecondaryContainer = CognitiveTealLight,
    
    tertiary = EnergyOrange,
    onTertiary = Color.White,
    
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondaryDark,
    
    error = Error,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeuralPurple,
    onPrimary = Color.White,
    primaryContainer = NeuralPurpleLight,
    onPrimaryContainer = NeuralPurpleDark,
    
    secondary = CognitiveTeal,
    onSecondary = Color.White,
    secondaryContainer = CognitiveTealLight,
    onSecondaryContainer = CognitiveTealDark,
    
    tertiary = EnergyOrange,
    onTertiary = Color.White,
    
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLightElevated,
    onSurfaceVariant = TextSecondaryLight,
    
    error = Error,
    onError = Color.White
)

@Composable
fun MentalGymTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            val window = activity.window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
