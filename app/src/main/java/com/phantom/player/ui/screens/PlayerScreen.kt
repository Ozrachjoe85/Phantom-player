package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Status Bar with Spectrum
            TopStatusBar(isPlaying = isPlaying)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // MASSIVE Circular Album Art with Rings
                    CircularAlbumArtDisplay(
                        albumArtPath = currentSong?.albumArtPath,
                        isPlaying = isPlaying,
                        trackTitle = currentSong?.title ?: "NO TRACK",
                        artist = currentSong?.artist ?: "",
                        album = currentSong?.album ?: "",
                        currentPosition = currentPosition,
                        duration = duration
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Rainbow Waveform Visualizer
                    RainbowWaveformDisplay(isPlaying = isPlaying)
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            // Bottom Controls Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Playback Controls
                CircularPlaybackControls(
                    isPlaying = isPlaying,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onPrevious = { viewModel.skipToPrevious() },
                    onNext = { viewModel.skipToNext() }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Audio Info Bar
                AudioInfoBar()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Bottom Rainbow Waveform
                BottomRainbowWaveform(isPlaying = isPlaying)
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TopStatusBar(isPlaying: Boolean) {
    val bars = remember { List(40) { mutableStateOf(Random.nextFloat()) } }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                bars.forEach { it.value = Random.nextFloat() * 0.9f + 0.1f }
                kotlinx.coroutines.delay(50)
            }
        } else {
            bars.forEach { it.value = 0.1f }
        }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = (width / bars.size) * 0.7f
        val spacing = width / bars.size
        
        bars.forEachIndexed { index, heightState ->
            val barHeight = heightState.value * height
            val x = index * spacing
            
            // Rainbow gradient
            val hue = (index.toFloat() / bars.size) * 360f
            val color = when {
                index % 3 == 0 -> Color(0xFFFF6B00) // Orange
                index % 3 == 1 -> Color(0xFF9D00FF) // Purple
                else -> Color(0xFF00FF41) // Green
            }
            
            drawRect(
                color = color,
                topLeft = Offset(x, height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun CircularAlbumArtDisplay(
    albumArtPath: String?,
    isPlaying: Boolean,
    trackTitle: String,
    artist: String,
    album: String,
    currentPosition: Long,
    duration: Long
) {
    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = Modifier
            .size(340.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer rotating rings
        if (isPlaying) {
            Canvas(modifier = Modifier.size(360.dp).rotate(rotation)) {
                val center = Offset(size.width / 2, size.height / 2)
                
                // Rainbow outer ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFF6B00),
                            Color(0xFF9D00FF),
                            Color(0xFF00FF41),
                            Color(0xFF00E5FF),
                            Color(0xFFFF6B00)
                        )
                    ),
                    radius = size.minDimension / 2,
                    center = center,
                    style = Stroke(width = 8f)
                )
                
                // Middle ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFF00FF41),
                            Color(0xFF9D00FF),
                            Color(0xFFFF6B00),
                            Color(0xFF00FF41)
                        )
                    ),
                    radius = size.minDimension / 2 - 15f,
                    center = center,
                    style = Stroke(width = 4f)
                )
            }
        }
        
        // Main album art circle
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF1A0A2E),
                            Color(0xFF0A0A0A)
                        )
                    )
                )
                .border(
                    3.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF9D00FF),
                            Color(0xFF00FF41),
                            Color(0xFFFF6B00)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (albumArtPath != null) {
                AsyncImage(
                    model = albumArtPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color(0xFF9D00FF).copy(alpha = 0.3f)
                )
            }
            
            // Track info overlay at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xDD000000)
                            )
                        )
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = trackTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0xFFFF6B00)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (artist.isNotEmpty()) {
                    Text(
                        text = artist.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = Color(0xFF00FF41)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // Center time display
        if (duration > 0) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        fontSize = 48.sp,
                        letterSpacing = 2.sp
                    )
                )
                
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF9D00FF).copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RainbowWaveformDisplay(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_progress"
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A0A0A))
            .border(2.dp, Color(0xFF9D00FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        if (isPlaying) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val points = 150
            
            for (i in 0 until points) {
                val x = (i.toFloat() / points) * width
                val wave = sin((i + progress * 50) * 0.3).toFloat() * height * 0.35f
                
                val hue = (i.toFloat() / points) * 360f
                val color = when {
                    i % 4 == 0 -> Color(0xFFFF0000) // Red
                    i % 4 == 1 -> Color(0xFFFF6B00) // Orange
                    i % 4 == 2 -> Color(0xFFFFFF00) // Yellow
                    else -> Color(0xFF00FF00) // Green-ish
                }
                
                drawLine(
                    color = color.copy(alpha = 0.8f),
                    start = Offset(x, centerY - wave),
                    end = Offset(x, centerY + wave),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun CircularPlaybackControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF1A0A2E),
                        Color(0xFF0A0A0A),
                        Color(0xFF1A0A2E)
                    )
                )
            )
            .border(
                3.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF9D00FF),
                        Color(0xFF00FF41),
                        Color(0xFFFF6B00),
                        Color(0xFF9D00FF)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF9D00FF).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(3.dp, Color(0xFF9D00FF), CircleShape)
                    .clickable(onClick = onPrevious),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color(0xFF9D00FF),
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // MASSIVE Play/Pause button
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (isPlaying) {
                                listOf(
                                    Color(0xFF00FF41),
                                    Color(0xFF00FF41).copy(alpha = 0.6f)
                                )
                            } else {
                                listOf(
                                    Color(0xFFFF6B00),
                                    Color(0xFFFF6B00).copy(alpha = 0.6f)
                                )
                            }
                        )
                    )
                    .border(
                        4.dp,
                        if (isPlaying) Color(0xFF00FF41) else Color(0xFFFF6B00),
                        CircleShape
                    )
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color(0xFF0A0A0A),
                    modifier = Modifier.size(50.dp)
                )
            }
            
            // Next button
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFF6B00).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(3.dp, Color(0xFFFF6B00), CircleShape)
                    .clickable(onClick = onNext),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color(0xFFFF6B00),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun AudioInfoBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A0A2E))
            .border(1.dp, Color(0xFF9D00FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "F# FLAC / 24 BIT / 6 KHZ",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF00FF41)
            )
        )
        
        Text(
            text = "□ BASS BOOST",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFFFF6B00)
            )
        )
        
        Text(
            text = ")› DAC ENABLED",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF00E5FF)
            )
        )
    }
}

@Composable
fun BottomRainbowWaveform(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bottom_wave")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_progress"
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A0A0A))
            .border(2.dp, Color(0xFFFF6B00).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        if (isPlaying) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val points = 200
            
            val path = Path()
            path.moveTo(0f, centerY)
            
            for (i in 0 until points) {
                val x = (i.toFloat() / points) * width
                val wave = sin((i + progress * 60) * 0.25).toFloat() * height * 0.4f
                
                if (i == 0) {
                    path.moveTo(x, centerY + wave)
                } else {
                    path.lineTo(x, centerY + wave)
                }
            }
            
            // Draw filled gradient waveform
            val colors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFF6B00),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF00FFFF),
                Color(0xFF0000FF),
                Color(0xFFFF00FF),
                Color(0xFFFF0000)
            )
            
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(colors),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
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
