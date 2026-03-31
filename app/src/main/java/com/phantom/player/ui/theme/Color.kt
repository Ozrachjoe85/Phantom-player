package com.phantom.player.ui.theme

import androidx.compose.ui.graphics.Color

// LIQUID METAL HOLOGRAPHIC Color Palette
// Chrome surfaces, electric blue energy, holographic refractions

// Core Liquid Metal Colors
val MetallicSilver = Color(0xFFC0C0C0)      // Chrome surfaces
val ChromeLight = Color(0xFFE8E8E8)         // Bright metallic highlights
val MetallicGold = Color(0xFFFFD700)        // Auto EQ indicator

// Electric Energy Colors
val ElectricBlue = Color(0xFF00D4FF)        // Primary energy color
val HoloCyan = Color(0xFF00FFFF)            // Holographic cyan
val HoloPink = Color(0xFFFF00FF)            // Holographic pink/magenta

// Depth & Atmosphere
val DeepBlack = Color(0xFF000000)           // Pure void
val AtmosphericBlue = Color(0xFF000814)     // Deep blue-black
val AtmosphericDeep = Color(0xFF001233)     // Medium blue-black

// Surface & Glass
val SurfaceGlass = Color(0x33001233)        // Translucent surface
val SurfaceDeep = Color(0x22000814)         // Deep translucent

// Backwards compatibility (so existing code doesn't break)
val PhantomBlack = AtmosphericBlue
val PhantomPurple = ElectricBlue
val PhantomGreen = HoloCyan
val PhantomOrange = HoloPink
val PhantomDarkPurple = AtmosphericDeep
val PhantomWhite = ChromeLight

val NeonPurple = ElectricBlue
val NeonGreen = HoloCyan
val NeonOrange = MetallicGold
