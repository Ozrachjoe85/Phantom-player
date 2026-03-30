package com.phantom.player.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PhantomPurple,
    onPrimary = PhantomBlack,
    primaryContainer = PhantomDarkPurple,
    onPrimaryContainer = PhantomPurple,
    
    secondary = PhantomPurple,
    onSecondary = PhantomBlack,
    secondaryContainer = PhantomDarkPurple,
    onSecondaryContainer = PhantomPurple,
    
    tertiary = PhantomOrange,
    onTertiary = PhantomBlack,
    tertiaryContainer = PhantomDarkPurple,
    onTertiaryContainer = PhantomOrange,
    
    error = PhantomOrange,
    onError = PhantomBlack,
    errorContainer = PhantomDarkPurple,
    onErrorContainer = PhantomOrange,
    
    background = PhantomBlack,
    onBackground = PhantomWhite,
    
    surface = PhantomBlack,
    onSurface = PhantomWhite,
    surfaceVariant = PhantomDarkPurple,
    onSurfaceVariant = PhantomWhite,
    
    outline = PhantomDarkPurple,
    outlineVariant = PhantomDarkPurple,
    
    inverseSurface = PhantomWhite,
    inverseOnSurface = PhantomBlack,
    inversePrimary = PhantomGreen,
    
    // Surface tints for glass effect
    surfaceTint = PhantomPurple,
    
    // Scrim for overlays
    scrim = PhantomBlack.copy(alpha = 0.8f)
)

private val LightColorScheme = lightColorScheme(
    primary = PhantomGreen,
    onPrimary = PhantomWhite,
    primaryContainer = PhantomWhite,
    onPrimaryContainer = PhantomBlack,
    
    secondary = PhantomPurple,
    onSecondary = PhantomWhite,
    secondaryContainer = PhantomWhite,
    onSecondaryContainer = PhantomBlack,
    
    tertiary = PhantomOrange,
    onTertiary = PhantomWhite,
    tertiaryContainer = PhantomWhite,
    onTertiaryContainer = PhantomBlack,
    
    background = PhantomWhite,
    onBackground = PhantomBlack,
    
    surface = PhantomWhite,
    onSurface = PhantomBlack,
    surfaceVariant = PhantomWhite,
    onSurfaceVariant = PhantomDarkPurple
)

@Composable
fun PhantomPlayerTheme(
    darkTheme: Boolean = true, // Force dark theme by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
