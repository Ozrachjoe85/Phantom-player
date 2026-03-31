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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Phantom Dark Aesthetic Colors
private val PhantomSmoke = Color(0xFF1C1C1C)
private val PhantomGhost = Color(0xFF6A0DAD) // Deep purple
private val PhantomGlow = Color(0xFF9D4EDD) // Light purple
private val PhantomMist = Color(0xFF3C096C) // Dark purple
private val PhantomFire = Color(0xFFFF006E) // Hot pink accent
private val PhantomIce = Color(0xFF06FFA5) // Cyan accent
private val PhantomAsh = Color(0xFF0D0D0D) // Almost black

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
            .background(PhantomAsh)
    ) {
        // Animated smoke/mist background
        SmokeBackground(isPlaying = isPlaying)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Phantom logo/title
            PhantomHeader()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main album art with ghostly glow
            GhostlyAlbumArt(
                albumArtPath = currentSong?.albumArtPath,
                isPlaying = isPlaying
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Track info with fade effect
            PhantomTrackInfo(
                title = currentSong?.title ?: "Silence",
                artist = currentSong?.artist ?: "Unknown Specter",
                album = currentSong?.album ?: ""
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Spectral waveform
            SpectralWaveform(isPlaying = isPlaying)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress with ghostly trail
            GhostlyProgress(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { viewModel.seekTo(it) }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Ethereal controls
            EtherealControls(
                isPlaying = isPlaying,
                onPlayPause = { viewModel.togglePlayPause() },
                onPrevious = { viewModel.skipToPrevious() },
                onNext = { viewModel.skipToNext() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SmokeBackground(isPlaying: Boolean) {
    val smoke1 = rememberInfiniteTransition(label = "smoke1")
    val offset1 by smoke1.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smoke1_offset"
    )
    
    val smoke2 = rememberInfiniteTransition(label = "smoke2")
    val offset2 by smoke2.animateFloat(
        initialValue = 500f,
        targetValue = -500f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smoke2_offset"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Smoky gradient layers
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    PhantomMist.copy(alpha = if (isPlaying) 0.3f else 0.1f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.3f, offset1 % size.height)
            )
        )
        
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    PhantomGhost.copy(alpha = if (isPlaying) 0.2f else 0.05f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.7f, (offset2 + size.height) % size.height)
            )
        )
    }
}

@Composable
fun PhantomHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "P H A N T O M",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp,
                color = PhantomGlow.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
fun GhostlyAlbumArt(
    albumArtPath: String?,
    isPlaying: Boolean
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow effect
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .blur(40.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PhantomGlow.copy(alpha = glowAlpha),
                                PhantomFire.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // Main album art
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    2.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            PhantomGlow.copy(alpha = 0.6f),
                            PhantomFire.copy(alpha = 0.4f),
                            PhantomIce.copy(alpha = 0.3f)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .background(PhantomSmoke),
            contentAlignment = Alignment.Center
        ) {
            if (albumArtPath != null) {
                AsyncImage(
                    model = albumArtPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = if (isPlaying) 1f else 0.6f
                )
                
                // Ghostly overlay when playing
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        PhantomMist.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = PhantomGlow.copy(alpha = 0.3f)
                )
            }
        }
        
        // Corner accents
        CornerAccents(modifier = Modifier.size(240.dp))
    }
}

@Composable
fun CornerAccents(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // Top-left
        Canvas(modifier = Modifier
            .size(40.dp)
            .align(Alignment.TopStart)
        ) {
            drawLine(
                color = PhantomIce,
                start = Offset(0f, 20f),
                end = Offset(0f, 0f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = PhantomIce,
                start = Offset(0f, 0f),
                end = Offset(20f, 0f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
        
        // Top-right
        Canvas(modifier = Modifier
            .size(40.dp)
            .align(Alignment.TopEnd)
        ) {
            drawLine(
                color = PhantomFire,
                start = Offset(size.width, 20f),
                end = Offset(size.width, 0f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = PhantomFire,
                start = Offset(size.width, 0f),
                end = Offset(size.width - 20f, 0f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
        
        // Bottom-left
        Canvas(modifier = Modifier
            .size(40.dp)
            .align(Alignment.BottomStart)
        ) {
            drawLine(
                color = PhantomGlow,
                start = Offset(0f, size.height - 20f),
                end = Offset(0f, size.height),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = PhantomGlow,
                start = Offset(0f, size.height),
                end = Offset(20f, size.height),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
        
        // Bottom-right
        Canvas(modifier = Modifier
            .size(40.dp)
            .align(Alignment.BottomEnd)
        ) {
            drawLine(
                color = PhantomIce,
                start = Offset(size.width, size.height - 20f),
                end = Offset(size.width, size.height),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = PhantomIce,
                start = Offset(size.width, size.height),
                end = Offset(size.width - 20f, size.height),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun PhantomTrackInfo(
    title: String,
    artist: String,
    album: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = PhantomGlow
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = PhantomIce.copy(alpha = 0.8f)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        if (album.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = album,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = PhantomFire.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SpectralWaveform(isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "spectral")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PhantomSmoke.copy(alpha = 0.5f))
            .border(
                1.dp,
                PhantomGlow.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        if (isPlaying) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val points = 100
            
            val path = Path()
            
            for (i in 0 until points) {
                val x = (i.toFloat() / points) * width
                val frequency = 0.05f
                val amplitude = height * 0.3f
                val y = centerY + sin((i * frequency + phase * 0.01f).toDouble()).toFloat() * amplitude
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            // Draw spectral line with gradient
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PhantomIce,
                        PhantomGlow,
                        PhantomFire,
                        PhantomGlow,
                        PhantomIce
                    )
                ),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun GhostlyProgress(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PhantomSmoke.copy(alpha = 0.5f))
            .border(1.dp, PhantomGlow.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(PhantomMist.copy(alpha = 0.3f))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val seekProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((seekProgress * duration).toLong())
                    }
                }
        ) {
            // Glow trail
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .blur(8.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PhantomGlow,
                                PhantomFire,
                                PhantomIce
                            )
                        )
                    )
            )
            
            // Solid progress
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PhantomGlow,
                                PhantomFire,
                                PhantomIce
                            )
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = PhantomIce
                )
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = PhantomFire
                )
            )
        }
    }
}

@Composable
fun EtherealControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        PhantomMist.copy(alpha = 0.4f),
                        PhantomSmoke.copy(alpha = 0.6f),
                        PhantomMist.copy(alpha = 0.4f)
                    )
                )
            )
            .border(
                2.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        PhantomGlow.copy(alpha = 0.5f),
                        PhantomFire.copy(alpha = 0.5f),
                        PhantomIce.copy(alpha = 0.5f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
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
                        colors = listOf(
                            PhantomGlow.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, PhantomGlow.copy(alpha = 0.6f), CircleShape)
                .clickable(onClick = onPrevious),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = PhantomGlow,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Play/Pause - larger, more prominent
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isPlaying) {
                            listOf(
                                PhantomFire.copy(alpha = 0.6f),
                                PhantomFire.copy(alpha = 0.3f)
                            )
                        } else {
                            listOf(
                                PhantomIce.copy(alpha = 0.6f),
                                PhantomIce.copy(alpha = 0.3f)
                            )
                        }
                    )
                )
                .border(
                    3.dp,
                    if (isPlaying) PhantomFire else PhantomIce,
                    CircleShape
                )
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isPlaying) PhantomFire else PhantomIce,
                modifier = Modifier.size(40.dp)
            )
        }
        
        // Next
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PhantomIce.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, PhantomIce.copy(alpha = 0.6f), CircleShape)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = PhantomIce,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
