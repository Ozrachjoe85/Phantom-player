package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.PlayerViewModel
import com.phantom.player.ui.viewmodel.EqViewModel
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

/**
 * LIQUID METAL HOLOGRAPHIC DESIGN
 * 
 * Concept: Fluid metallic surfaces, holographic refractions, 3D depth
 * Colors: Chrome silver, electric blue, holographic rainbow, deep black
 * Effects: Liquid morphing, light refractions, metallic reflections
 */

// Metallic color palette
private val MetallicSilver = Color(0xFFC0C0C0)
private val ChromeLight = Color(0xFFE8E8E8)
private val ElectricBlue = Color(0xFF00D4FF)
private val HoloPink = Color(0xFFFF00FF)
private val HoloCyan = Color(0xFF00FFFF)
private val DeepBlack = Color(0xFF000000)
private val MetallicGold = Color(0xFFFFD700)

@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    eqViewModel: EqViewModel = hiltViewModel()
) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val duration by playerViewModel.duration.collectAsState()
    
    // EQ state for real-time visualization
    val isAutoEqActive by eqViewModel.isAutoEqActive.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF000814),
                        Color(0xFF001233),
                        Color(0xFF000814)
                    )
                )
            )
    ) {
        // Holographic background particles
        HolographicParticles(isPlaying = isPlaying)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Liquid Metal Album Display
            LiquidMetalAlbumArt(
                albumArtPath = currentSong?.albumArtPath,
                isPlaying = isPlaying,
                trackTitle = currentSong?.title ?: "NO TRACK",
                artist = currentSong?.artist ?: "UNKNOWN"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Real-time EQ Visualization
            LiveEqVisualizer(
                eqBands = eqBands,
                isAutoEqActive = isAutoEqActive,
                isPlaying = isPlaying
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Holographic Progress Bar
            HolographicProgressBar(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { playerViewModel.seekTo(it) }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Liquid Metal Controls
            LiquidMetalControls(
                isPlaying = isPlaying,
                onPlayPause = { playerViewModel.togglePlayPause() },
                onPrevious = { playerViewModel.skipToPrevious() },
                onNext = { playerViewModel.skipToNext() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HolographicParticles(isPlaying: Boolean) {
    val particles = remember {
        List(30) {
            HoloParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 60f + 20f,
                speed = Random.nextFloat() * 0.003f + 0.001f,
                hue = Random.nextFloat()
            )
        }
    }
    
    val time by rememberInfiniteTransition(label = "holo").animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    if (isPlaying) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val currentY = (particle.y + time * particle.speed) % 1f
                val x = size.width * particle.x
                val y = size.height * currentY
                
                val colors = listOf(
                    ElectricBlue.copy(alpha = 0.3f),
                    HoloPink.copy(alpha = 0.2f),
                    HoloCyan.copy(alpha = 0.3f)
                )
                
                val colorIndex = ((time * 0.1f + particle.hue) % 3f).toInt()
                
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            colors[colorIndex % colors.size],
                            Color.Transparent
                        )
                    ),
                    radius = particle.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}

data class HoloParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val hue: Float
)

@Composable
fun LiquidMetalAlbumArt(
    albumArtPath: String?,
    isPlaying: Boolean,
    trackTitle: String,
    artist: String
) {
    val rotation by rememberInfiniteTransition(label = "metal_rotate").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val shimmer by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Liquid metal outer ring
        Canvas(
            modifier = Modifier
                .size(320.dp)
                .rotate(if (isPlaying) rotation else 0f)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            
            // Chrome metallic ring
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        MetallicSilver,
                        ChromeLight,
                        ElectricBlue,
                        ChromeLight,
                        MetallicSilver
                    )
                ),
                radius = radius,
                center = center,
                style = Stroke(width = 6f)
            )
            
            // Inner holographic ring
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        HoloCyan.copy(alpha = 0.6f),
                        HoloPink.copy(alpha = 0.6f),
                        ElectricBlue.copy(alpha = 0.6f),
                        HoloCyan.copy(alpha = 0.6f)
                    )
                ),
                radius = radius - 10f,
                center = center,
                style = Stroke(width = 2f)
            )
        }
        
        // Main album art
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF001233),
                            Color(0xFF000814)
                        )
                    )
                )
                .border(
                    3.dp,
                    Brush.linearGradient(
                        listOf(
                            ElectricBlue,
                            HoloCyan,
                            ElectricBlue
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Metallic shimmer effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                MetallicSilver.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            start = Offset(shimmer * 1000f, 0f),
                            end = Offset(shimmer * 1000f + 200f, 500f)
                        )
                    )
            )
            
            if (albumArtPath != null) {
                AsyncImage(
                    model = albumArtPath,
                    contentDescription = null,
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = ElectricBlue.copy(alpha = 0.3f)
                )
            }
        }
        
        // Track info below
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 50.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xDD000814),
                            Color(0xEE001233),
                            Color(0xDD000814)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            ElectricBlue.copy(alpha = 0.5f),
                            HoloCyan.copy(alpha = 0.5f),
                            ElectricBlue.copy(alpha = 0.5f)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = trackTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ElectricBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = HoloCyan.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * REAL-TIME EQ VISUALIZER
 * Shows actual EQ band values with animated bars
 */
@Composable
fun LiveEqVisualizer(
    eqBands: List<com.phantom.player.data.local.database.entities.EqBand>,
    isAutoEqActive: Boolean,
    isPlaying: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x33001233),
                        Color(0x22000814)
                    )
                )
            )
            .border(
                2.dp,
                Brush.horizontalGradient(
                    listOf(
                        if (isAutoEqActive) MetallicGold else ElectricBlue,
                        if (isAutoEqActive) MetallicGold else HoloCyan,
                        if (isAutoEqActive) MetallicGold else ElectricBlue
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAutoEqActive) "AUTO EQ ACTIVE" else "MANUAL EQ",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = if (isAutoEqActive) MetallicGold else ElectricBlue
            )
            
            Icon(
                if (isAutoEqActive) Icons.Default.AutoAwesome else Icons.Default.Tune,
                contentDescription = null,
                tint = if (isAutoEqActive) MetallicGold else HoloCyan,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Real-time EQ bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            if (eqBands.isNotEmpty()) {
                val width = size.width
                val height = size.height
                val barWidth = (width / eqBands.size) * 0.6f
                val spacing = width / eqBands.size
                
                eqBands.forEachIndexed { index, band ->
                    // Convert dB value to visual height (-12 to +12 dB range)
                    val normalizedValue = (band.value + 12f) / 24f  // 0 to 1
                    val barHeight = normalizedValue * height
                    val x = index * spacing + spacing * 0.2f
                    
                    // Bar color based on value
                    val barColor = when {
                        band.value > 6f -> MetallicGold
                        band.value > 0f -> ElectricBlue
                        band.value < -6f -> HoloPink
                        else -> HoloCyan
                    }
                    
                    // Glow effect
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                barColor.copy(alpha = 0.3f),
                                barColor.copy(alpha = 0.1f)
                            )
                        ),
                        topLeft = Offset(x - 2f, height - barHeight - 2f),
                        size = androidx.compose.ui.geometry.Size(barWidth + 4f, barHeight + 4f)
                    )
                    
                    // Solid bar
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                barColor,
                                barColor.copy(alpha = 0.7f)
                            )
                        ),
                        topLeft = Offset(x, height - barHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )
                    
                    // Frequency label
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(180, 0, 212, 255)
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(
                            "${band.frequency}",
                            x + barWidth / 2,
                            height + 30f,
                            paint
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HolographicProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x22001233))
            .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = ElectricBlue
            )
            
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = HoloCyan.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress bar
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF001233))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val seekProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((seekProgress * duration).toLong())
                    }
                }
        ) {
            // Progress fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                ElectricBlue,
                                HoloCyan,
                                HoloPink.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            
            // Glow effect
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .blur(8.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                ElectricBlue.copy(alpha = 0.5f),
                                HoloCyan.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun LiquidMetalControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f),
        label = "scale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0x44001233),
                        Color(0x33000814),
                        Color(0x44001233)
                    )
                )
            )
            .border(
                2.dp,
                Brush.horizontalGradient(
                    listOf(
                        ElectricBlue,
                        HoloCyan,
                        HoloPink,
                        HoloCyan,
                        ElectricBlue
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            ElectricBlue.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, ElectricBlue, CircleShape)
                .clickable(onClick = onPrevious),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = ElectricBlue,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Play/Pause - LIQUID METAL
        Box(
            modifier = Modifier
                .size(90.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        if (isPlaying) {
                            listOf(
                                ElectricBlue,
                                ElectricBlue.copy(alpha = 0.7f),
                                Color(0xFF001233)
                            )
                        } else {
                            listOf(
                                MetallicSilver,
                                ChromeLight.copy(alpha = 0.7f),
                                Color(0xFF001233)
                            )
                        }
                    )
                )
                .border(
                    3.dp,
                    if (isPlaying) ElectricBlue else MetallicSilver,
                    CircleShape
                )
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            // Holographic glow
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .blur(20.dp)
                    .background(
                        if (isPlaying) ElectricBlue.copy(alpha = 0.6f) else Color.Transparent,
                        CircleShape
                    )
            )
            
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isPlaying) DeepBlack else ElectricBlue,
                modifier = Modifier.size(45.dp)
            )
        }
        
        // Next
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            HoloCyan.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, HoloCyan, CircleShape)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = HoloCyan,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
