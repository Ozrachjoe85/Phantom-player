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
    primary = PhantomCyan,
    onPrimary = PhantomBlack,
    primaryContainer = PhantomDarkGray,
    onPrimaryContainer = PhantomCyan,
    
    secondary = PhantomPurple,
    onSecondary = PhantomBlack,
    secondaryContainer = PhantomDarkGray,
    onSecondaryContainer = PhantomPurple,
    
    tertiary = PhantomPink,
    onTertiary = PhantomBlack,
    tertiaryContainer = PhantomDarkGray,
    onTertiaryContainer = PhantomPink,
    
    error = PhantomPink,
    onError = PhantomBlack,
    errorContainer = PhantomDarkGray,
    onErrorContainer = PhantomPink,
    
    background = PhantomBlack,
    onBackground = PhantomWhite,
    
    surface = SurfaceDark,
    onSurface = PhantomWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = PhantomLightGray,
    
    outline = PhantomGray,
    outlineVariant = PhantomDarkGray,
    
    inverseSurface = PhantomWhite,
    inverseOnSurface = PhantomBlack,
    inversePrimary = PhantomBlue
)

private val LightColorScheme = lightColorScheme(
    primary = PhantomBlue,
    onPrimary = PhantomWhite,
    primaryContainer = PhantomLightGray,
    onPrimaryContainer = PhantomBlack,
    
    secondary = PhantomPurple,
    onSecondary = PhantomWhite,
    secondaryContainer = PhantomLightGray,
    onSecondaryContainer = PhantomBlack,
    
    tertiary = PhantomPink,
    onTertiary = PhantomWhite,
    tertiaryContainer = PhantomLightGray,
    onTertiaryContainer = PhantomBlack,
    
    background = PhantomWhite,
    onBackground = PhantomBlack,
    
    surface = PhantomWhite,
    onSurface = PhantomBlack,
    surfaceVariant = PhantomLightGray,
    onSurfaceVariant = PhantomDarkGray
)

@Composable
fun PhantomPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
