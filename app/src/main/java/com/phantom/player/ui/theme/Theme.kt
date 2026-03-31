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

private val LiquidMetalColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = DeepBlack,
    primaryContainer = AtmosphericDeep,
    onPrimaryContainer = ElectricBlue,
    
    secondary = HoloCyan,
    onSecondary = DeepBlack,
    secondaryContainer = AtmosphericDeep,
    onSecondaryContainer = HoloCyan,
    
    tertiary = MetallicGold,
    onTertiary = DeepBlack,
    tertiaryContainer = AtmosphericDeep,
    onTertiaryContainer = MetallicGold,
    
    error = HoloPink,
    onError = DeepBlack,
    errorContainer = AtmosphericDeep,
    onErrorContainer = HoloPink,
    
    background = AtmosphericBlue,
    onBackground = ChromeLight,
    
    surface = AtmosphericBlue,
    onSurface = ChromeLight,
    surfaceVariant = AtmosphericDeep,
    onSurfaceVariant = MetallicSilver,
    
    outline = ElectricBlue,
    outlineVariant = AtmosphericDeep,
    
    inverseSurface = ChromeLight,
    inverseOnSurface = DeepBlack,
    inversePrimary = HoloCyan,
    
    surfaceTint = ElectricBlue,
    scrim = DeepBlack.copy(alpha = 0.8f)
)

// Backwards compatibility
private val DarkColorScheme = LiquidMetalColorScheme

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = ChromeLight,
    primaryContainer = ChromeLight,
    onPrimaryContainer = DeepBlack,
    
    secondary = HoloCyan,
    onSecondary = ChromeLight,
    secondaryContainer = ChromeLight,
    onSecondaryContainer = DeepBlack,
    
    tertiary = MetallicGold,
    onTertiary = ChromeLight,
    tertiaryContainer = ChromeLight,
    onTertiaryContainer = DeepBlack,
    
    background = ChromeLight,
    onBackground = DeepBlack,
    
    surface = ChromeLight,
    onSurface = DeepBlack,
    surfaceVariant = MetallicSilver,
    onSurfaceVariant = AtmosphericDeep
)

@Composable
fun PhantomPlayerTheme(
    darkTheme: Boolean = true, // Always dark for liquid metal aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LiquidMetalColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
